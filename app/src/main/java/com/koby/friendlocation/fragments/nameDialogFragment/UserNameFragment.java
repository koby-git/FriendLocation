package com.koby.friendlocation.fragments.nameDialogFragment;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UserNameFragment extends NameFragment {

    private static final String TAG = UserNameFragment.class.getSimpleName();

    @Override
    public void setCurrentName() {
        usernameEditText.setText(mAuth.getCurrentUser().getDisplayName());
    }

    @Override
    protected void updateProfile(String name) {
        //Update name in firestore
        firebaseRepository.setUserProfileName(name);

        //Update name in firebaseAuth
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(name).build();

        mAuth.getCurrentUser().updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            Toast.makeText(getContext(), "User profile updated", Toast.LENGTH_SHORT).show();
                            nameViewModel.setName(name);
                            dismiss();
                        }
                    }
                });
    }
}
