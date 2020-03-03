package com.koby.friendlocation.fragments;

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
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koby.friendlocation.R;
import com.koby.friendlocation.classes.UsernameViewModel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.koby.friendlocation.classes.FirebaseConstants.USERS;

public class ProfileFragment extends Fragment {

    public static final int REQUSET_PHOTO_FROM_GALLERY = 1;
    public static final int REQUEST_OK = 2;

    FirebaseAuth mAuth;
    ImageView imageView;
    FirebaseStorage mStorageRef;
    FirebaseFirestore db;
    Uri downloadUri;
    FirebaseUser user;
    GridLayout gridLayout;
    TextView username;
    UsernameViewModel usernameViewModel;
    FloatingActionButton fab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usernameViewModel = ViewModelProviders.of(requireActivity()).get(UsernameViewModel.class); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile,container,false); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Init database
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();

        //Init user
        user = mAuth.getCurrentUser();

        //Init UI element
        gridLayout = view.findViewById(R.id.profile_grid);
        username = view.findViewById(R.id.profile_name);
        imageView = view.findViewById(R.id.profile_imageview);
        fab = view.findViewById(R.id.profile_fab);

        username.setText(user.getDisplayName());

        //Observe username changes
        usernameViewModel.getName().observe(requireActivity(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                username.setText(s);
            }
        });

        //Load user profile image
        if (user.getPhotoUrl()!=null){
                        Glide.with(view).load(user.getPhotoUrl()).into(imageView);
        }

        //Change user profile image
        fab.setOnClickListener(view1 -> {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            }else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_OK);
            }
        });

        //Change user name
        gridLayout.setOnClickListener(view12 -> {
            UsernameFragment taskBottomSheet = new UsernameFragment();
            Bundle args = new Bundle();
            args.putString("username",user.getDisplayName());
            taskBottomSheet.setArguments(args);
            taskBottomSheet.show(getFragmentManager(), "navigation bottomSheet");
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_OK && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        } else {
            Toast.makeText(getContext(), "please provide permission", Toast.LENGTH_SHORT).show();
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
        if(requestCode == REQUSET_PHOTO_FROM_GALLERY && resultCode == RESULT_OK && data != null) {
            getImageUri(data);
        }
    }

    private void getImageUri(Intent data) {
        // Let's read picked image data - its URI
        Uri pickedImage = data.getData();
        // Let's read picked image path using content resolver
        String[] filePath = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().getContentResolver().query(pickedImage, filePath, null, null, null);
        cursor.moveToFirst();
        String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

        File f = new File(imagePath);
        Uri contentUri = Uri.fromFile(f);

        uploadImage(contentUri);

        cursor.close();

    }

    private void uploadImage(Uri contentUri) {

        final StorageReference ref = mStorageRef.getReference().child("users/" +user.getUid()+".jpg");
        UploadTask uploadTask = ref.putFile(contentUri);

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
                    updateProfile(downloadUri);
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    private void updateProfile(Uri contentUri) {

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setPhotoUri(contentUri).build();

        //Update user auth
        user.updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            Glide.with(getContext()).load(user.getPhotoUrl()).into(imageView);

                        }
                    }
                });

        //Update user db
        Map<String,Uri> imageMap = new HashMap<>();
        imageMap.put("imageUri",contentUri);
        db.collection(USERS).document(user.getUid()).set(imageMap, SetOptions.merge());

    }


}
