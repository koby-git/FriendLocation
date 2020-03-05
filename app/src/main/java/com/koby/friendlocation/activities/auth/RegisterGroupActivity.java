package com.koby.friendlocation.activities.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.koby.friendlocation.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.koby.friendlocation.classes.constant.FirebaseConstants.COMPANIES;
import static com.koby.friendlocation.classes.constant.FirebaseConstants.USERS;

public class RegisterGroupActivity extends AppCompatActivity {

    private static FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private EditText companyTv, nameEt;
    private String company, name;
    private Button registerCompanyBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_company);

        mAuth = FirebaseAuth.getInstance();
        companyTv = findViewById(R.id.et_company);
        nameEt = findViewById(R.id.login_name_et);
        db = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.register_progress_bar);
        registerCompanyBtn = findViewById(R.id.btn_regiter);
        registerCompanyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerCompany();
            }
        });
    }

    private void registerCompany() {

        name = nameEt.getText().toString();
        company = companyTv.getText().toString();
        if (TextUtils.isEmpty(company)) {
            companyTv.setError("נא למלא שם חברה");
        } else if (TextUtils.isEmpty(name)) {
            nameEt.setError("נא למלא שם משתמש");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            db.collection("companies").document(company)
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    if(documentSnapshot.exists()){
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(RegisterGroupActivity.this, "קבוצה זו כבר קיימת במערכת", Toast.LENGTH_SHORT).show();
                    }else {

                        WriteBatch batch = db.batch();
                        Map defaultValue = new HashMap();
                        Map userGroup = new HashMap();
                        ArrayList arrayList= new ArrayList();
                        arrayList.add(company);
                        userGroup.put("group",arrayList);
                        DocumentReference groupRef = db.collection(COMPANIES +"/"+ company +"/"+ USERS).document(mAuth.getUid());
                        DocumentReference userRef = db.collection(USERS).document(mAuth.getUid());

                        batch.set(groupRef,defaultValue);
                        batch.set(userRef,userGroup);
                        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(RegisterGroupActivity.this, company + " הוקמה בהצלחה", Toast.LENGTH_SHORT).show();
//                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    finish();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(RegisterGroupActivity.this, "נכשל", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        });
                    }
                }
            });
        }
    }
}