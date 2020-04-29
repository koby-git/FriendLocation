package com.koby.friendlocation.activities.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.koby.friendlocation.R;
import com.koby.friendlocation.activities.auth.LoginActivity;
import com.koby.friendlocation.model.Group;
import com.koby.friendlocation.repository.FirebaseRepository;

import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.DaggerAppCompatActivity;


public class InviteReceiveActivity extends DaggerAppCompatActivity {

    public static final String TAG = InviteReceiveActivity.class.getSimpleName();

    @BindView(R.id.invite_receive_join_button) Button join;
    @BindView(R.id.invite_receive_image) ImageView groupImageView;
    @BindView(R.id.invite_receive_group_name) TextView groupName;

    @Inject FirebaseRepository firebaseRepository;
    @Inject Group group;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_recive);

        ButterKnife.bind(this);

        if (firebaseRepository.getCurrentUser() != null) {
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

                            firebaseRepository.getGroup(deepLink.getQueryParameter("groupUid"))
                                    .addOnTaskCompleteListener(new FirebaseRepository.OnTaskCompleteListener() {
                                @Override
                                public void onComplete(Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        join.setEnabled(true);
                                        group = task.getResult().toObject(Group.class);
                                        groupName.setText(group.getName());
                                        Glide.with(InviteReceiveActivity.this)
                                                .load(group.getImage())
                                                .centerCrop()
                                                .error(R.drawable.ic_group_grey)
                                                .fallback(R.drawable.ic_group_grey)
                                                .into(groupImageView);

                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(e -> e.printStackTrace());

        } else {
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            finish();
        }

        join.setOnClickListener(v ->
                firebaseRepository.addGroupUser(group)
                .addOnTaskCompleteListener(
                        (FirebaseRepository.OnBatchCompleteListener) task -> {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.i(TAG, task.getException().toString());
                                Toast.makeText(InviteReceiveActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }));
    }
}
