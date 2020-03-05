package com.koby.friendlocation.classes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.koby.friendlocation.R;
import com.koby.friendlocation.classes.model.Group;

public class FirestoreUiGroupAdapter extends FirestoreRecyclerAdapter<Group, FirestoreUiGroupAdapter.GroupHolder> {

    GroupAdapter.onItemClickListener listener;

    public interface onItemClickListener{
        void onItemClick(Group group);
    }

    public void setOnItemClickListener(GroupAdapter.onItemClickListener listener){
        this.listener = listener;
    }


    public FirestoreUiGroupAdapter(@NonNull FirestoreRecyclerOptions<Group> options) {
        super(options);
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
    }



    public class GroupHolder extends RecyclerView.ViewHolder{

        public TextView groupName;

        public GroupHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.adapter_group_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().get(position));
                    }
                }
            });
        }

    }
}
