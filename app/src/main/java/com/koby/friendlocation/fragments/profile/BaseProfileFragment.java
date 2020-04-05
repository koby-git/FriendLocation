package com.koby.friendlocation.fragments.profile;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.koby.friendlocation.R;
import com.koby.friendlocation.providers.CameraProvider;
import com.koby.friendlocation.repository.FirebaseRepository;
import com.koby.friendlocation.viewmodel.NameViewModel;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.DaggerFragment;

import static android.app.Activity.RESULT_OK;
import static com.koby.friendlocation.providers.CameraProvider.REQUEST_OK;
import static com.koby.friendlocation.providers.CameraProvider.REQUSET_PHOTO_FROM_GALLERY;

public abstract class BaseProfileFragment extends DaggerFragment{

    @Inject
    @Nullable
    public FirebaseUser firebaseUser;

    @Inject
    public FirebaseRepository firebaseRepository;

    @BindView(R.id.profile_name) public TextView username;
    @BindView(R.id.profile_imageview) public ImageView imageView;

    public CameraProvider cameraProvider;
    private NameViewModel usernameViewModel;
    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        usernameViewModel = ViewModelProviders.of(requireActivity()).get(NameViewModel.class); }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile,container,false); }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        unbinder = ButterKnife.bind(this,view);

        //Init user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //Init camera provider
        cameraProvider = new CameraProvider(getActivity(),imageView,firebaseRepository);

        //Observe username changes
        usernameViewModel.getName().observe(requireActivity(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                username.setText(s);
            }
        });
    }

    public void pickImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, REQUSET_PHOTO_FROM_GALLERY);
    }

    //Image fab
    @OnClick(R.id.profile_fab)
    public void changeImage(){
        if (cameraProvider.checkPermission()) {
            pickImage();
        }else {
            cameraProvider.requestPermission();
        }
    }

    @OnClick(R.id.profile_grid)
    public void changeUserName(){
        loadNameFragment();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUSET_PHOTO_FROM_GALLERY && resultCode == RESULT_OK && data != null) {
            Uri imageUri = cameraProvider.getImageUri(data);
            uploadImage(imageUri);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    protected abstract void uploadImage(Uri uri);
    protected abstract void loadNameFragment();

}


