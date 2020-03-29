package com.koby.friendlocation.activities.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.goodiebag.pinview.Pinview;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.koby.friendlocation.R;
import com.koby.friendlocation.activities.auth.LoginActivity;
import com.koby.friendlocation.model.Group;

import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;

import static com.koby.friendlocation.constant.FirebaseConstants.GROUPS;
import static com.koby.friendlocation.constant.FirebaseConstants.USERS;


public class InviteReceiveActivity extends DaggerAppCompatActivity {

    @BindView(R.id.join_group_btn) Button join;
    @BindView(R.id.pinview)Pinview pinview;

    @Inject FirebaseFirestore db;
    @Inject @Nullable FirebaseUser firebaseUser;

    @Inject
    Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_recive);

        ButterKnife.bind(this);

        if (firebaseUser != null) {
            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(getIntent())
                    .addOnSuccessListener(this, pendingDynamicLinkData -> {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        ///
                        // If the user isn't signed in and the pending Dynamic Link is
                        // an invitation, sign in the user anonymously, and record the
                        // referrer's UID.
                        //

                        if (deepLink != null
                                && deepLink.getBooleanQueryParameter("groupUid", false)) {

                            group.setGroupUid(deepLink.getQueryParameter("groupUid"));
                            pinview.setValue(group.getGroupUid());
                        }
                    }).addOnFailureListener(e -> e.printStackTrace());

        } else {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }


        join.setEnabled(false);


        pinview.setPinViewEventListener((pinview, fromUser) -> {
            //Make api calls here or what not

            db.collection(GROUPS).whereEqualTo("groupInviteCode", pinview.getValue()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            join.setEnabled(true);
                            List<DocumentSnapshot> groups = task.getResult().getDocuments();
                            for (DocumentSnapshot documentSnapshot : groups) {

                                group = documentSnapshot.toObject(Group.class);

//                                groupUid = documentSnapshot.getId();
//                                groupName = documentSnapshot.get("groupName").toString();
//                                groupInviteCode = documentSnapshot.get("groupInviteCode").toString();
                            }
                        }
                    });
        });


        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                HashMap map = new HashMap();
//                map.put("groupUid", group.getGroupUid());
//                map.put("groupName", group.getGroupName());
//                map.put("groupInviteCode", group.getGroupInviteCode());

                //add user's document reference to group
                db.collection(GROUPS).document(group.getGroupUid()).update("users", FieldValue.arrayUnion(firebaseUser.getUid()));

                db.collection(USERS).document(firebaseUser.getUid()).update("groupsUid", FieldValue.arrayUnion(group.getGroupUid()));

                db.collection(USERS).document(firebaseUser.getUid()).update("groups", FieldValue.arrayUnion(group));

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

}
