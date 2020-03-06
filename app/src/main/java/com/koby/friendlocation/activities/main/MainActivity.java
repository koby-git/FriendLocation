package com.koby.friendlocation.activities.main;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.koby.friendlocation.activities.maps.MapsActivity;
import com.koby.friendlocation.classes.adapter.FirestoreUiGroupAdapter;
import com.koby.friendlocation.classes.LocationProvider;
import com.koby.friendlocation.R;
import com.koby.friendlocation.classes.model.Group;
import com.koby.friendlocation.classes.adapter.GroupAdapter;
import com.koby.friendlocation.activities.auth.LoginActivity;

import com.koby.friendlocation.repository.FirebaseRepository;
import com.koby.friendlocation.utils.Utils;

import javax.inject.Inject;

import dagger.android.support.DaggerAppCompatActivity;

public class MainActivity extends DaggerAppCompatActivity {

    @Inject
    FirebaseAuth mAuth;

    @Inject
    FirebaseRepository firebaseRepository;

    @Inject
    FirebaseFirestore db;

    private RecyclerView recyclerView;
    private FirestoreUiGroupAdapter mAdapter;
    private ExtendedFloatingActionButton createFab;
    private LocationProvider locationProviderSingleton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Check if permission granted
        // if permission not granted - request permission
        if (!Utils.checkPermissions(MainActivity.this)) {
            Utils.requestPermissions(MainActivity.this);
        }

        //Init LocationProvider
        locationProviderSingleton = LocationProvider.getInstance(MainActivity.this);

        //UI Element
        createFab = findViewById(R.id.main_fab_create);
        recyclerView = findViewById(R.id.main_recycler_view);

        setRecyclerView();

        //Create group
        createFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddGroupActivity.class));
            }
        });

        //Choose group
        mAdapter.setOnItemClickListener(new GroupAdapter.onItemClickListener() {
            @Override
            public void onItemClick(Group group) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("group",group);
                startActivity(intent);
            }
        });

        //First time request
        if(Utils.getRequestingLocationUpdates(MainActivity.this)){
            locationProviderSingleton.requestLocationUpdates();
        }
    }

    //set FirestoreUI recycler view
    private void setRecyclerView() {

        FirestoreRecyclerOptions<Group> options  = firebaseRepository.getGroupsOptions();

        mAdapter = new FirestoreUiGroupAdapter(options);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
    }

    //Inflate Options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //Handle Options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_setting:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_logout:

                //Sign out
                mAuth.signOut();
                //Remove location tracker
                locationProviderSingleton.removeLocationUpdates();
                //Move to login activity
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Start listening to group database
    @Override
    protected void onStart() {
        super.onStart();

        mAdapter.startListening();
    }

    //Stop listening to group database
    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

}
