package com.koby.friendlocation.activities.auth;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.koby.friendlocation.R;
import com.koby.friendlocation.classes.CameraProvider;
import com.koby.friendlocation.classes.viewmodel.UsernameViewModel;
import com.koby.friendlocation.fragments.UsernameFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;
import static com.koby.friendlocation.classes.CameraProvider.REQUEST_OK;
import static com.koby.friendlocation.classes.CameraProvider.REQUSET_PHOTO_FROM_GALLERY;

public abstract class ProfileBaseFragment extends Fragment {

    @BindView(R.id.profile_imageview)
    public ImageView imageView;

    FirebaseUser firebaseUser;

    @BindView(R.id.profile_name)
    public TextView username;

    private UsernameViewModel usernameViewModel;

    private CameraProvider cameraProvider;

    private Unbinder unbinder;

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

        unbinder = ButterKnife.bind(this,view);
        //Init user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //Init camera provider
        //TODO: Check builder pattern
        cameraProvider = new CameraProvider(getActivity(),imageView);

        setViewModelName(cameraProvider,username);

        //TODO: Check if do image view with view model
        //Observe username changes
        usernameViewModel.getName().observe(requireActivity(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                username.setText(s);
            }
        });

        loadImage(cameraProvider);

    }

    protected abstract void setViewModelName(CameraProvider cameraProvider,TextView username);

    protected abstract void loadImage(CameraProvider cameraProvider);

    @OnClick(R.id.profile_fab)
    public void changeUserImage(){
        if (cameraProvider.checkPermission()) {
            cameraProvider.pickImage();

        }else {
            cameraProvider.requestPermission();
        }
    }

    @OnClick(R.id.profile_grid)
    public void changeUserName(){
        UsernameFragment taskBottomSheet = new UsernameFragment();
        taskBottomSheet.show(getFragmentManager(), "navigation bottomSheet");
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
            Uri uri = cameraProvider.getImageUri(data);
            uploadImage(cameraProvider,uri);
        }
    }

    public abstract void uploadImage(CameraProvider cameraProvider,Uri uri);

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
