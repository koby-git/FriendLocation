package com.koby.friendlocation.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.koby.friendlocation.R;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

public class RecoverPasswordActivity extends DaggerAppCompatActivity {

    @Inject
    FirebaseAuth mAuth;

    @BindView(R.id.recover_password_email)
    EditText passwordEmailEt;

    private String passwordEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recover_password);
        ButterKnife.bind(this);
    }

    //send password rest email
    @OnClick(R.id.recover_password_button)
    public void recoverPassword() {

        passwordEmail = passwordEmailEt.getText().toString().trim();

        if(emailValidation()) {
            mAuth.sendPasswordResetEmail(passwordEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(RecoverPasswordActivity.this, "Password reset email sent!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RecoverPasswordActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RecoverPasswordActivity.this, "Error in sending password reset email", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public boolean emailValidation(){
        //validate email pattern.
        if (Patterns.EMAIL_ADDRESS.matcher(passwordEmail).matches()){
            //send password rest email
            return true;
        }else {
            passwordEmailEt.setError("Email is not valid");
            return false;
        }
    }
}


