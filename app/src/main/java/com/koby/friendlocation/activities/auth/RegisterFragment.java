package com.koby.friendlocation.activities.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.koby.friendlocation.R;
import com.koby.friendlocation.fragments.ProfileFragment;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterActivity";

    private Button registerBtn;
    private FirebaseAuth mAuth;
    private EditText emailTv, passwordTv;
    private String email, password;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container,false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        passwordTv = view.findViewById(R.id.register_password);

        emailTv = view.findViewById(R.id.register_email);

        registerBtn = view.findViewById(R.id.register_btn);
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        progressBar = view.findViewById(R.id.register_progress_bar);
    }


    public void register() {

        email = emailTv.getText().toString();
        password = passwordTv.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.equals("")) {
            emailTv.setError("כתובת האימייל אינה תקינה");
        }else if (TextUtils.isEmpty(password)){
            passwordTv.setError("סיסמא לא נכונה");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            signIn();
        }
    }

    private void signIn() {
        mAuth.createUserWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                RegisterProfileFragment profileFragment = new RegisterProfileFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                transaction.replace(R.id.fragment, profileFragment);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // If sign in fails, display a message to the user.
                progressBar.setVisibility(View.GONE);
                e.printStackTrace();
                Toast.makeText(getActivity(), "createUser failed.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
