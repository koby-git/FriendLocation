package com.koby.friendlocation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class ProfileActivity extends AppCompatActivity {

    private static final int REQUSET_PHOTO_FROM_GALLERY = 1;
    private static final int REQUEST_OK = 2;


    FirebaseAuth mAuth;
    EditText usernameEditText;
    ImageView imageView;
    FirebaseStorage mStorageRef = FirebaseStorage.getInstance();
    Uri downloadUri;
    FirebaseUser user;
    boolean groupSetting = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);




        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
//        usernameEditText = view.findViewById(R.id.profile_username);

//        usernameEditText.setText(mAuth.getCurrentUser().getDisplayName());


        imageView = findViewById(R.id.profile_imageview);
        FloatingActionButton fab = findViewById(R.id.profile_fab);

        if (user.getPhotoUrl()!=null){
//            Glide.with(view).load(user.getPhotoUrl()).circleCrop().into(imageView);
            Glide.with(this).load(user.getPhotoUrl()).into(imageView);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    pickImage();
                }else {
                    ActivityCompat.requestPermissions(ProfileActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_OK);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_OK && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        } else {
            Toast.makeText(ProfileActivity.this, "please provide permission", Toast.LENGTH_SHORT).show();
        }
    }

    private void pickImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUSET_PHOTO_FROM_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("request:" + REQUSET_PHOTO_FROM_GALLERY + " == " + requestCode);
        System.out.println("result: " + RESULT_OK + " == " + resultCode);

        if(data!=null){
            System.out.println("hara");
        }

        if(requestCode == REQUSET_PHOTO_FROM_GALLERY && resultCode == RESULT_OK && data != null) {
            System.out.println("nnnnn");
            getImageUri(data);
        }
    }

    private void getImageUri(Intent data) {
        // Let's read picked image data - its URI
        Uri pickedImage = data.getData();
        // Let's read picked image path using content resolver
        String[] filePath = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
        cursor.moveToFirst();
        String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);

        final StorageReference ref = mStorageRef.getReference().child("users/" +mAuth.getCurrentUser().getUid()+".jpg");
        UploadTask uploadTask = ref.putFile(contentUri);

        System.out.println("ccccc");
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUri = task.getResult();
                    if(groupSetting) {
//                        updateGroup(downloadUri);
                    }else {
                        System.out.println("qqqq");
                        updateProfile(downloadUri);
                    }
                } else {
                    // Handle failures
                    // ...
                }
            }
        });

//            getActivity().getContentResolver().takePersistableUriPermission(contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cursor.close();

    }

    private void updateProfile(Uri contentUri) {

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setPhotoUri(contentUri).build();


        System.out.println("aaaaaaa");
        mAuth.getCurrentUser().updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            Glide.with(ProfileActivity.this).load(mAuth.getCurrentUser().getPhotoUrl()).into(imageView);

                        }
                    }
                });

    }
}
