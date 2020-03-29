package com.koby.friendlocation.activities.maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.koby.friendlocation.R;
import com.koby.friendlocation.model.Contact;
import com.koby.friendlocation.view.adapter.SettingContactsAdapter;
import com.koby.friendlocation.model.Group;
import com.koby.friendlocation.providers.CameraProvider;
import com.koby.friendlocation.repository.FirebaseRepository;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.koby.friendlocation.constant.FirebaseConstants.GROUPS;
import static com.koby.friendlocation.providers.CameraProvider.REQUEST_OK;
import static com.koby.friendlocation.providers.CameraProvider.REQUSET_PHOTO_FROM_GALLERY;


public class GroupSettingActivity extends AppCompatActivity {

    private static final String TAG = GroupSettingActivity.class.getSimpleName();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    //Ui element
    @BindView(R.id.setting_group_toolbar)Toolbar toolbar;
    @BindView(R.id.settting_group_recyclerview) RecyclerView recyclerView;
    @BindView(R.id.setting_group_image) ImageView imageView;

    //private vars
    private SettingContactsAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Contact> contacts;
    private CameraProvider cameraProvider;

    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_setting);

        group = (Group) getIntent().getSerializableExtra("group");

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(group.getGroupName());

        setRecyclerView();
        cameraProvider = new CameraProvider(GroupSettingActivity.this,imageView);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_OK && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraProvider.pickImage();
        } else {
            Toast.makeText(GroupSettingActivity.this, "please provide permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUSET_PHOTO_FROM_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri uri = cameraProvider.getImageUri(data);
            FirebaseRepository.getInstance().uploadGroupImage(uri,group.getGroupUid());
        }
    }

    @OnClick(R.id.setting_group_collapsing_toolbar)
    public void changGroupImage(){
        if (cameraProvider.checkPermission()) {
            cameraProvider.pickImage();
        }else {
            cameraProvider.requestPermission();
        }
    }

    private void setRecyclerView() {
        contacts = new ArrayList<>();
        contacts = (ArrayList<Contact>) getIntent().getSerializableExtra("groupContacts");
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new SettingContactsAdapter(contacts,GroupSettingActivity.this);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new SettingContactsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String imageUri) {

                LayoutInflater inflater = LayoutInflater.from(GroupSettingActivity.this);
                View view =inflater.inflate(R.layout.dialog_image, null);
                ImageView imageView = view.findViewById(R.id.dialog_image);
                Glide.with(GroupSettingActivity.this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_delete_forever)
                        .centerCrop()
                        .into(imageView);

                AlertDialog.Builder builder = new AlertDialog.Builder(GroupSettingActivity.this);
                builder.setView(view);
                        builder.show();




            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        final DocumentReference docRef = db.collection(GROUPS).document(group.getGroupUid());
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, "Current data: " + snapshot.getData());

                    group = snapshot.toObject(Group.class);

                    if(group.getGroupImage()!=null) {
                        Glide.with(GroupSettingActivity.this).load(Uri.parse(group.getGroupImage())).into(imageView);
                    }

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });
    }
}
