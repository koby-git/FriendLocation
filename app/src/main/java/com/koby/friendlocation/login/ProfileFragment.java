package com.koby.friendlocation.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.koby.friendlocation.activities.MainActivity;
import com.koby.friendlocation.R;

import java.util.HashMap;
import java.util.Map;

import static com.koby.friendlocation.classes.FirebaseConstants.USERS;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private EditText username;
    private Button nextBtn;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container,false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
//        username = view.findViewById(R.id.profile_username);
//        nextBtn = view.findViewById(R.id.profile_next_btn);
        mAuth = FirebaseAuth.getInstance();

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!username.getText().toString().isEmpty()){
                    Map name = new HashMap();
                    name.put("name",username.getText().toString());
                    db.collection(USERS).document(mAuth.getUid()).set(name)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(getActivity(), MainActivity.class));
                                    }else {
                                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            e.printStackTrace();
                        }
                    });
                }

            }
        });

    }
}
