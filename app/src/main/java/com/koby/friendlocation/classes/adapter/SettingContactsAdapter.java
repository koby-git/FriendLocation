package com.koby.friendlocation.classes.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.koby.friendlocation.R;
import com.koby.friendlocation.classes.model.Contact;

import java.util.ArrayList;

public class SettingContactsAdapter extends RecyclerView.Adapter<SettingContactsAdapter.ContactsHolder> {

    ArrayList<Contact> contacts;
    Context context;

    public SettingContactsAdapter(ArrayList<Contact> contacts, Context context) {
        this.contacts = contacts;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_group_setting,parent,false);
        return new ContactsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsHolder holder, int position) {
        holder.contactName.setText(contacts.get(position).getName());
        if (contacts.get(position).getImageUri()!=null){
            Glide.with(context).load(contacts.get(position).getImageUri()).circleCrop().into(holder.contactImage);
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public class ContactsHolder extends RecyclerView.ViewHolder{

        TextView contactName;
        ImageView contactImage;

        public ContactsHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contact_name);
            contactImage = itemView.findViewById(R.id.adapter_contact_image);
        }
    }
}
