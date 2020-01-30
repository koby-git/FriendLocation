package com.koby.friendlocation.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.koby.friendlocation.R;
import com.koby.friendlocation.SettingsActivity;
import com.koby.friendlocation.classes.Group;
import com.koby.friendlocation.classes.GroupAdapter;
import com.koby.friendlocation.login.LoginActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        createFab = findViewById(R.id.main_fab_create);
        joinFab = findViewById(R.id.main_fab_join);
        groupList = new ArrayList<>();

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            spanCount = 6;
        }else {
            spanCount = 3;
        }

        recyclerView = findViewById(R.id.main_recycler_view);
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        layoutManager = new GridLayoutManager(this, spanCount);
        recyclerView.setLayoutManager(layoutManager);
        // specify an adapter (see also next example)
        mAdapter = new GroupAdapter(groupList);
        recyclerView.setAdapter(mAdapter);

        Map online = new HashMap();
        online.put("isOnline",true);
        db.collection(USERS).document(mAuth.getUid()).update(online);

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


//        DocumentReference userRef = db.collection(USERS).document(mAuth.getUid());
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
//        db.collection(USERS).document(mAuth.getUid())
//                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//
//                if (documentSnapshot.exists()) {
//                    if (documentSnapshot.get("groups")!=null) {
//                        ArrayList<Map> a = new ArrayList<>();
//                        a.addAll((Collection<? extends Map>) documentSnapshot.get("groups"));
//                        for (Map g : a ){
//                            Group group = new Group();
//                            group.setGroupInviteCode(g.get("groupInviteCode").toString());
//                            group.setGroupUid(g.get("groupUid").toString());
//                            group.setGroupName(g.get("groupName").toString());
//                            groupList.add(group);
//                        }
//                        mAdapter.notifyDataSetChanged();
//                    }
//                }
//            }
//        });

    }

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

//    @Override
//    public void applyName(String groupName) {
//        if (!groupName.trim().isEmpty()) {
//
//            ArrayList<DocumentReference> usersDocumentsArray = new ArrayList<>();
//
//            DocumentReference documentReference = db.collection(USERS).document(mAuth.getUid());
//            usersDocumentsArray.add(documentReference);
//            String groupInviteCode = UUID.randomUUID().toString().substring(0,6);
//
//            HashMap groupMap = new HashMap();
//
//            groupMap.put("groupName",groupName);
//            groupMap.put("groupInviteCode",groupInviteCode);
////            groupMap.put("users",mAuth.getUid());
////            groupMap.put("dr",usersDocumentsArray);
//
//            Group newGroup = new Group();
//
//            newGroup.setGroupName(groupName);
//            newGroup.setGroupInviteCode(groupInviteCode);
//
//            db.collection(GROUPS).add(groupMap)
//                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//                @Override
//                public void onComplete(@NonNull Task<DocumentReference> task) {
//                    if (task.isSuccessful()){
//
//                        Map map1 = new HashMap();
//                        newGroup.setGroupUid(task.getResult().getId());
////                        groupRefList.add();
//                        groupList.add(newGroup);
//                        map1.put("groups",groupList);
//
//                        mAdapter.notifyDataSetChanged();
//
//                        db.collection(USERS)
//                                .document(mAuth.getUid()).set(map1, SetOptions.merge());
//
//                        db.collection(USERS).document(mAuth.getUid()).update("groupsUid", FieldValue.arrayUnion(task.getResult().getId()));
//
//                    }
//                }
//            });
//
//
//
////            db.collection(GROUP +"/"+ groupName +"/"+ mAuth.getUid()).add(new HashMap<>());
//        }
//    }
}
