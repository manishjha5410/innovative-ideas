package com.innovation.dietbuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.innovation.dietbuddy.adapters.FoodAdapter;
import com.innovation.dietbuddy.data.FoodData;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;

public class FoodListDisplayActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FoodAdapter foodAdapter;
    private ArrayList<FoodData> foodDataArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list_display);
        foodDataArrayList = new ArrayList<>();
        foodAdapter = new FoodAdapter();
        recyclerView = findViewById(R.id.rv_food_items);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(foodAdapter);
        ((TextView)findViewById(R.id.tv_category)).setText(getIntent().getStringExtra("dbName"));
        FirebaseDatabase.getInstance().getReference("food_categories").child(getIntent().getStringExtra("dbName")).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try{
                    ArrayList<String> arrayList = (ArrayList<String>) snapshot.child("list").getValue();
                    FirebaseDatabase.getInstance().getReference("food_items").addListenerForSingleValueEvent(new ValueEventListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            foodDataArrayList.clear();
                            for(String str: arrayList) {
                                DataSnapshot dataSnapshot = snapshot.child(str);
                                FoodData foodData = new FoodData();
                                foodData.setName(dataSnapshot.getKey());
                                foodData.setImage(dataSnapshot.child("image").getValue().toString());
                                foodData.setUnit(dataSnapshot.child("unit").getValue().toString());
                                foodData.setEnergy(dataSnapshot.child("energy").getValue().toString());
                                foodData.setProtein(dataSnapshot.child("protein").getValue().toString());
                                foodData.setFats(dataSnapshot.child("fats").getValue().toString());
                                foodDataArrayList.add(foodData);
                            }
                            foodAdapter.setFoodDataArrayList(foodDataArrayList);
                            ((TextView)findViewById(R.id.tv_no_of_items)).setText(foodDataArrayList.size()+" items");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}