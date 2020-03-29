package com.koby.friendlocation.fragments.profile;

import android.net.Uri;
import android.widget.TextView;

import com.koby.friendlocation.fragments.nameDialogFragment.UserNameFragment;
import com.koby.friendlocation.repository.FirebaseRepository;

public class UserProfileFragment extends BaseProfileFragment {

    @Override
    protected void loadNameFragment() {
        UserNameFragment taskBottomSheet = new UserNameFragment();
        taskBottomSheet.show(getFragmentManager(), "navigation bottomSheet");
    }

    //Set current name
    public void setViewModelName(TextView username) {
        username.setText(firebaseUser.getDisplayName());
    }

    //Load current user profile image
    public void loadImage() {
        cameraProvider.loadUserImage();
    }

    //Upload chosen image
    @Override
    protected void uploadImage(Uri uri) {
        System.out.println("2222222222");
        FirebaseRepository.getInstance().uploadUserImage(uri);
    }


    @Override
    public void onStart() {
        super.onStart();
        setViewModelName(username);
        loadImage();
    }
}


