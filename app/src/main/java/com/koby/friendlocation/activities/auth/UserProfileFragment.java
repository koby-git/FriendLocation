package com.koby.friendlocation.activities.auth;

import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;;
import com.koby.friendlocation.classes.CameraProvider;

public abstract class UserProfileFragment extends ProfileBaseFragment {

    @Override
    protected void setViewModelName(CameraProvider cameraProvider,TextView username) {
        username.setText(cameraProvider.getUserDisplayName());
    }

    //Load user profile image
    @Override
    protected void loadImage(CameraProvider cameraProvider) {
        cameraProvider.loadUserImage();
    }

    //Upload chosen image
    @Override
    public void uploadImage(CameraProvider cameraProvider,Uri uri) {
        cameraProvider.uploadUserImage(uri);
    }
}


