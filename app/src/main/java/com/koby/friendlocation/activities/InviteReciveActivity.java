package com.koby.friendlocation.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.goodiebag.pinview.Pinview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.koby.friendlocation.R;
import com.koby.friendlocation.login.LoginActivity;

import java.util.HashMap;
import java.util.List;

import static com.koby.friendlocation.classes.FirebaseConstants.GROUPS;
import static com.koby.friendlocation.classes.FirebaseConstants.USERS;


public class InviteReciveActivity extends AppCompatActivity {

    Button join;
    String groupUid,groupName,groupInviteCode;
    private FirebaseFirestore db;
    Pinview pinview;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_recive);

        join = findViewById(R.id.join_group_btn);
        db = FirebaseFirestore.getInstance();
        pinview = findViewById(R.id.pinview);

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(getIntent())
                    .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                        @Override
                        public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                            // Get deep link from result (may be null if no link is found)
                            System.out.println("debug 1");
                            System.out.println(pendingDynamicLinkData.getLink());
                            Uri deepLink = null;
                            if (pendingDynamicLinkData != null) {
                                deepLink = pendingDynamicLinkData.getLink();
                                System.out.println("debug 2");
                            }
                            ///
                            // If the user isn't signed in and the pending Dynamic Link is
                            // an invitation, sign in the user anonymously, and record the
                            // referrer's UID.
                            //

                            if (deepLink != null
                                    && deepLink.getBooleanQueryParameter("groupUid", false)) {
                                String referrerUid = deepLink.getQueryParameter("groupUid");
                                groupUid = deepLink.getQueryParameter("groupUid");

                                pinview.setValue(groupUid);

                                createAnonymousAccountWithReferrerInfo(referrerUid);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    e.printStackTrace();
                }
            });
    }else {
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }


        join.setEnabled(false);




        pinview.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                //Make api calls here or what not

                Toast.makeText(getApplicationContext(), pinview.getValue(), Toast.LENGTH_SHORT).show();

                db.collection(GROUPS).whereEqualTo("groupInviteCode", pinview.getValue()).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    join.setEnabled(true);
                                    List<DocumentSnapshot> groups = task.getResult().getDocuments();
                                    for (DocumentSnapshot documentSnapshot : groups) {
                                        groupUid = documentSnapshot.getId();
                                        groupName = documentSnapshot.get("groupName").toString();
                                        groupInviteCode = documentSnapshot.get("groupInviteCode").toString() ;
                                    }
                                }
                            }
                        });
            }
        });


        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(groupUid+"777");

                WriteBatch batch = db.batch();

                DocumentReference dr = db.collection(USERS).document(mAuth.getUid());

                                        HashMap map = new HashMap();
                        map.put("groupUid",groupUid);
                        map.put("groupName",groupName);
                        map.put("groupInviteCode",groupInviteCode);


                //add user's document reference to group
//                DocumentReference db.collection(GROUPS).document(groupUid).update("dr", FieldValue.arrayUnion(dr));
                db.collection(USERS).document(mAuth.getUid()).update("groups",FieldValue.arrayUnion(map));
                db.collection(USERS).document(mAuth.getUid()).update("groupsUid",FieldValue.arrayUnion(groupUid));

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });


    }

    private void createAnonymousAccountWithReferrerInfo(final String referrerUid) {
        FirebaseAuth.getInstance()
                .signInAnonymously()
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        // Keep track of the referrer in the RTDB. Database calls
                        // will depend on the structure of your app's RTDB.
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//                        DatabaseReference userRecord =
//                                FirebaseDatabase.getInstance().getReference()
//                                        .child("users")
//                                        .child(user.getUid());
//                        userRecord.child("referred_by").setValue(referrerUid);
                    }
                });
    }
}
