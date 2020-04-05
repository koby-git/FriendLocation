package com.koby.friendlocation.fragments.profile;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.koby.friendlocation.R;
import com.koby.friendlocation.activities.main.MainActivity;
import com.koby.friendlocation.fragments.nameDialogFragment.GroupNameFragment;
import com.koby.friendlocation.repository.FirebaseRepository;

public class GroupProfileFragment extends BaseProfileFragment {

    public static final String TAG = GroupProfileFragment.class.getSimpleName();

    Uri imagePathUri = null;

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
        }
        return true;
    }

    private void createGroup() {
        String groupName = username.getText().toString();

        if (groupName.isEmpty()){
            username.setError("נא למלא שם קבוצה");
            return;
        }

        firebaseRepository
                .setGroup(groupName, imagePathUri).addOnTaskCompleteListener(new FirebaseRepository.OnBatchCompleteListener() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful()) {
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    getActivity().finish();
                } else {
                    Log.i(TAG, task.getException().toString());
                    Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    protected void loadNameFragment() {
        GroupNameFragment taskBottomSheet = new GroupNameFragment();
        taskBottomSheet.show(getFragmentManager(), "navigation bottomSheet");
    }

    @Override
    public void uploadImage(Uri uri) {
        imagePathUri = uri;
        Glide.with(getContext()).load(uri).centerCrop().into(imageView);
    }

}
