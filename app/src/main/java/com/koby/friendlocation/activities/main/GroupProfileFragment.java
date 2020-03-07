package com.koby.friendlocation.activities.main;

import android.net.Uri;
import android.widget.TextView;

import com.koby.friendlocation.activities.auth.ProfileBaseFragment;
import com.koby.friendlocation.classes.CameraProvider;

public class GroupProfileFragment extends ProfileBaseFragment {

    @Override
    protected void setViewModelName(CameraProvider cameraProvider,TextView username) {
        username.setText(cameraProvider.getGroupDisplayName());
    }

    @Override
    protected void loadImage(CameraProvider cameraProvider) {
        cameraProvider.loadGroupImage();
    }

    @Override
    public void uploadImage(CameraProvider cameraProvider, Uri uri) {
        cameraProvider.uploadGroupImage(uri);
    }
}
