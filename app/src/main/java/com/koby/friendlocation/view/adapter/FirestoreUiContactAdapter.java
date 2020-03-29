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
import com.koby.friendlocation.model.Contact;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FirestoreUiContactAdapter extends FirestoreRecyclerAdapter<Contact, FirestoreUiContactAdapter.ContactHolder> {

    onItemClickListener listener;
    Context context;

    public interface onItemClickListener{
        void onItemClick(Contact group);
    }

    public void setOnItemClickListener(onItemClickListener listener){
        this.listener = listener;
    }
    public FirestoreUiContactAdapter(@NonNull FirestoreRecyclerOptions<Contact> options, Context context) {
        super(options);
        this.context = context;

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
            Glide.with(context).load(Uri.parse(model.getImageUri())).circleCrop().into(holder.contactImage);
        }
        holder.contactName.setText(model.getName());
        holder.contactStreet.setText(model.getAddress());
        holder.contactDate.setText(model.getDate());
    }

    public class ContactHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.contact_adapter_name) TextView contactName;
        @BindView(R.id.contact_adapter_street) TextView contactStreet ;
        @BindView(R.id.contact_adapter_date) TextView contactDate;

        @BindView(R.id.contact_adapter_image) ImageView contactImage;

        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
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
