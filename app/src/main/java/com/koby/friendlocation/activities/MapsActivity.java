package com.koby.friendlocation.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityTransition;
import com.google.android.gms.location.ActivityTransitionRequest;
import com.google.android.gms.location.DetectedActivity;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.koby.friendlocation.GroupSettingActivity;
import com.koby.friendlocation.Utils;
import com.koby.friendlocation.classes.GroupAdapter;
import com.koby.friendlocation.classes.LocationConstants;
import com.koby.friendlocation.R;
import com.koby.friendlocation.services.AddressResultReceiver;
import com.koby.friendlocation.services.TransitionIntentService;
import com.koby.friendlocation.classes.Contact;
import com.koby.friendlocation.classes.ContactsAdapter;
import com.koby.friendlocation.classes.Group;
import com.koby.friendlocation.model.LocationDoc;
import com.koby.friendlocation.services.FetchAddressIntentService;

import java.util.ArrayList;
import java.util.List;

import static com.koby.friendlocation.LocationProviderSingleton.FASTEST_UPDATE_INTERVAL;
import static com.koby.friendlocation.LocationProviderSingleton.UPDATE_INTERVAL;
import static com.koby.friendlocation.classes.FirebaseConstants.USERS;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration registration;
    private Group group;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private static boolean requestingLocationUpdates = true;
    private ContactsAdapter contactsAdapter;
    private ArrayList<Contact> contacts;
    protected Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //UI Element
        RecyclerView recyclerView = findViewById(R.id.maps_recycler_view);
        Button addNewMember = findViewById(R.id.maps_add_new_member);
        View bottomSheet = findViewById(R.id.maps_bottom_sheet);

        //Init Database
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        group = (Group) getIntent().getSerializableExtra("group");

        //Set Contacts UI
        BottomSheetBehavior bottomSheetBehavior;
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        //Set Contacts
        setRecyclerview(recyclerView);

        //Sets clickListeners
        addNewMember.setOnClickListener(v -> {

            String link = "https://www.example.com/?groupUid=" + group.getGroupInviteCode();

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
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                }
            });
        });
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

        if (!Utils.checkPermission(this)) {
            System.out.println("No permission");
            Utils.requestPermission(this);
        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startLocationCallback();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private void startLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    System.out.println("Location is null");
                }

                for (Location location : locationResult.getLocations()) {

                    db.collection(USERS).document(mAuth.getUid())
                            .set(new LocationDoc(mAuth.getUid()
                                    , location.getLatitude(), location.getLongitude()), SetOptions.merge());

                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        };

    }

    private void sendDynamicLink(Uri shortLink) {

        String invitationLink = shortLink.toString();

        String message = mAuth.getCurrentUser().getDisplayName() + " wants to invite you to Friends location!" +
                "Let's join Friends location! Here is my group invite code - " + group.getGroupInviteCode() + " Use my referrer link: "
                + invitationLink;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(share, "Share friend location app"));
    }

//    protected void startIntentService() {
//        Intent intent = new Intent(this, FetchAddressIntentService.class);
//        intent.putExtra(LocationConstants.RECEIVER, resultReceiver);
//        intent.putExtra(LocationConstants.LOCATION_DATA_EXTRA, lastLocation);
//        startService(intent);
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        mMap.setMyLocationEnabled(true);
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
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
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        createLocationRequest();
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

    @Override
    protected void onStart() {
        super.onStart();

        Query query = db.collection(USERS).whereArrayContains("groupsUid",group.getGroupUid());

        registration = query.addSnapshotListener((queryDocumentSnapshots, ex) -> {

            mMap.clear();
            contacts.clear();

            //get contacts
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                try {

                    Contact contact = documentSnapshot.toObject(Contact.class);

                    LatLng latLng = new LatLng(contact.getLatitude(), contact.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(contact.getName()));

                    contacts.add(contact);

                } catch (NullPointerException e) {
                }
            }

            contactsAdapter.notifyDataSetChanged();
        });

        // Activity Detection
        registerHandler();
    }

    private void registerHandler() {
        Intent intent = new Intent(this, TransitionIntentService.class);
        PendingIntent transitionPendingIntent = PendingIntent.getService(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        List<ActivityTransition> transitions = new ArrayList<>();

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.STILL)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        transitions.add(
                new ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build());

        ActivityTransitionRequest request = new ActivityTransitionRequest(transitions);
        Task<Void> task = ActivityRecognition.getClient(this)
                .requestActivityTransitionUpdates(request, transitionPendingIntent);

        task.addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Handle success

                    }
                }
        );

        task.addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // Handle error
                        e.printStackTrace();
                    }
                }
        );

    }

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

    @Override
    protected void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    null /* Looper */);
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //Options
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_setting:
                Intent intent = new Intent(this, GroupSettingActivity.class);
                intent.putExtra("groupContacts",contacts);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


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
