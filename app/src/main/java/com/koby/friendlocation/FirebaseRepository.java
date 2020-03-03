package com.koby.friendlocation;

import androidx.lifecycle.MutableLiveData;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.koby.friendlocation.model.Group;

import java.util.List;

import static com.koby.friendlocation.classes.FirebaseConstants.GROUPS;
import static com.koby.friendlocation.classes.FirebaseConstants.USERS;

public class FirebaseRepository {

    private static final FirebaseRepository ourInstance = new FirebaseRepository();
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public static FirebaseRepository getInstance() {
        return ourInstance;
    }

    FirebaseFirestore db;
    private FirebaseRepository() {

        db = FirebaseFirestore.getInstance();
    }

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
}
