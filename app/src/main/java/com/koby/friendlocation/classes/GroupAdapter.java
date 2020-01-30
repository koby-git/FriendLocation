package com.koby.friendlocation.classes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.koby.friendlocation.R;

import java.util.ArrayList;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupHolder>{

    onItemClickListener listener;

    public interface onItemClickListener{
        void onItemClick(Group group);
    }

    public void setOnItemClickListener(onItemClickListener listener){
        this.listener = listener;
    }

    public ArrayList<Group> groupList;

    public GroupAdapter(ArrayList<Group> groupList) {
        this.groupList = groupList;
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
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public class GroupHolder extends RecyclerView.ViewHolder{

        public TextView groupName;


        public GroupHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.adapter_group_name);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(groupList.get(position));
                    }
                }
            });
        }
    }
}
