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

import static com.koby.friendlocation.classes.FirebaseConstants.USERS;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 4455;
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ListenerRegistration registration;
    private Group group;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static LocationRequest locationRequest;
    private static boolean requestingLocationUpdates = false;
    private ContactsAdapter contactsAdapter;
    private ArrayList<Contact> contacts;
    private Contact userContact;
    private String userName;
    protected Location lastLocation;
    private AddressResultReceiver resultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        group = (Group) getIntent().getSerializableExtra("group");
        RecyclerView recyclerView = findViewById(R.id.maps_recycler_view);
        Button addNewMember = findViewById(R.id.maps_add_new_member);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        contacts = new ArrayList<>();
        userContact = new Contact();

        resultReceiver = new AddressResultReceiver(new Handler());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        setSupportActionBar(toolbar);
        BottomSheetBehavior bottomSheetBehavior;
        View bottomSheet = findViewById(R.id.maps_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)
        contactsAdapter = new ContactsAdapter(contacts, this);
        recyclerView.setAdapter(contactsAdapter);

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

        if (!Utils.checkFineLocationPermission(this)) {
            Utils.requestFineLocationPermission(this);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        startLocationCallback();

        requestingLocationUpdates = true;

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

//        String referrerName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

        String invitationLink = shortLink.toString();

        String message = userName + " wants to invite you to Friends location!" +
                "Let's join Friends location! Here is my group invite code - " + group.getGroupInviteCode() + " Use my referrer link: "
                + invitationLink;
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(share, "Share friend location app"));
    }

    protected void startIntentService() {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(LocationConstants.RECEIVER, resultReceiver);
        intent.putExtra(LocationConstants.LOCATION_DATA_EXTRA, lastLocation);
        startService(intent);
    }

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
                                startIntentService();
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

        System.out.println("//////");
        System.out.println(group.getGroupUid());
        Query query = db.collection(USERS).whereArrayContains("groupsUid",group.getGroupUid());

        registration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException ex) {

//                System.out.println("exeption " + ex.toString());
                System.out.println("****");
                mMap.clear();
                contacts.clear();

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                    boolean isOnline = true;
                    try {
                        isOnline = (Boolean) documentSnapshot.get("isOnline");
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (isOnline) {

                        Contact contact = new Contact();

                        if(documentSnapshot.getId().equals(mAuth.getUid())){
                            userName = mAuth.getCurrentUser().getDisplayName();
                            contact.setName(userName);
                            contact.setImageUri(mAuth.getCurrentUser().getPhotoUrl().toString());
                            if(documentSnapshot.get("state").equals("STILL")) {
                                stopLocationUpdates();
                            }else {
                                startLocationUpdates();
                            }
                        }

                        try {
                            contact.setAddress(documentSnapshot.get("address").toString());
                        } catch (NullPointerException e) {
                            contact.setAddress("update...");
                        }

                        try {
                            contact.setDate(documentSnapshot.get("date").toString());
                        } catch (NullPointerException e) {
                            contact.setDate("update...");
                        }

                        try {
                            contact.setState(documentSnapshot.get("state").toString());
                        } catch (NullPointerException e) {
                            contact.setState("update...");
                        }

//                        String name = documentSnapshot.get("name").toString();

                        try {
                            double latitude = documentSnapshot.getDouble("latitude");
                            double longitude = documentSnapshot.getDouble("longitude");

                            contact.setLatitude(latitude);
                            contact.setLongitude(longitude);
                            LatLng latLng = new LatLng(latitude, longitude);
                            mMap.addMarker(new MarkerOptions().position(latLng).title(userName));

                        } catch (NullPointerException e) {

                        }
//                        contact.setName(name);

                        contacts.add(contact);

                        contactsAdapter.notifyDataSetChanged();
                    }
                }

            }
        });

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

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (requestingLocationUpdates) {
            startLocationUpdates();
//        }
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
//        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        requestingLocationUpdates = false;
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

//    public static class AddressResultReceiver extends ResultReceiver {
//        public AddressResultReceiver(Handler handler) {
//            super(handler);
//        }
//
//        @Override
//        protected void onReceiveResult(int resultCode, Bundle resultData) {
//
//            if (resultData == null) {
//                return;
//            }
//
//            // Display the address string
//            // or an error message sent from the intent service.
//            String addressOutput = resultData.getString(LocationConstants.RESULT_DATA_KEY);
//            if (addressOutput == null) {
//                addressOutput = "";
//            }
//
////            displayAddressOutput();
//
//            // Show a toast message if an address was found.
//            if (resultCode == LocationConstants.SUCCESS_RESULT) {
////                showToast(getString(R.string.address_found));
//
////                SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy hh:mm:ss", Locale.getDefault());
//
//                final Map map = new HashMap();
//                map.put("address", addressOutput);
//
////                formatter.format(Calendar.getInstance().getTime())
//                map.put("date", DateFormat.getDateTimeInstance().format(new Date()));
//
//                Contact contact = new Contact();
//
//                contact.setAddress(addressOutput);
////                formatter.format(Calendar.getInstance().getTime())
////                DateFormat.getTimeInstance().format(new Date())
//                contact.setDate(DateFormat.getDateTimeInstance().format(new Date()));
//                System.out.println(DateFormat.getTimeInstance().format(new Date())+"444");
//                contact.setName(mAuth.getCurrentUser().getEmail());
//
//                contactsAdapter.notifyDataSetChanged();
//
//                db.collection(USERS).document(mAuth.getUid()).set(map, SetOptions.merge());
//
//                if (!contacts.isEmpty()) {
//                    for (Contact c : contacts) {
//                        if (c.getName().equals(mAuth.getCurrentUser().getEmail())) {
//                            c.setAddress(addressOutput);
//                        }
//                    }
//
//                }
//            }
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

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


}
