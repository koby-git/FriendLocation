package com.koby.friendlocation.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.koby.friendlocation.activities.main.MainActivity;
import com.koby.friendlocation.R;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

import static com.koby.friendlocation.classes.constant.FirebaseConstants.USERS;

public class LoginActivity extends DaggerAppCompatActivity {

    private static final String TAG = "LoginActivity";

    @Inject
    FirebaseAuth mAuth;

    @Inject
    @Nullable
    FirebaseUser firebaseUser;

    @Inject
    FirebaseFirestore db;

    private Button login;
    private String email, password;
    private ProgressBar progressBar;
    private TextView forgotPasswordTv,registerTv;
    private EditText emailTv, passwordTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        //UI element
        passwordTv = findViewById(R.id.login_password_et);
        emailTv = findViewById(R.id.login_email_et);
        login = findViewById(R.id.login_login_btn);
        progressBar = findViewById(R.id.login_progress_bar);
        forgotPasswordTv = findViewById(R.id.login_forgot_password_tv);
        registerTv = findViewById(R.id.login_regiter_tv);

        //Login button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(inputValidation()){
                    login();
                }
            }
        });

        //Forget password buttton
        forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });

        //Register button
        registerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

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

    //Login user
    public void login() {

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
//                            startUserActivity();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressBar.setVisibility(View.GONE);
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

//    //Start main activity
//    private void startUserActivity() {
//        Log.d(TAG, "signInWithEmail:success");
//
//        db.collection(USERS).document(firebaseUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
//                finish();
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                progressBar.setVisibility(View.GONE);
//                Toast.makeText(LoginActivity.this, "Authentication failed.",
//                        Toast.LENGTH_SHORT).show();
//
//            }
//        });
//    }

    //Forgert password activity button
    public void forgotPassword() {
        startActivity(new Intent(getApplicationContext(), RecoverPasswordActivity.class));
    }

    //Register Activity button
    public void register() {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

}