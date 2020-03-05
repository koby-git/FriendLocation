package com.koby.friendlocation.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.koby.friendlocation.R;
import com.koby.friendlocation.classes.model.Group;

public class InviteContactActivity extends AppCompatActivity {

    Button inviteContact;
    EditText inviteContactEmail;
    private Group group;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_contact);

        group = (Group) getIntent().getSerializableExtra("group");

        inviteContactEmail = findViewById(R.id.invite_contact_email);
        inviteContact = findViewById(R.id.invite_contact_btn);
        inviteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                String uid = user.getUid();
                String link = "https://www.example.com/?groupUid=" + group.getGroupInviteCode();
//                String link = "https://www.example.com";

                FirebaseDynamicLinks.getInstance().createDynamicLink()
                        .setLink(Uri.parse(link))
                        .setDomainUriPrefix("https://friendlocationv2.page.link")
                        .setAndroidParameters(
                                new DynamicLink.AndroidParameters.Builder()
                                        .build())
                        .buildShortDynamicLink()
                        .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                            @Override
                            public void onSuccess(ShortDynamicLink shortDynamicLink) {
                                sendDynamicLink(shortDynamicLink.getShortLink());
                                // ...
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

    }

    private void sendDynamicLink(Uri shortLink) {

        String referrerName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String subject = String.format("%s wants to invite you to Friends location!", referrerName);
        String invitationLink = shortLink.toString();
        System.out.println("666666");
        String msg = "Let's join Friends location! Here is my group invite code - " + group.getGroupInviteCode() + " Use my referrer link: "
                + invitationLink;
        String msgHtml = String.format("<p>Let's join Friends location! Here is my your group invite code %s Use my "
                + "<a href=\"%s\">referrer link</a>!</p>",group.getGroupUid(), invitationLink);

        String message = "Let's join Friends location! Here is my group invite code - " + group.getGroupInviteCode() + " Use my referrer link: "
                + invitationLink;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(share, "Share friend location app"));


    }
}
