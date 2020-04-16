package com.koby.friendlocation.fragments.nameDialogFragment;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.koby.friendlocation.repository.FirebaseRepository;

public class UserNameFragment extends NameFragment{

    @Override
    protected void setTitle() {
        title.setText("הקלד את שמך");
    }

    @Override
    protected void updateProfile(String name) {
        //Update name in firestore
        firebaseRepository.setUserProfileName(name)
                .addOnTaskCompleteListener(new FirebaseRepository.OnBatchCompleteListener() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                nameViewModel.setName(name);
                dismiss();
            }
        });
    }
}
