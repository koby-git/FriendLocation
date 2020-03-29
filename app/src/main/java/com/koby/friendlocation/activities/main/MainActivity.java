package com.koby.friendlocation.activities.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.koby.friendlocation.activities.maps.MapsActivity;
import com.koby.friendlocation.view.adapter.FirestoreUiGroupAdapter;
import com.koby.friendlocation.R;
import com.koby.friendlocation.model.Group;
import com.koby.friendlocation.view.adapter.GroupAdapter;

import com.koby.friendlocation.providers.LocationProvider;
import com.koby.friendlocation.repository.FirebaseRepository;
import com.koby.friendlocation.utils.Utils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.android.support.DaggerAppCompatActivity;

public class MainActivity extends DaggerAppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    @Inject FirebaseRepository firebaseRepository;
    @Inject LocationProvider locationProvider;

    @BindView(R.id.main_recycler_view) RecyclerView recyclerView;

    private FirestoreUiGroupAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        //Check if permission granted
        // if permission not granted - request permission
        if (!Utils.checkPermissions(MainActivity.this)) {
            Utils.requestPermissions(MainActivity.this);
        }

        setGroupRecyclerView();

        //First time request
        if (Utils.getRequestingLocationUpdates(MainActivity.this)) {
            locationProvider.requestLocationUpdates();
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");

            } else if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                    (grantResults[1] == PackageManager.PERMISSION_GRANTED)
            ) {
                // Permission was granted.
                locationProvider.requestLocationUpdates();

            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Toast.makeText(this, "dont have permission", Toast.LENGTH_SHORT).show();
            }
        }
    }
    //Create group fab
    @OnClick(R.id.main_fab_create)
    public void crateGroup() {
        startActivity(new Intent(MainActivity.this, AddGroupActivity.class));
    }

    //set FirestoreUI recycler view
    private void setGroupRecyclerView() {

        mAdapter = new FirestoreUiGroupAdapter(firebaseRepository.getGroupsOptions(),this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        //Choose group
        mAdapter.setOnItemClickListener(new GroupAdapter.onItemClickListener() {
            @Override
            public void onItemClick(Group group) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("group", group);
                startActivity(intent);
            }
        });

        //Zoom group image
        mAdapter.setImageClickListener(new GroupAdapter.onImageClickListener() {
            @Override
            public void onImageClick(String imageUri) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                View view =inflater.inflate(R.layout.dialog_image, null);
                ImageView imageView = view.findViewById(R.id.dialog_image);
                Glide.with(MainActivity.this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_delete_forever)
                        .fallback(R.drawable.ic_group_grey)
                        .centerCrop()
                        .into(imageView);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setView(view);
                builder.show();
            }
        });
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

                default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Start listening to group firestoreUi
    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    //Stop listening to group firestoreUi
    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

}