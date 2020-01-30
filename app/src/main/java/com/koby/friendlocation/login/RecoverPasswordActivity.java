package com.koby.friendlocation.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.koby.friendlocation.R;

public class RecoverPasswordActivity extends AppCompatActivity {

    private EditText passwordEmailEt;
    private Button resetPasswordBtn;
    private FirebaseAuth mAuth;
    private String passwordEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);

        passwordEmailEt = findViewById(R.id.et_password_email);
        resetPasswordBtn = findViewById(R.id.btn_reset_password);


        mAuth = FirebaseAuth.getInstance();

        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordEmail = passwordEmailEt.getText().toString().trim();
                //cheking if its email pattern.
                if (Patterns.EMAIL_ADDRESS.matcher(passwordEmail).matches()){
                    mAuth.sendPasswordResetEmail(passwordEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(RecoverPasswordActivity.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RecoverPasswordActivity.this,LoginActivity.class));
                                finish();
                            }else {
                                Toast.makeText(RecoverPasswordActivity.this, "Error in sending password reset email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    passwordEmailEt.setError("Email is not valid");
                }
            }
        });

    }
}
