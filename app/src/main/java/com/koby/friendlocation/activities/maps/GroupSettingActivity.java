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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.koby.friendlocation.R;
import com.koby.friendlocation.activities.main.MainActivity;
import com.koby.friendlocation.model.Contact;
import com.koby.friendlocation.view.adapter.SettingContactsAdapter;
import com.koby.friendlocation.model.Group;
import com.koby.friendlocation.providers.CameraProvider;
import com.koby.friendlocation.repository.FirebaseRepository;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

import static com.koby.friendlocation.constant.FirebaseConstants.GROUPS;
import static com.koby.friendlocation.providers.CameraProvider.REQUEST_OK;
import static com.koby.friendlocation.providers.CameraProvider.REQUSET_PHOTO_FROM_GALLERY;


public class GroupSettingActivity extends DaggerAppCompatActivity {

    private static final String TAG = GroupSettingActivity.class.getSimpleName();

    @Inject FirebaseRepository firebaseRepository;

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
    String groupUid,groupName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_setting);

        groupUid = getIntent().getStringExtra("groupUid");
        groupName = getIntent().getStringExtra("groupName");

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setTitle(groupName);

        setRecyclerView();
        cameraProvider = new CameraProvider(GroupSettingActivity.this,imageView,firebaseRepository);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_OK && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        } else {
            Toast.makeText(GroupSettingActivity.this, "please provide permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUSET_PHOTO_FROM_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri uri = cameraProvider.getImageUri(data);
            firebaseRepository.uploadGroupImage(uri,group.getUid());
        }
    }

    @OnClick(R.id.group_setting_add_new_member)
    public void addNewMember(){
        String link = "https://www.example.com/?groupUid=" + group.getInviteCode();

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDomainUriPrefix("https://firendlocation.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder()
                                .build())
                .buildShortDynamicLink()
                .addOnSuccessListener(shortDynamicLink -> {

                    String invitationLink = shortDynamicLink.getShortLink().toString();

                    String message = firebaseRepository.getCurrentUser().getDisplayName() + " wants to invite you to Friends location!" +
                            "Let's join Friends location! Here is my group invite code - " + group.getInviteCode() + " Use my referrer link: "
                            + invitationLink;
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, message);

                    startActivity(Intent.createChooser(share, "Share friend location app"));
                }).addOnFailureListener(e -> e.printStackTrace());
    }

    @OnClick(R.id.setting_group_exit)
    public void exitGroup(){
        firebaseRepository.deleteGroup(group);
        Intent intent = new Intent(GroupSettingActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();

    }

    @OnClick(R.id.setting_group_collapsing_toolbar)
    public void changGroupImage(){
        if (cameraProvider.checkPermission()) {
            pickImage();
        }else {
            cameraProvider.requestPermission();
        }
    }

    public void pickImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUSET_PHOTO_FROM_GALLERY);
    }

    private void setRecyclerView() {
        contacts = new ArrayList<>();
        contacts = (ArrayList<Contact>) getIntent().getSerializableExtra("groupContacts");
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new SettingContactsAdapter(contacts,GroupSettingActivity.this);
        recyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(imageUri -> {

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
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        final DocumentReference docRef = firebaseRepository.getGroupDocumentReference(groupUid);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Log.d(TAG, "Current data: " + snapshot.getData());

                group = snapshot.toObject(Group.class);
                    Glide.with(getApplicationContext())
                            .load(group.getImage())
                            .fallback(R.drawable.ic_group_grey)
                            .centerCrop()
                            .into(imageView);

            } else {
                Log.d(TAG, "Current data: null");
            }
        });
    }
}
