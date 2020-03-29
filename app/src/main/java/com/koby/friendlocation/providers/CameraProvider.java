package com.koby.friendlocation.providers;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.koby.friendlocation.repository.FirebaseRepository;

import java.io.File;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class CameraProvider {

    public static final int REQUSET_PHOTO_FROM_GALLERY = 1;
    public static final int REQUEST_OK = 2;

    private Activity activity;
    private FirebaseUser firebaseUser;
    private FirebaseRepository firebaseRepository;
    private FirebaseStorage mStorageRef;
    public ImageView imageView;
    public Uri imageUri;

    public CameraProvider(Activity context,ImageView imageView) {
        this.activity = context;
        this.imageView = imageView;
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance();
        firebaseRepository = FirebaseRepository.getInstance();
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

    public Uri getImageUri(Intent data) {
        // Let's read picked image data - its URI
        Uri pickedImage = data.getData();
        // Let's read picked image path using content resolver
        String[] filePath = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(pickedImage, filePath, null, null, null);
        cursor.moveToFirst();
        String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);

        cursor.close();
        return contentUri;
    }


//    private void updateGroupProfile(Uri result) {
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        WriteBatch batch = db.batch();
//
//        String groupInviteCode = UUID.randomUUID().toString().substring(0, 6);
//        String groupUid = UUID.randomUUID().toString();
//        Group group = new Group("", groupUid, groupInviteCode);
//        group.addUser(firebaseUser.getUid());
//
//        DocumentReference groupRef = db.collection(GROUPS).document(groupUid);
//        DocumentReference userRef = db.collection(USERS).document(firebaseUser.getUid());
//
//        batch.set(groupRef, group);
//        batch.update(userRef, "groupsUid", FieldValue.arrayUnion(groupUid));
//
//        batch.commit();
//
//    }

    //Update profile image
    private void updateUserProfile(Uri contentUri) {

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

        firebaseRepository.setUserProfileImage(contentUri);

    }

    public void loadGroupImage() {}

    public void loadUserImage() {
        if (firebaseUser.getPhotoUrl()!=null){
            Glide.with(activity).load(firebaseUser.getPhotoUrl()).into(imageView);
        }
    }


}
