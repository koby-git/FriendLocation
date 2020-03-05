package com.koby.friendlocation;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koby.friendlocation.classes.model.Group;
import com.koby.friendlocation.classes.model.LocationDoc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;
import static com.koby.friendlocation.classes.constant.FirebaseConstants.GROUPS;
import static com.koby.friendlocation.classes.constant.FirebaseConstants.USERS;

public class FirebaseRepository {

    private static final FirebaseRepository ourInstance = new FirebaseRepository();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser firebaseUser = mAuth.getCurrentUser();


    public static FirebaseRepository getInstance() {
        return ourInstance;
    }

    FirebaseFirestore db;
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

    public void setUserLocation(Location location){
        db.collection(USERS).document(firebaseUser.getUid())
                .set(new LocationDoc(firebaseUser.getUid()
                        , location.getLatitude(), location.getLongitude()), SetOptions.merge());
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
