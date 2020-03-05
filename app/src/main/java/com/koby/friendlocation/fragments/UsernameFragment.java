package com.koby.friendlocation.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.koby.friendlocation.FirebaseRepository;
import com.koby.friendlocation.R;
import com.koby.friendlocation.classes.viewmodel.UsernameViewModel;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class UsernameFragment extends BottomSheetDialogFragment {

    FirebaseAuth mAuth;
    FirebaseRepository firebaseRepository;
    UsernameViewModel usernameViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);
        usernameViewModel = ViewModelProviders.of(requireActivity()).get(UsernameViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_username, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mAuth = FirebaseAuth.getInstance();
        firebaseRepository = FirebaseRepository.getInstance();

        String currUsername = mAuth.getCurrentUser().getDisplayName();
        EditText usernameEditText = view.findViewById(R.id.username_et);
        Button confirmBtn = view.findViewById(R.id.confirm_btn);
        Button cancel = view.findViewById(R.id.cancel_btn);
        usernameEditText.requestFocus();
        usernameEditText.setText(currUsername);


        confirmBtn.setOnClickListener(view1 -> {
            String username = usernameEditText.getText().toString();
            if(username.equals(currUsername) || username.isEmpty()){
                return;
            }else {
                updateProfile(username);
            }
        });
    }

    private void updateProfile(String newUsername) {

        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(newUsername).build();

        //Update name in db
        firebaseRepository.setProfileUsername(newUsername);

        mAuth.getCurrentUser().updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                            Toast.makeText(getContext(), "User profile updated", Toast.LENGTH_SHORT).show();
                            usernameViewModel.setName(newUsername);
                            dismiss();
                        }
                    }
                });



    }
}
