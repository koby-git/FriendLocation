package com.koby.friendlocation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    FirebaseAuth mAuth;
    EditText usernameEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
//        usernameEditText = view.findViewById(R.id.profile_username);

//        usernameEditText.setText(mAuth.getCurrentUser().getDisplayName());

        ImageView imageView = view.findViewById(R.id.profile_imageview);
        if (user.getPhotoUrl()!=null){
//            Glide.with(view).load(user.getPhotoUrl()).circleCrop().into(imageView);
                        Glide.with(view).load(user.getPhotoUrl()).into(imageView);
        }


        imageView.setOnClickListener(v -> {
            CameraBottomSheet cameraBottomsheet = new CameraBottomSheet();
//            args = new Bundle();
//            args.putSerializable("files", filesName);
//            args.putString(TASK_UID, task.getTaskUid());
//            cameraBottomsheet.setArguments(args);
            cameraBottomsheet.show(getActivity().getSupportFragmentManager(), "bottomSheet");
        });
    }
}
