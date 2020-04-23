package com.koby.friendlocation.activities.maps;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.koby.friendlocation.activities.main.SettingsActivity;
import com.koby.friendlocation.view.adapter.ContactsAdapter;
import com.koby.friendlocation.providers.LocationProvider;
import com.koby.friendlocation.repository.FirebaseRepository;
import com.koby.friendlocation.utils.Utils;
import com.koby.friendlocation.R;
import com.koby.friendlocation.model.Contact;
import com.koby.friendlocation.model.Group;

import java.util.ArrayList;

import javax.annotation.Nullable;
import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

public class MapsActivity extends DaggerAppCompatActivity implements OnMapReadyCallback {

    //Dagger injection
    @Inject LocationProvider locationProvider;
    @Inject @Nullable FirebaseUser firebaseUser;
    @Inject FirebaseRepository firebaseRepository;

    //Ui element
    @BindView(R.id.maps_toolbar) Toolbar toolbar;
    @BindView(R.id.maps_toolbar_imageview) ImageView toolbarGroupImage;
    @BindView(R.id.maps_toolbar_textview) TextView toolbarGroupName;
    @BindView(R.id.maps_recycler_view) RecyclerView recyclerView;
    @BindView(R.id.maps_bottom_sheet) View bottomSheet;

    //private vars
    private GoogleMap mMap;
    private Group group;
    private ContactsAdapter contactsAdapter;
    private ArrayList<Contact> contacts;
    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

        //Get chosen group object
        group = (Group) getIntent().getSerializableExtra("group");

        //Set toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        toolbarGroupName.setText(group.getName());
        Glide.with(this)
                .load(group.getImage())
                .circleCrop()
                .into(toolbarGroupImage);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Set Contacts UI bottom sheet
        BottomSheetBehavior bottomSheetBehavior;
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        //Check permission
        //if permission not granted - request permission
        if (!Utils.checkPermission(this)) {
            Utils.requestPermission(this);
        }

        //Set contacts recylerview
        setUsersRecyclerView();

    }
    //Start listen to users locations database
    @Override
    protected void onStart() {
        super.onStart();

        registration = firebaseRepository.getUsersQuery(group.getUid())
                .addSnapshotListener((queryDocumentSnapshots, ex) -> {

                    //Update ui when receive new location from group members
                    updateUi(queryDocumentSnapshots);
                });
    }

    //Set map and update camera
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        locationProvider.getFusedLocationProviderClient()
                .getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
                    LatLng myLocation = new LatLng((location.getLatitude()), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(myLocation)      // Sets the center of the map to Mountain View
                            .zoom(17)                   // Sets the zoom
                            .bearing(90)                // Sets the orientation of the camera to east
                            .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                            .build();                   // Creates a CameraPosition from the builder
                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        });
    }

    //Stop listening to group firestoreUi
    @Override
    protected void onStop() {
        super.onStop();
        if(registration!=null){
            registration.remove();
        }
    }

    //Toolbar button - start group settings activity
    @OnClick(R.id.maps_toolbar)
    public void groupSetting(){
        Intent intent = new Intent(MapsActivity.this, GroupSettingActivity.class);
        intent.putExtra("groupContacts",contacts);
        intent.putExtra("groupUid",group.getUid());
        intent.putExtra("groupName",group.getName());
        startActivity(intent);
    }

     //Send invite to the wanted contact
    @OnClick(R.id.maps_add_new_member)
    public void sendInvite() {
        String link = "https://www.example.com/?groupUid=" + group.getInviteCode();

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDomainUriPrefix("https://firendlocation.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder()
                                .build())
                .buildShortDynamicLink()
                .addOnSuccessListener(shortDynamicLink -> {

                    String invitationLink = shortDynamicLink.getShortLink().toString();

                    String message = firebaseUser.getDisplayName() + " wants to invite you to Friends location!" +
                            "Let's join Friends location! Here is my group invite code - " + group.getInviteCode() + " Use my referrer link: "
                            + invitationLink;
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, message);

                    startActivity(Intent.createChooser(share, "Share friend location app"));
                }).addOnFailureListener(e -> e.printStackTrace());
    }

    //Set group members
    private void setUsersRecyclerView() {
        contacts = new ArrayList<>();
        contactsAdapter = new ContactsAdapter(contacts,this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(contactsAdapter);

        //Zoom contact location
        contactsAdapter.setOnItemClickListener(contact -> {
            LatLng myLocation = new LatLng(contact.getLatitude(), contact.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(myLocation)      // Sets the center of the map to Mountain View
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        });

        //Zoom contact image
        contactsAdapter.setImageClickListener(new ContactsAdapter.OnImageClickListener(){
            @Override
            public void onImageClick(String imageUri) {
                LayoutInflater inflater = LayoutInflater.from(MapsActivity.this);
                View view =inflater.inflate(R.layout.dialog_image, null);
                ImageView imageView = view.findViewById(R.id.dialog_image);
                Glide.with(MapsActivity.this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_delete_forever)
                        .fallback(R.drawable.ic_group_grey)
                        .centerCrop()
                        .into(imageView);

                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                builder.setView(view);
                builder.show();
            }
        });
    }

    //Update ui every location callback from group members
    private void updateUi(QuerySnapshot queryDocumentSnapshots) {

        //Clear screen when users update
        mMap.clear();
        contacts.clear();

        //get contacts
        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
            try {

                //Add contact
                Contact contact = documentSnapshot.toObject(Contact.class);
                contacts.add(contact);
                contactsAdapter.notifyDataSetChanged();

                //Add member marker to map
                LatLng latLng = new LatLng(contact.getLatitude(), contact.getLongitude());
                mMap.addMarker(new MarkerOptions().position(latLng).title(contact.getName()));

            } catch (NullPointerException e) {
            }
        }
    }

}
