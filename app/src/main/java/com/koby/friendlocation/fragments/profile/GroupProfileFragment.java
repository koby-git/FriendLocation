package com.koby.friendlocation.fragments.profile;

import android.content.Intent;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.koby.friendlocation.R;
import com.koby.friendlocation.activities.main.MainActivity;
import com.koby.friendlocation.fragments.nameDialogFragment.GroupNameFragment;

public class GroupProfileFragment extends BaseProfileFragment {

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
            createGroup();
            startActivity(new Intent(getContext(), MainActivity.class));
            getActivity().finish();
        }
        return true;
    }

    private void createGroup() {
        String groupName = username.getText().toString();
//        Uri imagePathUri = cameraProvider.getImageUri();
//        if (imagePathUri == null) {
//            FirebaseRepository.getInstance().setGroup(groupName);
//        }else {
//            FirebaseRepository.getInstance().setGroup(groupName, imagePathUri);
//        }
    }

    @Override
    protected void loadNameFragment() {
        GroupNameFragment taskBottomSheet = new GroupNameFragment();
        taskBottomSheet.show(getFragmentManager(), "navigation bottomSheet");
    }

    @Override
    public void uploadImage(Uri uri) {
//        FirebaseRepository.getInstance().uploadGroupImage(uri,group.getGroupUid());
        System.out.println("333333333");
    }
}
