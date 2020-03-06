package com.koby.friendlocation.classes;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koby.friendlocation.repository.FirebaseRepository;

import java.io.File;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.koby.friendlocation.classes.constant.FirebaseConstants.USERS;

public class CameraProvider {

    public static final int REQUSET_PHOTO_FROM_GALLERY = 1;
    public static final int REQUEST_OK = 2;

    private Activity activity;
    private FirebaseUser firebaseUser;
    private FirebaseRepository firebaseRepository;
    private FirebaseStorage mStorageRef;
    public ImageView imageView;

    public CameraProvider(Activity context) {
        this.activity = context;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance();
//        firebaseRepository = FirebaseRepository.getInstance();
    }

    public void setImageView(ImageView imageView){
            this.imageView = imageView;
    }

    public boolean checkPermission(){
       return ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(){
        ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_OK);

    }

    public void pickImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        activity.startActivityForResult(photoPickerIntent, REQUSET_PHOTO_FROM_GALLERY);
    }

    public void getImageUri(Intent data) {
        // Let's read picked image data - its URI
        Uri pickedImage = data.getData();
        // Let's read picked image path using content resolver
        String[] filePath = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(pickedImage, filePath, null, null, null);
        cursor.moveToFirst();
        String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);

        uploadImage(contentUri);

        cursor.close();

    }



    private void uploadImage(Uri contentUri) {

        final StorageReference ref = mStorageRef.getReference().child(USERS + "/" +firebaseUser.getUid()+".jpg");
        UploadTask uploadTask = ref.putFile(contentUri);

        //Get firebase storage image uri
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
                    Uri downloadUri;
                    downloadUri = task.getResult();
                    updateProfile(downloadUri);
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    //Update profile image
    private void updateProfile(Uri contentUri) {

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setPhotoUri(contentUri).build();

        //Update user auth
        firebaseUser.updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            try {
                                Glide.with(activity).load(firebaseUser.getPhotoUrl()).into(imageView);
                            }catch (NullPointerException e){
                                e.printStackTrace();
                            }
                        }
                    }
                });

        firebaseRepository.setProfileImage(contentUri);

    }

}
