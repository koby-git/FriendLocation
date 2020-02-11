package com.koby.friendlocation.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.koby.friendlocation.LocationUpdatesBroadcastReceiver;
import com.koby.friendlocation.R;
import com.koby.friendlocation.SettingsActivity;
import com.koby.friendlocation.classes.Group;
import com.koby.friendlocation.classes.GroupAdapter;
import com.koby.friendlocation.login.LoginActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.koby.friendlocation.Utils;

import static com.koby.friendlocation.classes.FirebaseConstants.GROUPS;
import static com.koby.friendlocation.classes.FirebaseConstants.USERS;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private GroupAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Group> groupList;
    private FirebaseFirestore db;
    private ExtendedFloatingActionButton createFab,joinFab;
    private int spanCount;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL = 60000; // Every 60 seconds.

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    public static final long FASTEST_UPDATE_INTERVAL = 30000; // Every 30 seconds

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    public static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5; // Every 5 minutes.

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Provides access to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //UI ELEMENT
        createFab = findViewById(R.id.main_fab_create);
        joinFab = findViewById(R.id.main_fab_join);
        recyclerView = findViewById(R.id.main_recycler_view);

        setRecyclerview(recyclerView);
        getGroups();

//        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
//            spanCount = 6;
//        }else {
//            spanCount = 3;
//        }


        createFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AddGroupActivity.class));
//                    DialogFragment newFragment = new NewGroupDialog();
//                    newFragment.show(getSupportFragmentManager(), "newGroup");
//                NewGroupFragment newGroupFragment = new NewGroupFragment();
//                newGroupFragment.show(getSupportFragmentManager(),"newGroup");
            }
        });

        joinFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, JoinGroupActivity.class));
            }
        });

        mAdapter.setOnItemClickListener(new GroupAdapter.onItemClickListener() {
            @Override
            public void onItemClick(Group group) {
                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
                intent.putExtra("group",group);
                startActivity(intent);
            }
        });

        if (!Utils.checkPermissions(this)) {
            Utils.requestPermissions(this);
        }



    }

    private void getGroups() {

        db.collection(GROUPS).whereArrayContains("users",mAuth.getUid()).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (queryDocumentSnapshots!=null){
                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots){
                                Group group = querySnapshot.toObject(Group.class);
                                groupList.add(group);
                            }
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    private void setRecyclerview(RecyclerView recyclerView) {
        groupList = new ArrayList<>();
        // specify an adapter (see also next example)
        mAdapter = new GroupAdapter(groupList);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        if(Utils.getRequestingLocationUpdates(this)) {
//            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//            createLocationRequest();
//            Utils.setRequestingLocationUpdates(this, true);
//            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
//        }


    }

//
//    private void createLocationRequest() {
//        mLocationRequest = new LocationRequest();
//
//        // Sets the desired interval for active location updates. This interval is
//        // inexact. You may not receive updates at all if no location sources are available, or
//        // you may receive them slower than requested. You may also receive updates faster than
//        // requested if other applications are requesting location at a faster interval.
//        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
//        // less frequently than this interval when the app is no longer in the foreground.
//        mLocationRequest.setInterval(UPDATE_INTERVAL);
//
//        // Sets the fastest rate for active location updates. This interval is exact, and your
//        // application will never receive updates faster than this value.
//        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
//
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        // Sets the maximum time when batched location updates are delivered. Updates may be
//        // delivered sooner than this interval.
//        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
//    }
//
//    private PendingIntent getPendingIntent() {
//        // Note: for apps targeting API level 25 ("Nougat") or lower, either
//        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
//        // location updates. For apps targeting API level O, only
//        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
//        // started in the background in "O".
//
//        // TODO(developer): uncomment to use PendingIntent.getService().
////        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
////        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
////        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Intent intent = new Intent(this, LocationUpdatesBroadcastReceiver.class);
//        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    }
//

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
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_logout:
                mAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
