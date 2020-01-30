package com.koby.friendlocation.classes;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.koby.friendlocation.R;

import java.util.ArrayList;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactHolder> {

    private ContactsAdapter.OnItemClickListener listener;
    private OnDeleteClickListener deleteClickListener;
    private ArrayList<Contact> contacts;
//    private ArrayList<Contact> selectedItems;
    private ActionMode actionMode;
    Context context;
    private Button bottom_toolbar;

    public interface OnItemClickListener {
        void onItemClick(Contact contact);
    }

    public void setOnItemClickListener(ContactsAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(ArrayList<Contact> selectedContact);
    }

    public void setOnDelteClickListener(ContactsAdapter.OnDeleteClickListener deleteClickListener) {
        this.deleteClickListener = deleteClickListener;
    }



    public ContactsAdapter(ArrayList<Contact> contacts, Context context) {
        this.contacts = contacts;
        this.context = context;
//        selectedItems = new ArrayList<>();
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

        if (contacts.get(i).getImageUri()!=null){
            Glide.with(context).load(Uri.parse(contacts.get(i).getImageUri())).circleCrop().into(contactHolder.contactImage);
        }
        if (contacts.get(i).getState().equals("STILL")) {
            contactHolder.contactName.setText(contacts.get(i).getName());
            contactHolder.contactStreet.setText(contacts.get(i).getAddress());
            contactHolder.contactDate.setText(contacts.get(i).getDate());
        }else {
            contactHolder.contactName.setText(contacts.get(i).getName());
            contactHolder.contactStreet.setText(contacts.get(i).getState());
            contactHolder.contactDate.setText("");
        }




    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }


    public class ContactHolder extends RecyclerView.ViewHolder {

        TextView contactName,contactStreet,contactDate;
        CardView linearLayout;
        ImageView contactImage;

        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            contactName = itemView.findViewById(R.id.contact_name);
            contactStreet = itemView.findViewById(R.id.contact_street);
            contactDate = itemView.findViewById(R.id.contact_date);
            contactImage = itemView.findViewById(R.id.adapter_contact_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(contacts.get(position));
                    }
                }
            });

        }
    }
}
