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

import com.innovation.dietbuddy.FoodListDisplayActivity;
import com.innovation.dietbuddy.R;
import com.innovation.dietbuddy.data.CategoryData;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private ArrayList<CategoryData> categoryDataArrayList;
    private Context context;
    public CategoryAdapter() {
        categoryDataArrayList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.category_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        CategoryData categoryData = categoryDataArrayList.get(position);
        Picasso.get().load(categoryData.getImage()).into(holder.imageView);
        holder.textView.setText(categoryData.getName()+" ("+categoryData.getItems().size()+")");
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, FoodListDisplayActivity.class);
                intent.putExtra("dbName",categoryData.getName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryDataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_category);
            textView = itemView.findViewById(R.id.tv_category);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setCategoryDataArrayList(ArrayList<CategoryData> categoryDataArrayList) {
        this.categoryDataArrayList = categoryDataArrayList;
        notifyDataSetChanged();
    }
}
