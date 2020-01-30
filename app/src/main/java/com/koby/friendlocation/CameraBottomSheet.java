package com.koby.friendlocation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.File;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.koby.friendlocation.classes.FirebaseConstants.GROUPS;

public class CameraBottomSheet extends BottomSheetDialogFragment {

    public static final int REQUSET_PHOTO_FROM_GALLERY = 1;
    private static final int REQUEST_OK = 2;
    private FirebaseFirestore db;
    public FirebaseAuth mAuth;
    public UserProfileChangeRequest profileChangeRequest;
    FirebaseStorage mStorageRef = FirebaseStorage.getInstance();
    Uri downloadUri;
    boolean user;

//    public CameraBottomSheet(boolean user){
//            this.user = user;
//    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottomsheet_camera,container,true);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        ImageButton cameraFab = view.findViewById(R.id.bottomsheet_camera);
        ImageButton galleryFab = view.findViewById(R.id.bottomsheet_gallery);

        cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                dispatchTakePictureIntent();

            }
        });

        galleryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, REQUSET_PHOTO_FROM_GALLERY);
                }else {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_OK);
                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_OK && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, REQUSET_PHOTO_FROM_GALLERY);

        } else {
            Toast.makeText(getContext(), "please provide permission", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode == REQUSET_PHOTO_FROM_GALLERY && resultCode == RESULT_OK && data != null) {
//            // Let's read picked image data - its URI
//            Uri pickedImage = data.getData();
//            // Let's read picked image path using content resolver
//            String[] filePath = {MediaStore.Images.Media.DATA};
//            Cursor cursor = getActivity().getContentResolver().query(pickedImage, filePath, null, null, null);
//            cursor.moveToFirst();
//            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));
//
//            File f = new File(imagePath);
//            Uri contentUri = Uri.fromFile(f);
//
//            final StorageReference ref = mStorageRef.getReference().child("users/" +mAuth.getCurrentUser().getUid()+".jpg");
//            UploadTask uploadTask = ref.putFile(contentUri);
//
//            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
//                @Override
//                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
//                    if (!task.isSuccessful()) {
//                        throw task.getException();
//                    }
//
//                    // Continue with the task to get the download URL
//                    return ref.getDownloadUrl();
//                }
//            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
//                @Override
//                public void onComplete(@NonNull Task<Uri> task) {
//                    if (task.isSuccessful()) {
//                        downloadUri = task.getResult();
//                        if(user) {
//                            updateProfile(downloadUri);
//                        }else {
//                            updateGroup(downloadUri);
//                        }
//                    } else {
//                        // Handle failures
//                        // ...
//                    }
//                }
//            });
//
//
//
////            getActivity().getContentResolver().takePersistableUriPermission(contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            cursor.close();
//        }
//    }

    private void updateGroup(Uri downloadUri) {

//        db.collection(GROUPS).document()
    }


    private void updateProfile(Uri contentUri) {

        profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName("KOBY")
                .setPhotoUri(contentUri).build();


        mAuth.getCurrentUser().updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
//                            Glide.with(getContext()).load(mAuth.getCurrentUser().getPhotoUrl()).into(imageView);

                        }
                    }
                });

    }
}
