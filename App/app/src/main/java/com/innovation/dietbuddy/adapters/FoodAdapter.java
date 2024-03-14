package com.innovation.dietbuddy.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.innovation.dietbuddy.FoodAnalysisActivity;
import com.innovation.dietbuddy.R;
import com.innovation.dietbuddy.data.FoodData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.ViewHolder> {

    private Context context;
    private ArrayList<FoodData> foodDataArrayList;

    public FoodAdapter() {
        foodDataArrayList = new ArrayList<>();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFoodDataArrayList(ArrayList<FoodData> foodDataArrayList) {
        this.foodDataArrayList = foodDataArrayList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.food_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        FoodData foodData = foodDataArrayList.get(position);
        Picasso.get().load(foodData.getImage()).into(holder.imageView);
        holder.name.setText(foodData.getName());
        holder.calories.setText(foodData.getEnergy()+" Calories");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FoodAnalysisActivity.class);
                intent.putExtra("dbName",foodData.getName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodDataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, calories;
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_food_name);
            calories = itemView.findViewById(R.id.tv_food_calories);
            imageView = itemView.findViewById(R.id.iv_food_item);
        }
    }
}
