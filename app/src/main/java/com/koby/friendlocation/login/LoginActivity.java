package com.koby.friendlocation.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.koby.friendlocation.activities.MainActivity;
import com.koby.friendlocation.R;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    private Button login;
    private String email, password;
    private ProgressBar progressBar;
    private TextView forgotPasswordTv;
    private TextView registerTv;
    private FirebaseFirestore db;
    private CollectionReference users;
    private ImageView loadingImg;
    private LinearLayout linearLayout;
    private FirebaseUser firebaseUser;
    private EditText emailTv, passwordTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        loadingImg = findViewById(R.id.login_loading_img);
        passwordTv = findViewById(R.id.login_password_et);
        emailTv = findViewById(R.id.login_email_et);

        Glide.with(this).asGif().load(R.drawable.loading_radar).into(loadingImg);

        linearLayout = findViewById(R.id.login_linearLayout);
        db = FirebaseFirestore.getInstance();
        users = db.collection("users");
        passwordTv = findViewById(R.id.login_password_et);
        emailTv = findViewById(R.id.login_email_et);
        login = findViewById(R.id.login_login_btn);
        progressBar = findViewById(R.id.login_progress_bar);
        forgotPasswordTv = findViewById(R.id.login_forgot_password_tv);
        registerTv = findViewById(R.id.login_regiter_tv);

        mAuth = FirebaseAuth.getInstance();

//        UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName();


        firebaseUser = mAuth.getCurrentUser();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgotPassword();
            }
        });
        registerTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        if (firebaseUser != null) {
            loginPreviousUser();

        } else {
            loadingImg.setVisibility(View.GONE);
        }
    }

    private void loginPreviousUser() {

        linearLayout.setVisibility(View.GONE);

                startUserActivity();
}

    public void login() {

        email = emailTv.getText().toString();
        password = passwordTv.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.equals("")) {
            emailTv.setError("כתובת האימייל אינה תקינה");
        } else if (TextUtils.isEmpty(password)) {
            passwordTv.setError("סיסמא לא נכונה");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                startUserActivity();
                                // Sign in success, update UI with the signed-in user's information

                            } else {
                                // If sign in fails, display a message to the user.
                                progressBar.setVisibility(View.GONE);
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                            // ...
                        }
                    });
        }
    }

    private void startUserActivity() {
        Log.d(TAG, "signInWithEmail:success");
        final FirebaseUser user = mAuth.getCurrentUser();

        users.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LoginActivity.this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show();

            }
        });
    }

    public void forgotPassword() {
        startActivity(new Intent(getApplicationContext(), RecoverPasswordActivity.class));
    }

    public void register() {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
    }

}