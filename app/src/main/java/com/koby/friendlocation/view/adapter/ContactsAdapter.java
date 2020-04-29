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
import com.koby.friendlocation.model.Contact;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactHolder> {

    private ArrayList<Contact> contactsList;
    private Context context;

    OnItemClickListener itemClickListener;
    OnImageClickListener imageClickListener;

    public interface OnImageClickListener{ void onImageClick(String imageUri);}
    public interface OnItemClickListener{ void onItemClick(Contact contact);}

    public void setImageClickListener(OnImageClickListener imageClickListener){
        this.imageClickListener = imageClickListener;
    }
    public void setOnItemClickListener(OnItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }


    public ContactsAdapter(ArrayList<Contact> contacts, Context context) {
        this.contactsList = contacts;
        this.context = context;
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_contact, viewGroup, false);

        return new ContactHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactHolder contactHolder, int i) {

        if (contactsList.get(i).getImageUri() != null) {
            Glide.with(context)
                    .load(Uri.parse(contactsList.get(i).getImageUri()))
                    .circleCrop()
                    .into(contactHolder.contactImage);
        }
        if (contactsList.get(i).getAddress()==null) {
            contactHolder.contactName.setText(contactsList.get(i).getName());
            contactHolder.contactStreet.setText("מעדכן...");
        } else {
            contactHolder.contactName.setText(contactsList.get(i).getName());
            contactHolder.contactStreet.setText(contactsList.get(i).getAddress());
            contactHolder.contactDate.setText(contactsList.get(i).getDate());
        }
    }

    @Override
    public int getItemCount() {
        return contactsList.size();
    }


    public class ContactHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.contact_adapter_name)
        TextView contactName;
        @BindView(R.id.contact_adapter_street)
        TextView contactStreet;
        @BindView(R.id.contact_adapter_date)
        TextView contactDate;

        @BindView(R.id.contact_adapter_image)
        ImageView contactImage;

        public ContactHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this,itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                        itemClickListener.onItemClick(contactsList.get(position));
                    }
                }
            });

            contactImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && imageClickListener != null) {
                        imageClickListener.onImageClick(contactsList.get(position).getImageUri());
                    }
                }
            });

        }
    }
}
