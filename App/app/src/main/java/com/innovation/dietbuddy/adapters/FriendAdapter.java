package com.devilopers.personalcoach.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.devilopers.personalcoach.AppConstants;

import com.devilopers.personalcoach.R;
import com.devilopers.personalcoach.datamodels.FriendData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private ArrayList<FriendData> friendDataArrayList;
    private ArrayList<String> sentRequestList = new ArrayList<>();
    private ArrayList<String> receivedRequestList = new ArrayList<>();
    private ArrayList<String> friendsArrayList = new ArrayList<>();
    private String mode;

    public FriendAdapter(String mode) {
        this.mode = mode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_item,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendData friendData = friendDataArrayList.get(position);
        holder.setIsRecyclable(false);
        holder.name.setText(friendData.getName());
        Picasso.get().load(friendData.getImage()).into(holder.image);
        holder.add.setVisibility(View.GONE);
        holder.waiting.setVisibility(View.GONE);
        holder.reject.setVisibility(View.GONE);
        holder.accept.setVisibility(View.GONE);
        switch (mode) {
            case AppConstants.MODE_FRIEND_REQUEST: {
                holder.reject.setVisibility(View.VISIBLE);
                holder.accept.setVisibility(View.VISIBLE);
                break;
            }
            case AppConstants.MODE_FRIEND_SEARCH: {
                if (friendsArrayList.contains(friendData.getId())) {
                    holder.reject.setVisibility(View.VISIBLE);
                }
                else if(receivedRequestList.contains(friendData.getId())) {
                    holder.reject.setVisibility(View.VISIBLE);
                    holder.accept.setVisibility(View.VISIBLE);
                }
                else if (!sentRequestList.contains(friendData.getId())) holder.add.setVisibility(View.VISIBLE);
                else holder.waiting.setVisibility(View.VISIBLE);
                break;
            }
            case AppConstants.MODE_FRIEND: {
                holder.reject.setVisibility(View.VISIBLE);
                break;
            }
        }
        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String appUserId = user.getUid();
                String friendUserId = friendData.getId();
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(appUserId)
                        .child("friend_lists")
                        .child("sent_requests")
                        .child(friendUserId);
                DatabaseReference friendReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(friendUserId)
                        .child("friend_lists")
                        .child("pending_requests")
                        .child(appUserId);
                userReference.child("name").setValue(friendData.getName());
                userReference.child("image").setValue(friendData.getImage());
                userReference.child("email").setValue(friendData.getEmail());
                friendReference.child("name").setValue(user.getDisplayName());
                friendReference.child("image").setValue(String.valueOf(user.getPhotoUrl()));
                friendReference.child("email").setValue(user.getEmail());
            }
        });
        holder.waiting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String appUserId = user.getUid();
                String friendUserId = friendData.getId();
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(appUserId)
                        .child("friend_lists")
                        .child("sent_requests")
                        .child(friendUserId);
                DatabaseReference friendReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(friendUserId)
                        .child("friend_lists")
                        .child("pending_requests")
                        .child(appUserId);
                userReference.removeValue();
                friendReference.removeValue();
            }
        });
        holder.reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String appUserId = user.getUid();
                String friendUserId = friendData.getId();
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(appUserId)
                        .child("friend_lists")
                        .child("pending_requests")
                        .child(friendUserId);
                DatabaseReference friendReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(friendUserId)
                        .child("friend_lists")
                        .child("sent_requests")
                        .child(appUserId);
                userReference.removeValue();
                friendReference.removeValue();

                userReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(appUserId)
                        .child("friend_lists")
                        .child("friends")
                        .child(friendUserId);
                friendReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(friendUserId)
                        .child("friend_lists")
                        .child("friends")
                        .child(appUserId);
                userReference.removeValue();
                friendReference.removeValue();
            }
        });
        holder.accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String appUserId = user.getUid();
                String friendUserId = friendData.getId();
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(appUserId)
                        .child("friend_lists")
                        .child("friends")
                        .child(friendUserId);
                DatabaseReference friendReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(friendUserId)
                        .child("friend_lists")
                        .child("friends")
                        .child(appUserId);
                userReference.child("name").setValue(friendData.getName());
                userReference.child("image").setValue(friendData.getImage());
                userReference.child("email").setValue(friendData.getEmail());
                friendReference.child("name").setValue(user.getDisplayName());
                friendReference.child("image").setValue(String.valueOf(user.getPhotoUrl()));
                friendReference.child("email").setValue(user.getEmail());
                userReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(appUserId)
                        .child("friend_lists")
                        .child("pending_requests")
                        .child(friendUserId);
                friendReference = FirebaseDatabase.getInstance().getReference("users")
                        .child(friendUserId)
                        .child("friend_lists")
                        .child("sent_requests")
                        .child(appUserId);
                userReference.removeValue();
                friendReference.removeValue();

            }
        });
    }

    @Override
    public int getItemCount() {
        if(friendDataArrayList==null) return 0;
        return  friendDataArrayList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFriendDataArrayList(ArrayList<FriendData> friendDataArrayList) {
        this.friendDataArrayList = friendDataArrayList;
        notifyDataSetChanged();
    }

    public void setSentRequestList(ArrayList<String> sentRequestList) {
        this.sentRequestList = sentRequestList;
        notifyDataSetChanged();
    }

    public void setReceivedRequestList(ArrayList<String> receivedRequestList) {
        this.receivedRequestList = receivedRequestList;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setFriendsArrayList(ArrayList<String> friendsArrayList) {
        this.friendsArrayList = friendsArrayList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView add, accept, reject, waiting;
        TextView name;
        ImageView image;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            add = itemView.findViewById(R.id.add_friend);
            accept = itemView.findViewById(R.id.accept_friend_request);
            reject = itemView.findViewById(R.id.reject_friend_request);
            waiting = itemView.findViewById(R.id.waiting_for_approval);
            name = itemView.findViewById(R.id.name);
            image = itemView.findViewById(R.id.image);

        }
    }
}