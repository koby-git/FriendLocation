package com.koby.friendlocation.activities.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.koby.friendlocation.R;

import java.util.List;

import static com.koby.friendlocation.classes.constant.FirebaseConstants.GROUPS;

public class JoinGroupActivity extends AppCompatActivity {

    Button join;
    String groupUid;
    private FirebaseFirestore db;
    Pinview pinview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        join = findViewById(R.id.join_group_btn);
        db = FirebaseFirestore.getInstance();

        join.setEnabled(false);

        pinview = findViewById(R.id.pinview);
        pinview.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                //Make api calls here or what not



                Toast.makeText(JoinGroupActivity.this, pinview.getValue(), Toast.LENGTH_SHORT).show();

                db.collection(GROUPS).whereEqualTo("groupInviteCode",pinview.getValue()).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            join.setEnabled(true);
                            List<DocumentSnapshot> groups = task.getResult().getDocuments();
                            for (DocumentSnapshot documentSnapshot :groups){
                                groupUid = documentSnapshot.getId();
                            }
                        }
                    }
                });
            }
        });


        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                db.collection(GROUPS).document(groupUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            System.out.println("qqqq");
                        }
                    }
                });
            }
        });








        //        otpView = findViewById(R.id.otp_view);
//        otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
//            @Override public void onOtpCompleted(String otp) {     // do Stuff
//
//                db.collection(GROUPS).whereEqualTo("groupInviteCode",otp).get()
//                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()){
//                            join.setEnabled(true);
//                            List<DocumentSnapshot> groups = task.getResult().getDocuments();
//                            for (DocumentSnapshot documentSnapshot :groups){
//                                groupUid = documentSnapshot.getId();
//                            }
//
//                        }
//                    }
//                });

//            }
//        });

    }
}
