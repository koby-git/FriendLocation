package com.koby.friendlocation.activities.auth;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.koby.friendlocation.R;
import com.koby.friendlocation.activities.main.MainActivity;
import com.koby.friendlocation.fragments.ProfileFragment;

public class RegisterProfileFragment extends ProfileFragment {

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.group_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_done){
            startActivity(new Intent(getContext(), MainActivity.class));
            getActivity().finish();
        }
        return true;
    }
}
