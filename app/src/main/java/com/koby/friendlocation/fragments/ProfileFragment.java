package com.koby.friendlocation.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.koby.friendlocation.CameraProvider;
import com.koby.friendlocation.R;
import com.koby.friendlocation.classes.viewmodel.UsernameViewModel;

import static android.app.Activity.RESULT_OK;
import static com.koby.friendlocation.CameraProvider.REQUEST_OK;
import static com.koby.friendlocation.CameraProvider.REQUSET_PHOTO_FROM_GALLERY;

public class ProfileFragment extends Fragment {

    ImageView imageView;
    FirebaseUser firebaseUser;
    GridLayout gridLayout;
    TextView username;
    UsernameViewModel usernameViewModel;
    FloatingActionButton fab;
    CameraProvider cameraProvider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        usernameViewModel = ViewModelProviders.of(requireActivity()).get(UsernameViewModel.class); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile,container,false); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Init user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //UI element
        gridLayout = view.findViewById(R.id.profile_grid);
        username = view.findViewById(R.id.profile_name);
        imageView = view.findViewById(R.id.profile_imageview);
        fab = view.findViewById(R.id.profile_fab);

        //Init camera provider
        //TODO: Check builder pattern
        cameraProvider = new CameraProvider(getActivity());
        cameraProvider.setImageView(imageView);

        username.setText(firebaseUser.getDisplayName());

        //TODO: Check if do image view with view model
        //Observe username changes
        usernameViewModel.getName().observe(requireActivity(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                username.setText(s);
            }
        });

        //Load user profile image
        if (firebaseUser.getPhotoUrl()!=null){
            Glide.with(view).load(firebaseUser.getPhotoUrl()).into(imageView);
        }

        //Change user profile image
        fab.setOnClickListener(view1 -> {
            if (cameraProvider.checkPermission()) {
                cameraProvider.pickImage();
            }else {
                cameraProvider.requestPermission();
                }
        });

        //Change user name
        gridLayout.setOnClickListener(view12 -> {
            UsernameFragment taskBottomSheet = new UsernameFragment();
            Bundle args = new Bundle();
            args.putString("username",firebaseUser.getDisplayName());
            taskBottomSheet.setArguments(args);
            taskBottomSheet.show(getFragmentManager(), "navigation bottomSheet");
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_OK && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            cameraProvider.pickImage();
        } else {
            Toast.makeText(getContext(), "please provide permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUSET_PHOTO_FROM_GALLERY && resultCode == RESULT_OK && data != null) {
            cameraProvider.getImageUri(data);
        }
    }
}


