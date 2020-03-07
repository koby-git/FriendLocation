package com.koby.friendlocation.activities.auth;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.koby.friendlocation.R;
import com.koby.friendlocation.activities.main.MainActivity;
import com.koby.friendlocation.classes.CameraProvider;

public class RegisterProfileFragment extends UserProfileFragment {

    //inflate menu
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.group_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //handle menu
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_done){
            startActivity(new Intent(getContext(), MainActivity.class));
            getActivity().finish();
        }
        return true;
    }
}
