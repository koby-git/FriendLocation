package com.koby.friendlocation.repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koby.friendlocation.model.Contact;
import com.koby.friendlocation.model.Group;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



import static com.koby.friendlocation.constant.FirebaseConstants.GROUPS;
import static com.koby.friendlocation.constant.FirebaseConstants.USERS;

public class FirebaseRepository {

    private static String TAG = FirebaseRepository.class.getSimpleName();
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;

    private static final FirebaseRepository ourInstance = new FirebaseRepository();
    public static FirebaseRepository getInstance() {
        return ourInstance;
    }


    //Constructor
    private FirebaseRepository() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
    }

    //Get firestoreRecyclerOptions
    public FirestoreRecyclerOptions<Group> getGroupsOptions() {
        Query query =  db.collection(GROUPS).whereArrayContains("users", mAuth.getUid());
        FirestoreRecyclerOptions<Group> options = new FirestoreRecyclerOptions.Builder<Group>()
                .setQuery(query, Group.class)
                .build();

        return options;
    }

    public FirestoreRecyclerOptions<Contact> getUsersOptions(String groupUid){
        Query query = db.collection(USERS).whereArrayContains("groupsUid",groupUid);
        FirestoreRecyclerOptions<Contact> options = new FirestoreRecyclerOptions.Builder<Contact>()
                .setQuery(query, Contact.class)
                .build();
        return options;
    }
    public FirestoreRecyclerOptions<Contact> getUsersOptions(GoogleMap map, String groupUid){
        Query query = db.collection(USERS).whereArrayContains("groupsUid",groupUid);
        FirestoreRecyclerOptions<Contact> options = new FirestoreRecyclerOptions.Builder<Contact>()
                .setQuery(query, new SnapshotParser<Contact>() {
                    @NonNull
                    @Override
                    public Contact parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Contact contact = snapshot.toObject(Contact.class);

                        //Add member marker to map
                        if (map!=null) {
                            LatLng latLng = new LatLng(contact.getLatitude(), contact.getLongitude());
                            map.addMarker(new MarkerOptions().position(latLng).title(contact.getName()));
                        }
                        return contact;
                    }
                })
                .build();
        return options;
    }

    public Query getGroupsQuery(){
        return db.collection(GROUPS).whereArrayContains("users", mAuth.getUid());
    }
    public Query getUsersQuery(String groupUid) {
        return db.collection(USERS).whereArrayContains("groupsUid",groupUid);
    }

    //Set profiles
    public void setGroup(String groupName, Uri imagePathUri) {

        //Set group name
        Map<String, String> map = new HashMap();
        map.put("groupName", groupName);
        map.put("users",firebaseUser.getUid());

        //Set group document
        db.collection(GROUPS).add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                //Upload group image
                uploadGroupImage(imagePathUri,documentReference.getId());
            }
        });
    }
    public void setGroup(String groupName) {

        //Set group name
        Map map = new HashMap();
        map.put("groupName", groupName);

        //Set current user in this group
        db.collection(GROUPS).add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                ArrayList<String> usersUid = new ArrayList();
                usersUid.add(mAuth.getUid());

                Map usersUidMap = new HashMap();
                usersUidMap.put("user",usersUid);

                Map map2 = new HashMap();
                map2.put("group",documentReference);
                db.collection(USERS +"/"+ firebaseUser.getUid() +"/"+ GROUPS).add(map2);

            }
        });
    }

    public void setGroupProfileName(String groupProfileName) {

    }
    public void setGroupProfileImage(Uri imagePathUri,String groupUid){

//        Uri databaseImageUriRef = uploadUserImage(imagePathUri);
        Map<String,String> imageMap = new HashMap<>();
//        imageMap.put("imageUri",contentUri.toString());
        db.collection(GROUPS).document(firebaseUser.getUid()).set(imageMap, SetOptions.merge());
    }

    public void setUserProfileName(String userProfileName){
        Map<String,String> map = new HashMap();
        map.put("name",userProfileName);
        db.collection(USERS).document(mAuth.getUid()).set(map,SetOptions.merge());
    }
    public void setUserProfileImage(Uri contentUri){
        //Update user db
        Map<String,String> imageMap = new HashMap<>();
        imageMap.put("imageUri",contentUri.toString());
        db.collection(USERS).document(firebaseUser.getUid()).set(imageMap, SetOptions.merge());
    }


    //Upload profile image
    public void uploadUserImage(Uri contentUri) {

        System.out.println("111111111111111");
        final StorageReference ref = FirebaseStorage.getInstance().getReference().child(USERS + "/" +firebaseUser.getUid()+".jpg");
        UploadTask uploadTask = ref.putFile(contentUri);

        //Get firebase storage image uri
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    updateUserProfileImage(task.getResult());
                } else {
                    // Handle failures
                    // ...
                    Log.i(TAG,task.getException().toString());
                }
            }
        });
    }
    public void uploadGroupImage(Uri contentUri,String groupUid){

        final StorageReference ref = FirebaseStorage.getInstance().getReference().child(GROUPS + "/" + groupUid +".jpg");
        UploadTask uploadTask = ref.putFile(contentUri);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    updateGroupProfileImage(groupUid,task.getResult());
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    private void updateGroupProfileImage(String groupUid, Uri groupImage) {
        Map<String, String> map = new HashMap();
        map.put("groupImage", groupImage.toString());

        db.collection(GROUPS).document(groupUid).set(map,SetOptions.merge());
    }
    private void updateUserProfileImage(Uri userImage){
        Map<String, String> map = new HashMap();
        map.put("imageUri", userImage.toString());

        db.collection(USERS).document(firebaseUser.getUid()).set(map,SetOptions.merge());

        System.out.println("What the fffff");
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(userImage)
                .build();

        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User profile updated.");
                        }
                    }
                });
    }


}
