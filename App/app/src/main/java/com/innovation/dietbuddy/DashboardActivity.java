package com.innovation.dietbuddy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.innovation.dietbuddy.adapters.CategoryAdapter;
import com.innovation.dietbuddy.adapters.FoodAdapter;
import com.innovation.dietbuddy.data.CategoryData;
import com.innovation.dietbuddy.data.FoodData;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DashboardActivity extends AppCompatActivity {

    private static final int PICK_FILE_RESULT_CODE = 999;
    private ArrayList<CategoryData> categoryDataArrayList;
    private ArrayList<FoodData> foodDataArrayList;
    private CategoryAdapter categoryAdapter;
    private FoodAdapter foodAdapter;
    private RecyclerView categoryRecycler;
    private RecyclerView foodRecycler;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        categoryDataArrayList = new ArrayList<>();
        foodDataArrayList = new ArrayList<>();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null) {
            Picasso.get().load(String.valueOf(user.getPhotoUrl())).into((ImageView)findViewById(R.id.iv_profile));
            String userName = user.getDisplayName();
            if(userName!=null && userName.contains(" ")) userName = userName.substring(0, userName.indexOf(" "));
            ((TextView)findViewById(R.id.tv_name)).setText("Hello "+userName);
        }
        categoryRecycler = findViewById(R.id.rv_category);
        categoryRecycler.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        categoryAdapter = new CategoryAdapter();
        categoryRecycler.setAdapter(categoryAdapter);
        foodRecycler = findViewById(R.id.rv_food_items);
        foodRecycler.setLayoutManager(new LinearLayoutManager(this));
        foodAdapter = new FoodAdapter();
        foodRecycler.setAdapter(foodAdapter);
        loadFoodItems();
        loadCategories();
        listenForSearches();
        findViewById(R.id.cv_pick_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImage();
            }
        });
        findViewById(R.id.cv_open_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardActivity.this, FoodAnalysisActivity.class));
            }
        });
    }

    private void listenForSearches() {
        ((EditText)findViewById(R.id.et_search)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String searchText = charSequence.toString().toLowerCase();
                        if(searchText.replace(" ","").length()==0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    foodRecycler.setVisibility(View.GONE);
                                }
                            });
                            return;
                        }
                        ArrayList<FoodData> filteredList = new ArrayList<>();
                        for(FoodData foodData: foodDataArrayList) {
                            if(foodData.getName().toLowerCase().contains(searchText)) filteredList.add(foodData);
                        }
                        if(filteredList.size()==0) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    foodRecycler.setVisibility(View.GONE);
                                }
                            });
                            return;
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                foodAdapter.setFoodDataArrayList(filteredList);
                                foodRecycler.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void loadFoodItems() {
        FirebaseDatabase.getInstance().getReference("food_items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                foodDataArrayList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    try {
                        FoodData foodData = new FoodData();
                        foodData.setName(dataSnapshot.getKey());
                        foodData.setImage(dataSnapshot.child("image").getValue().toString());
                        foodData.setUnit(dataSnapshot.child("unit").getValue().toString());
                        foodData.setEnergy(dataSnapshot.child("energy").getValue().toString());
                        foodData.setProtein(dataSnapshot.child("protein").getValue().toString());
                        foodData.setFats(dataSnapshot.child("fats").getValue().toString());
                        foodDataArrayList.add(foodData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadCategories() {
        FirebaseDatabase.getInstance().getReference("food_categories").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryDataArrayList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    try{
                        CategoryData categoryData = new CategoryData();
                        categoryData.setName(dataSnapshot.getKey());
                        categoryData.setImage(dataSnapshot.child("image").getValue().toString());
                        categoryData.setItems((ArrayList<String>)dataSnapshot.child("list").getValue());
                        categoryDataArrayList.add(categoryData);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                categoryAdapter.setCategoryDataArrayList(categoryDataArrayList);
                if(categoryDataArrayList.size()==0) categoryRecycler.setVisibility(View.GONE);
                else categoryRecycler.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent,"Complete action using..."), PICK_FILE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==PICK_FILE_RESULT_CODE) {
            if(resultCode==RESULT_OK) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Uri uri = data.getData();
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(DashboardActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        String mimeType = getContentResolver().getType(uri);
                        File file = new File(getFilesDir(),"image."+mimeType.substring(mimeType.indexOf("/")+1));
                        if(!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(file));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(DashboardActivity.this, FoodAnalysisActivity.class);
                        intent.putExtra("bitmap",file.getName());
                        startActivity(intent);
                    }
                }).start();
            }
        }super.onActivityResult(requestCode, resultCode, data);
    }
}