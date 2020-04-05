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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.koby.friendlocation.R;
import com.koby.friendlocation.model.Group;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FirestoreUiGroupAdapter extends FirestoreRecyclerAdapter<Group, FirestoreUiGroupAdapter.GroupHolder> {

    Context context;

    GroupAdapter.onItemClickListener itemClickListener;
    GroupAdapter.onImageClickListener imageClickListener;

    public interface onImageClickListener{ void onImageClick(String imageUri);}
    public interface onItemClickListener{ void onItemClick(Group group);}

    public void setImageClickListener(GroupAdapter.onImageClickListener imageClickListener){
        this.imageClickListener = imageClickListener;
    }
    public void setOnItemClickListener(GroupAdapter.onItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }


    public FirestoreUiGroupAdapter(@NonNull FirestoreRecyclerOptions<Group> options, Context context) {
        super(options);
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
    protected void onBindViewHolder(@NonNull GroupHolder holder, int position, @NonNull Group model) {
        holder.groupName.setText(model.getGroupName());
        if(model.getGroupImage()!=null){
            Glide.with(context)
                    .load(Uri.parse(model.getGroupImage()))
                    .circleCrop()

                    .into(holder.groupImageView);
        }
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
                        itemClickListener.onItemClick(getSnapshots().get(position));
                    }
                }
            });

            groupImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && imageClickListener != null) {
                        imageClickListener.onImageClick(getSnapshots().get(position).getGroupImage());
                    }
                }
            });
        }

    }
}
