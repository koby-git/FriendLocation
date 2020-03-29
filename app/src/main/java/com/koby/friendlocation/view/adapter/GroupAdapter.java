package com.koby.friendlocation.view.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.koby.friendlocation.R;
import com.koby.friendlocation.model.Group;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupHolder>{

    onItemClickListener itemClickListener;

    onImageClickListener imageClickListener;

    public interface onImageClickListener{
        void onImageClick(String imageUri);
    }

    public interface onItemClickListener{
        void onItemClick(Group group);
    }

    public void setImageClickListener(onImageClickListener imageClickListener){
        this.imageClickListener = imageClickListener;
    }

    public void setOnItemClickListener(onItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    public ArrayList<Group> groupList;
    private Context context;

    public GroupAdapter(ArrayList<Group> groupList, Context context) {
        this.groupList = groupList;
        this.context = context;
    }

    @NonNull
    @Override
    public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_group, parent, false);
        return new GroupHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupHolder holder, int position) {
        holder.groupName.setText(groupList.get(position).getGroupName());
        if(groupList.get(position).getGroupImage()!=null){
            Glide.with(context)
                    .load(Uri.parse(groupList.get(position).getGroupImage()))
                    .into(holder.groupImageView);
        }
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public class GroupHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.adapter_group_name) TextView groupName;
        @BindView(R.id.group_adapter_image) ImageView groupImageView;

        public GroupHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onItemClick(groupList.get(position));
                    }
                }
            });

            groupImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && imageClickListener != null) {
                        imageClickListener.onImageClick(groupList.get(position).getGroupImage());
                    }
                }
            });
        }
    }
}