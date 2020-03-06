package com.koby.friendlocation.repository;

import android.content.Context;
import android.location.Location;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.koby.friendlocation.classes.model.Group;
import com.koby.friendlocation.utils.Utils;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.koby.friendlocation.classes.constant.FirebaseConstants.GROUPS;
import static com.koby.friendlocation.classes.constant.FirebaseConstants.USERS;

public class FirebaseRepository {

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mAuth.getCurrentUser();
    FirebaseFirestore db;

    private static final FirebaseRepository ourInstance = new FirebaseRepository();
    public static FirebaseRepository getInstance() {
        return ourInstance;
    }


    private FirebaseRepository() { db = FirebaseFirestore.getInstance(); }

    public MutableLiveData<List<Group>> getGroups() {

        MutableLiveData<List<Group>> groups = new MutableLiveData<>();
        if (mAuth.getCurrentUser()!=null) {
            db.collection(GROUPS).whereArrayContains("users", mAuth.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            groups.setValue(queryDocumentSnapshots.toObjects(Group.class));
                        }
                    });
        }
        return groups;
    }

    public FirestoreRecyclerOptions<Group> getGroupsOptions() {

        Query query =  db.collection(GROUPS).whereArrayContains("users", mAuth.getUid());

        FirestoreRecyclerOptions<Group> options = new FirestoreRecyclerOptions.Builder<Group>()
                .setQuery(query, Group.class)
                .build();

        return options;
    }

    public void setLocationUpdatesResult(Context context, Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        Map mapLocation = new HashMap();


        mapLocation.put("address",Utils.getCompleteAddressString(context,latitude,longitude));
        mapLocation.put("date", DateFormat.getDateTimeInstance().format(new Date()));
        mapLocation.put("latitude", latitude);
        mapLocation.put("longitude" , longitude);

        db.collection(USERS).document(mAuth.getUid())
                .set(mapLocation, SetOptions.merge());
    }

    public Query getUsersQuery(Group group){
        return db.collection(USERS).whereArrayContains("groupsUid",group.getGroupUid());
    }

    public void setProfileUsername(String username){
        Map<String,String> map = new HashMap();
        map.put("name",username);
        db.collection(USERS).document(mAuth.getUid()).set(map,SetOptions.merge());
    }

    public void setProfileImage(Uri contentUri){
        //Update user db
        Map<String,String> imageMap = new HashMap<>();
        imageMap.put("imageUri",contentUri.toString());
        db.collection(USERS).document(firebaseUser.getUid()).set(imageMap, SetOptions.merge());
    }


}
