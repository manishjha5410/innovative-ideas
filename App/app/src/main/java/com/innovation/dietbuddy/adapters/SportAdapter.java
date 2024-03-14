package com.devilopers.personalcoach.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.devilopers.personalcoach.R;
import com.devilopers.personalcoach.datamodels.SportData;
import com.devilopers.personalcoach.sport_video;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SportAdapter extends RecyclerView.Adapter<SportAdapter.ViewHolder> {
    private Context context;
    private ArrayList<SportData> sportDataArrayList;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context=parent.getContext();
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.sport_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        SportData sportData = sportDataArrayList.get(position);
        holder.name.setText(sportData.getName());
        Picasso.get().load(sportData.getImage()).into(holder.image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(context, sport_video.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("sport",sportData.getName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(sportDataArrayList == null) return 0;
        return sportDataArrayList.size();
    }

    public void setSportDataArrayList(ArrayList<SportData> sportDataArrayList) {
        this.sportDataArrayList = sportDataArrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_sport_item);
            image = itemView.findViewById(R.id.iv_sport_item);

        }
    }
}