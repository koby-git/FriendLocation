package com.koby.friendlocation.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
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
import com.google.firebase.firestore.Query;
import com.koby.friendlocation.FirebaseRepository;
import com.koby.friendlocation.classes.FirestoreUiGroupAdapter;
import com.koby.friendlocation.classes.LocationProviderSingleton;
import com.koby.friendlocation.R;
import com.koby.friendlocation.model.Group;
import com.koby.friendlocation.classes.GroupAdapter;
import com.koby.friendlocation.login.LoginActivity;

import java.util.ArrayList;

import com.koby.friendlocation.model.GroupListViewModel;
import com.koby.friendlocation.utils.Utils;

import static com.koby.friendlocation.classes.FirebaseConstants.GROUPS;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;

    //mvvm
//    private GroupAdapter mAdapter;

    //mvvm ui
    private FirestoreUiGroupAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Group> groupList;
    private FirebaseFirestore db;
    private GroupListViewModel groupListViewModel;
    private ExtendedFloatingActionButton createFab,joinFab;
    private FirebaseRepository firebaseRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!Utils.checkPermissions(MainActivity.this)) {
            Utils.requestPermissions(MainActivity.this);
        }

        //Init LocationProvider
        LocationProviderSingleton locationProviderSingleton = LocationProviderSingleton.getInstance(MainActivity.this);

        //Init Database
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseRepository = FirebaseRepository.getInstance();

        //UI Element
        createFab = findViewById(R.id.main_fab_create);
        joinFab = findViewById(R.id.main_fab_join);
        recyclerView = findViewById(R.id.main_recycler_view);

        //Mvvm with Firebase
        /*
            GroupListViewModel groupListViewModel = new ViewModelProvider(this).get(GroupListViewModel.class);
            groupListViewModel.getGroups().observe(this, groups -> {
                mAdapter.setGroupList((ArrayList<Group>) groups);
                mAdapter.notifyDataSetChanged();
            });
         */

        //Mvvm with Firestore Ui
        GroupListViewModel groupListViewModel = new ViewModelProvider(this).get(GroupListViewModel.class);

//        setRecyclerView();

//        groupListViewModel.getGroups().observe(this, groups -> {
//            mAdapter.setGroupList((ArrayList<Group>) groups);
//            mAdapter.notifyDataSetChanged();
//        });

        setFirestoreRecyclerView();



        createFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AddGroupActivity.class));
            }
        });

        joinFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, JoinGroupActivity.class));
            }
        });

//        mAdapter.setOnItemClickListener(new GroupAdapter.onItemClickListener() {
//            @Override
//            public void onItemClick(Group group) {
//                Intent intent = new Intent(MainActivity.this,MapsActivity.class);
//                intent.putExtra("group",group);
//                startActivity(intent);
//            }
//        });

        //First time request
        if(Utils.getRequestingLocationUpdates(MainActivity.this)){
            locationProviderSingleton.requestLocationUpdates();
        }
    }

    private void setFirestoreRecyclerView() {

        FirestoreRecyclerOptions<Group> options  = firebaseRepository.getGroupsOptions();

        mAdapter = new FirestoreUiGroupAdapter(options);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
    }

//    private void getGroups() {
//
//        db.collection(GROUPS).whereArrayContains("users",mAuth.getUid()).get()
//                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        if (queryDocumentSnapshots!=null){
//                            for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots){
//                                Group group = querySnapshot.toObject(Group.class);
//                                groupList.add(group);
//                                mAdapter.notifyDataSetChanged();
//                            }
//
//                        }
//                    }
//                });
//    }

//    private void setRecyclerview() {
//        groupList = new ArrayList<>();
//        // specify an adapter (see also next example)
//        mAdapter = new GroupAdapter(groupList);
//        recyclerView.setHasFixedSize(true);
//        // use a linear layout manager
//        layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setAdapter(mAdapter);
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

    private void setRecyclerView() {


//        mAdapter = new GroupAdapter();

        FirestoreRecyclerOptions<Group> options  = firebaseRepository.getGroupsOptions();

        mAdapter = new FirestoreUiGroupAdapter(options);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

}
