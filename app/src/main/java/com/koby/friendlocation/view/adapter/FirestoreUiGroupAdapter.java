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

    OnItemClickListener itemClickListener;
    OnImageClickListener imageClickListener;

    public interface OnImageClickListener{ void onImageClick(String imageUri);}
    public interface OnItemClickListener{ void onItemClick(Group group);}

    public void setImageClickListener(OnImageClickListener imageClickListener){
        this.imageClickListener = imageClickListener;
    }
    public void setOnItemClickListener(OnItemClickListener itemClickListener){
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
        holder.groupName.setText(model.getName());
        if(model.getImage()!=null){
            Glide.with(context)
                    .load(Uri.parse(model.getImage()))
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
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    itemClickListener.onItemClick(getSnapshots().get(position));
                }
            });

            groupImageView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && imageClickListener != null) {
                    imageClickListener.onImageClick(getSnapshots().get(position).getImage());
                }
            });
        }
    }

}
