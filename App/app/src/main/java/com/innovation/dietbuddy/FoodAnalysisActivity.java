package com.innovation.dietbuddy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.innovation.dietbuddy.ml.Dietbuddy;
import com.squareup.picasso.Picasso;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class FoodAnalysisActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String filePath;
    private int imageSize;
    private DataSnapshot foodSnapshot;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                filePath = photoFile.getPath();
                Uri photoURI = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID+".fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }
    String currentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = System.currentTimeMillis()+"";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                timeStamp,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if(resultCode != RESULT_OK) {
                finish();
                return;
            }
            Bitmap imageBitmap = BitmapFactory.decodeFile(filePath);
            ((ImageView) findViewById(R.id.iv_captured_photo)).setImageBitmap(imageBitmap);
            classify(imageBitmap);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_analysis);
        if(getIntent().hasExtra("dbName")) {
            String prediction = getIntent().getStringExtra("dbName");
            ((TextView)findViewById(R.id.tv_name)).setText(prediction);
            FirebaseDatabase.getInstance().getReference("food_items").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        foodSnapshot = snapshot;
                        String results = "";
                        Picasso.get().load(foodSnapshot.child(prediction).child("image").getValue().toString()).into((ImageView)findViewById(R.id.iv_captured_photo));
                        results = results + "Protein: " + foodSnapshot.child(prediction).child("protein").getValue().toString() + "g\n";
                        results = results + "Carbohydrates: " + foodSnapshot.child(prediction).child("carbohydrate").getValue().toString() + "g\n";
                        results = results + "Fats: " + foodSnapshot.child(prediction).child("fats").getValue().toString() + "g\n";
                        results = results + "Energy: " + foodSnapshot.child(prediction).child("energy").getValue().toString() + "cal\n";
                        ((TextView) findViewById(R.id.results)).setText(results);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
            return;
        }
        if(getIntent().hasExtra("bitmap")) {
            Bitmap bitmap= BitmapFactory.decodeFile(new File(getFilesDir(),getIntent().getStringExtra("bitmap")).getPath());
            classify(bitmap);
        }
        else dispatchTakePictureIntent();
    }

    @SuppressLint("SetTextI18n")
    private void classify(Bitmap image) {
        try {
            //converting to square image
            int dimension = Math.min(image.getWidth(), image.getHeight());
            imageSize = 224;
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
            ((ImageView) findViewById(R.id.iv_captured_photo)).setImageBitmap(image);
            try {
                Dietbuddy model = Dietbuddy.newInstance(this);
                // Creates inputs for reference.
                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, imageSize, imageSize, 3}, DataType.FLOAT32);
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
                byteBuffer.order(ByteOrder.nativeOrder());
                int[] intValues = new int[imageSize * imageSize];
                image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
                int pixel = 0;
                for(int i=0;i<imageSize;i++) {
                    for(int j=0;j<imageSize;j++) {
                        int val = intValues[pixel++];
                        byteBuffer.putFloat(((val>>16)&0xFF)*(1.0f/255.0f));
                        byteBuffer.putFloat(((val>>8)&0xFF)*(1.0f/255.0f));
                        byteBuffer.putFloat((val&0xFF)*(1.0f/255.0f));
                    }
                }
                inputFeature0.loadBuffer(byteBuffer);

                // Runs model inference and gets result.
                Dietbuddy.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
                float[] confidence = outputFeature0.getFloatArray();
                int index = 0;
                for(int i=0;i<confidence.length;i++) {
                    if(confidence[i]>confidence[index]) index = i;
                }
                try{
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("labels.txt"), StandardCharsets.UTF_8));
                    ArrayList<String> arrayList = new ArrayList<>();
                    String str;
                    while ((str = bufferedReader.readLine())!=null) arrayList.add(str.substring(str.indexOf(" ")+1));
                    String prediction = arrayList.get(index);
                    ((TextView)findViewById(R.id.tv_name)).setText(prediction);
                    FirebaseDatabase.getInstance().getReference("food_items").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            try {
                                foodSnapshot = snapshot;
                                String results = "";
                                results = results + "Protein: " + foodSnapshot.child(prediction).child("protein").getValue().toString() + "g\n";
                                results = results + "Carbohydrates: " + foodSnapshot.child(prediction).child("carbohydrate").getValue().toString() + "g\n";
                                results = results + "Fats: " + foodSnapshot.child(prediction).child("fats").getValue().toString() + "g\n";
                                results = results + "Energy: " + foodSnapshot.child(prediction).child("energy").getValue().toString() + "cal\n";
                                ((TextView) findViewById(R.id.results)).setText(results);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Releases model resources if no longer used.
                model.close();
            } catch (IOException ignored) {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}