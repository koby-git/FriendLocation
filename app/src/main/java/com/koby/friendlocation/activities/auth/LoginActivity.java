package com.koby.friendlocation.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.koby.friendlocation.activities.main.MainActivity;
import com.koby.friendlocation.R;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

public class LoginActivity extends DaggerAppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Inject FirebaseAuth mAuth;

    @BindView(R.id.login_progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.login_email)
    EditText emailTv;
    @BindView(R.id.login_password)
    EditText passwordTv;

    private String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
    }

    //Login button
    @OnClick(R.id.login_button)
    public void login() {

        if (inputValidation()) {

            progressBar.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                progressBar.setVisibility(View.GONE);
                                Log.w(TAG, getString(R.string.authentacaion_failed), task.getException());
                                Toast.makeText(LoginActivity.this, getString(R.string.authentacaion_failed),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    ////Forget password button
    @OnClick(R.id.login_forgot_password)
    public void forgotPassword() {
        startActivity(new Intent(getApplicationContext(), RecoverPasswordActivity.class));
    }

    //Register button
    @OnClick(R.id.login_regiter)
    public void register() {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

    //Check if user input is valid
    private boolean inputValidation() {

        email = emailTv.getText().toString();
        password = passwordTv.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.equals("")) {
            emailTv.setError("כתובת האימייל אינה תקינה");
            return false;
        } else if (TextUtils.isEmpty(password)) {
            passwordTv.setError("סיסמא לא נכונה");
            return false;
        }

        return true;
    }



}