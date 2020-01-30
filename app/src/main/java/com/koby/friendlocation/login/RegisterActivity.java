package com.koby.friendlocation.login;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.koby.friendlocation.R;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private Button registerBtn;
    private FirebaseAuth mAuth;
    private EditText emailTv, passwordTv ,companyTv,nameEt;
    private String email, password,company,name;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        RegisterFragment registerFragment = new RegisterFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment, registerFragment);
        transaction.addToBackStack(null);

        transaction.commit();

//        mAuth = FirebaseAuth.getInstance();
//
//        passwordTv = findViewById(R.id.et_sign_password1);
//
//
//        emailTv = findViewById(R.id.et_email);
//
//        registerBtn = findViewById(R.id.btn_regiter);
//        registerBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                register();
//            }
//        });
//
//        progressBar = findViewById(R.id.register_progress_bar);
    }

//    public void register() {
//
//        email = emailTv.getText().toString();
//        password = passwordTv.getText().toString().trim();
//
//        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.equals("")) {
//            emailTv.setError("כתובת האימייל אינה תקינה");
//        }else if (TextUtils.isEmpty(password)){
//            passwordTv.setError("סיסמא לא נכונה");
//        } else {
//            progressBar.setVisibility(View.VISIBLE);
//            signIn();
//        }
//    }

//    private void signIn() {
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d(TAG, "createUserWithEmail:success");
//                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                            startActivity(intent);
//                            finish();
//
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            progressBar.setVisibility(View.GONE);
//                            System.out.println(task.getException()+"iii");
//                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            Toast.makeText(RegisterActivity.this, "createUser failed.",
//                                    Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//    }
}
