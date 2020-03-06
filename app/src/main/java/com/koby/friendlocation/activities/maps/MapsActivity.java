package com.koby.friendlocation.activities.maps;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.koby.friendlocation.activities.GroupSettingActivity;
import com.koby.friendlocation.repository.FirebaseRepository;
import com.koby.friendlocation.utils.Utils;
import com.koby.friendlocation.R;
import com.koby.friendlocation.classes.model.Contact;
import com.koby.friendlocation.classes.adapter.ContactsAdapter;
import com.koby.friendlocation.classes.model.Group;

import java.util.ArrayList;

import javax.annotation.Nullable;
import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

public class MapsActivity extends DaggerAppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();

    @Inject
    FirebaseFirestore db;

    @Inject
    @Nullable
    FirebaseUser firebaseUser;

    @Inject
    FirebaseRepository firebaseRepository;

    private ListenerRegistration registration;
    private GoogleMap mMap;
    private Group group;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean requestingLocationUpdates = true;
    private ContactsAdapter contactsAdapter;
    private ArrayList<Contact> contacts;
    protected Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Get chosen group object
        group = (Group) getIntent().getSerializableExtra("group");

        //Set toolbar
        Toolbar toolbar = findViewById(R.id.maps_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(group.getGroupName());

        //Start group settings activity
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity.this, GroupSettingActivity.class);
                intent.putExtra("groupContacts",contacts);
                startActivity(intent);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //UI Element
        RecyclerView recyclerView = findViewById(R.id.maps_recycler_view);
        Button addNewMember = findViewById(R.id.maps_add_new_member);
        View bottomSheet = findViewById(R.id.maps_bottom_sheet);

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
        setRecyclerview(recyclerView);

        //Add new member to the group
        addNewMember.setOnClickListener(v -> { sendInvite(); });

        //Choose contact to zoom in
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

        //Get fused location provider client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //start listening to user location callback
        startLocationCallback();

    }

    //Send invite to the wanted contact
    private void sendInvite() {
        String link = "https://www.example.com/?groupUid=" + group.getGroupInviteCode();

        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(link))
                .setDomainUriPrefix("https://firendlocation.page.link")
                .setAndroidParameters(
                        new DynamicLink.AndroidParameters.Builder()
                                .build())
                .buildShortDynamicLink()
                .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                    @Override
                    public void onSuccess(ShortDynamicLink shortDynamicLink) {
                        sendDynamicLink(shortDynamicLink.getShortLink());
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    //start listening to user location callback
    private void startLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    Log.i(TAG,"Location is null");
                }

                //Get location callback
                for (Location location : locationResult.getLocations()) {

                    //Set user location in database
                    firebaseRepository.setLocationUpdatesResult(MapsActivity.this,location);

                    //Update camera
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        };

    }

    //Send dynamicLink to the wanted contact
    private void sendDynamicLink(Uri shortLink) {

        String invitationLink = shortLink.toString();

        String message = firebaseUser.getDisplayName() + " wants to invite you to Friends location!" +
                "Let's join Friends location! Here is my group invite code - " + group.getGroupInviteCode() + " Use my referrer link: "
                + invitationLink;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(share, "Share friend location app"));
    }


    //Set map
    //Update camera
    //provide location callback
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

        //Try get last location and update camera
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location and update camera
                            if (location != null) {
                                lastLocation = location;
                                LatLng myLocation = new LatLng((location.getLatitude()), location.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(myLocation)      // Sets the center of the map to Mountain View
                                        .zoom(17)                   // Sets the zoom
                                        .bearing(90)                // Sets the orientation of the camera to east
                                        .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                                        .build();                   // Creates a CameraPosition from the builder
                                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                //Make delay to update camera
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Create location request object
                                        createLocationRequest();
                                        //Start to provide location callback
                                        startLocationUpdates();
                                    }
                                }, 2000);
                            }else {
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }


    //Start listen to users locations database
    @Override
    protected void onStart() {
        super.onStart();

        Query query = firebaseRepository.getUsersQuery(group);

        registration = query.addSnapshotListener((queryDocumentSnapshots, ex) -> {

            mMap.clear();
            contacts.clear();

            //get contacts
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                try {

                    Contact contact = documentSnapshot.toObject(Contact.class);

                    LatLng latLng = new LatLng(contact.getLatitude(), contact.getLongitude());

                    //Add member marker to map
                    mMap.addMarker(new MarkerOptions().position(latLng).title(contact.getName()));

                    contacts.add(contact);

                } catch (NullPointerException e) {
                }
            }

            contactsAdapter.notifyDataSetChanged();
        });

    }

    //Create location request object
    public void createLocationRequest() {
        locationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        locationRequest.setInterval(3000);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        locationRequest.setFastestInterval(1500);

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //If requesting location updates - start to provide location update
    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    //Start to provide location update
    private void startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    /*
    Stop listen to users locations database
    Stop to provide user location callback
    */
    @Override
    protected void onPause() {
        super.onPause();
        if(registration!=null) {
            registration.remove();
        }
        stopLocationUpdates();
    }


    private void stopLocationUpdates() {
        requestingLocationUpdates = false;
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    //Inflate options menu
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//        return true;
//    }

    //Handle options menu
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle item selection
//        switch (item.getItemId()) {
//            case R.id.action_setting:
//                Intent intent = new Intent(this, GroupSettingActivity.class);
//                intent.putExtra("groupContacts",contacts);
//                startActivity(intent);
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }


    private void setRecyclerview(RecyclerView recyclerView) {
        contacts = new ArrayList<>();
        // specify an adapter (see also next example)
        contactsAdapter = new ContactsAdapter(contacts, this);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)

        recyclerView.setAdapter(contactsAdapter);
    }


}
