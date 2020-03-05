package com.koby.friendlocation.classes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.koby.friendlocation.R;
import com.koby.friendlocation.classes.model.Contact;

public class FirestoreUiContactAdapter extends FirestoreRecyclerAdapter<Contact, FirestoreUiContactAdapter.ContactHolder> {

    public FirestoreUiContactAdapter(@NonNull FirestoreRecyclerOptions<Contact> options) {
        super(options);
    }

    @NonNull
    @Override
    public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_contact, parent, false);

        return new ContactHolder(view);
    }


    @Override
    protected void onBindViewHolder(@NonNull ContactHolder holder, int position, @NonNull Contact model) {
        if (model.getImageUri()!=null){
//            Glide.with(context).load(Uri.parse(contacts.get(i).getImageUri())).circleCrop().into(contactHolder.contactImage);
        }
        holder.contactName.setText(model.getName());
        holder.contactStreet.setText(model.getAddress());
        holder.contactDate.setText(model.getDate());
    }



    public class ContactHolder extends RecyclerView.ViewHolder {

        TextView contactName, contactStreet, contactDate;
        ImageView contactImage;

        public ContactHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contact_name);
            contactStreet = itemView.findViewById(R.id.contact_street);
            contactDate = itemView.findViewById(R.id.contact_date);
            contactImage = itemView.findViewById(R.id.adapter_contact_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
//                    if (position != RecyclerView.NO_POSITION && listener != null) {
//                        listener.onItemClick(getSnapshots().get(position));
//                    }
                }
            });
        }


    }
}
