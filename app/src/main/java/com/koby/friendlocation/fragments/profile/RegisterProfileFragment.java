package com.koby.friendlocation.fragments.profile;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.koby.friendlocation.R;
import com.koby.friendlocation.activities.main.MainActivity;
import com.koby.friendlocation.fragments.nameDialogFragment.UserNameFragment;

public class RegisterProfileFragment extends BaseProfileFragment {

    @Override
    protected void loadNameFragment(){
        UserNameFragment taskBottomSheet = new UserNameFragment();
        taskBottomSheet.show(getFragmentManager(), "username bottomSheet");
    }

    @Override
    public void uploadImage(Uri uri) {
        firebaseRepository.uploadUserImage(uri);
    }


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
