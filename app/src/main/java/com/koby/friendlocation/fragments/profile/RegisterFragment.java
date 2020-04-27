package com.koby.friendlocation.fragments.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.koby.friendlocation.R;
import com.koby.friendlocation.viewmodel.NameViewModel;
import com.koby.friendlocation.repository.FirebaseRepository;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.support.DaggerFragment;

public class RegisterFragment extends DaggerFragment {

    private static final String TAG = RegisterFragment.class.getSimpleName();

    @Inject FirebaseAuth mAuth;
    @Inject FirebaseRepository firebaseRepository;
    //Ui element
    @BindView(R.id.register_email) EditText emailTextView;
    @BindView(R.id.register_password) EditText passwordTextView;
    @BindView(R.id.register_progress_bar) ProgressBar progressBar;

    //Private vars
    private String email, password;
    private NameViewModel usernameViewModel;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container,false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        unbinder = ButterKnife.bind(this,view);
        //ViewModel username
        usernameViewModel = ViewModelProviders.of(requireActivity()).get(NameViewModel.class);

    }


    //Validate user input
    public boolean validateInput() {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.equals("")) {
            emailTextView.setError("כתובת האימייל אינה תקינה");
            return false;
        }else if (TextUtils.isEmpty(password)){
            passwordTextView.setError("סיסמא לא נכונה");
            return false;
        } else {
            progressBar.setVisibility(View.VISIBLE);
            return true;
        }
    }

    @OnClick(R.id.register_btn)
    public void register() {

        email = emailTextView.getText().toString();
        password = passwordTextView.getText().toString().trim();

        if(validateInput()) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    setUser();
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success");
                    RegisterProfileFragment profileFragment = new RegisterProfileFragment();
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.fragment, profileFragment);
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

    //Set new user
    private void setUser() {
        //Set user in database
        firebaseRepository.setUserProfileName("אורח");

        usernameViewModel.setName("אורח");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
