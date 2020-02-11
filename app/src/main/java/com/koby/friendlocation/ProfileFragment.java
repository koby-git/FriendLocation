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
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koby.friendlocation.classes.UsernameViewModel;

import java.io.File;

import static android.app.Activity.RESULT_OK;
import static androidx.constraintlayout.widget.Constraints.TAG;

public class ProfileFragment extends Fragment {

    private static final int REQUSET_PHOTO_FROM_GALLERY = 1;
    private static final int REQUEST_OK = 2;

    FirebaseAuth mAuth;
    EditText usernameEditText;
    ImageView imageView;
    FirebaseStorage mStorageRef = FirebaseStorage.getInstance();
    Uri downloadUri;
    FirebaseUser user;
    boolean groupSetting = false;
    GridLayout gridLayout;
    TextView username;
    UsernameViewModel usernameViewModel;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usernameViewModel = ViewModelProviders.of(requireActivity()).get(UsernameViewModel.class);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        gridLayout = view.findViewById(R.id.profile_grid);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
//        usernameEditText = view.findViewById(R.id.profile_username);
        username = view.findViewById(R.id.profile_name);
//        username.setText(mAuth.getCurrentUser().getDisplayName());
//        usernameEditText.setText(mAuth.getCurrentUser().getDisplayName());


        imageView = view.findViewById(R.id.profile_imageview);
        FloatingActionButton fab = view.findViewById(R.id.profile_fab);

        usernameViewModel.getName().observe(requireActivity(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                username.setText(s);
            }
        });

        if (user.getPhotoUrl()!=null){
//            Glide.with(view).load(user.getPhotoUrl()).circleCrop().into(imageView);
                        Glide.with(view).load(user.getPhotoUrl()).into(imageView);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    pickImage();
                }else {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_OK);
                }
            }
        });

        gridLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeUsername taskBottomSheet = new ChangeUsername();
                Bundle args = new Bundle();
                args.putString("username",mAuth.getCurrentUser().getDisplayName());
                taskBottomSheet.setArguments(args);
                taskBottomSheet.show(getFragmentManager(), "navigation bottomSheet");
            }
        });

//        imageView.setOnClickListener(v -> {
//            CameraBottomSheet cameraBottomsheet = new CameraBottomSheet();
////            args = new Bundle();
////            args.putSerializable("files", filesName);
////            args.putString(TASK_UID, task.getTaskUid());
////            cameraBottomsheet.setArguments(args);
//            cameraBottomsheet.show(getActivity().getSupportFragmentManager(), "bottomSheet");
//        });
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

        final StorageReference ref = mStorageRef.getReference().child("users/" +mAuth.getCurrentUser().getUid()+".jpg");
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
                    if(groupSetting) {
//                        updateGroup(downloadUri);
                    }else {
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
                            Glide.with(getContext()).load(mAuth.getCurrentUser().getPhotoUrl()).into(imageView);

                        }
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
    }




}
