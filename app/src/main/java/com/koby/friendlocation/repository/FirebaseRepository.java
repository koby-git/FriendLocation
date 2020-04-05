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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koby.friendlocation.model.Contact;
import com.koby.friendlocation.model.Group;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.koby.friendlocation.constant.FirebaseConstants.GROUPS;
import static com.koby.friendlocation.constant.FirebaseConstants.USERS;

@Singleton
public class FirebaseRepository {

    private static String TAG = FirebaseRepository.class.getSimpleName();
    OnBatchCompleteListener batchCompleteListener;
    OnTaskCompleteListener taskCompleteListener;
    public FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseFirestore db;
    WriteBatch batch;

//    private static final FirebaseRepository ourInstance = new FirebaseRepository();
//    public static FirebaseRepository getInstance() {
//        return ourInstance;
//    }

    //Constructor
    @Inject
    public FirebaseRepository(FirebaseFirestore db,FirebaseAuth mAuth) {
        this.db = db;
        this.mAuth = mAuth;

        firebaseUser = mAuth.getCurrentUser();
        batch = db.batch();
    }

    public TaskResult getGroup(String groupUid) {
        db.collection(GROUPS)
                .document(groupUid)
                .get()
                .addOnCompleteListener(task -> {
                    taskCompleteListener.onComplete(task);
                });
        return new TaskResult();
    }
    public TaskResult addGroupUser(Group group) {

        //Write in groups collection
        DocumentReference usersUid = db.collection(GROUPS).document(group.getGroupUid());
        batch.update(usersUid, "users", FieldValue.arrayUnion(firebaseUser.getUid()));

        //Write in users collection
        DocumentReference groupsUid = db.collection(USERS).document(firebaseUser.getUid());
        batch.update(groupsUid, "groupsUid", FieldValue.arrayUnion(group.getGroupUid()));

        DocumentReference groups = db.collection(USERS).document(firebaseUser.getUid());
        batch.update(groups, "groups", FieldValue.arrayUnion(group));

        batch.commit().addOnCompleteListener(task -> {
            batchCompleteListener.onComplete(task);
        });

        return new TaskResult();
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

    public TaskResult deleteGroup(Group group) {

        batch = FirebaseFirestore.getInstance().batch();

        //Write in groups collection
        DocumentReference usersUidDR = FirebaseFirestore.getInstance().collection(GROUPS).document(group.getGroupUid());
        batch.update(usersUidDR, "users", FieldValue.arrayRemove(firebaseUser.getUid()));

        //Write in users collection
        DocumentReference groupsUidDR = FirebaseFirestore.getInstance().collection(USERS).document(firebaseUser.getUid());
        batch.update(groupsUidDR, "groupsUid", FieldValue.arrayRemove(group.getGroupUid()));

        DocumentReference groups = FirebaseFirestore.getInstance().collection(USERS).document(firebaseUser.getUid());
        batch.update(groups, "groups", FieldValue.arrayUnion(group));

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                batchCompleteListener.onComplete(task);
            }
        });

        return new TaskResult();

    }

    //Set profiles
    public TaskResult setGroup(String groupName,Uri imageUri) {

        batch = FirebaseFirestore.getInstance().batch();
        Group group = new Group();

        //Create empty group and get the generated document uid
        FirebaseFirestore.getInstance().collection(GROUPS).add(group)
                .addOnSuccessListener(documentReference -> {

                    group.setGroupUid(documentReference.getId());
                    group.setGroupName(groupName);
                    group.setGroupInviteCode(documentReference.getId());
                    if (imageUri!=null){
                        group.setGroupImage(imageUri.toString());
                    }

                    //Write in groups collection
                    DocumentReference userNamesDR = FirebaseFirestore.getInstance().collection(GROUPS).document(group.getGroupUid());
                    batch.update(userNamesDR, "userNames", FieldValue.arrayUnion(firebaseUser.getDisplayName()));

                    DocumentReference groupDetailsDR = db.collection(GROUPS).document(documentReference.getId());
                    batch.set(groupDetailsDR,group,SetOptions.merge());

                    DocumentReference usersUidDR = FirebaseFirestore.getInstance().collection(GROUPS).document(group.getGroupUid());
                    batch.update(usersUidDR, "users", FieldValue.arrayUnion(firebaseUser.getUid()));

                    //Write in users collection
                    DocumentReference groupsUidDR = FirebaseFirestore.getInstance().collection(USERS).document(firebaseUser.getUid());
                    batch.update(groupsUidDR, "groupsUid", FieldValue.arrayUnion(group.getGroupUid()));

                    DocumentReference groups = FirebaseFirestore.getInstance().collection(USERS).document(firebaseUser.getUid());
                    batch.update(groups, "groups", FieldValue.arrayUnion(group));

                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                batchCompleteListener.onComplete(task);
                            }
                        }
                    });
                });
        return new TaskResult();
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

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(userImage)
                .build();

        firebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated.");
                    }
                });
    }

    public DocumentReference getGroupDocumentReference(String groupUid) {
        return db.collection(GROUPS).document(groupUid);
    }

    //Return object
    public class TaskResult {

        public void addOnTaskCompleteListener(OnBatchCompleteListener clistener){
            batchCompleteListener = clistener;
        }

        public void addOnTaskCompleteListener(OnTaskCompleteListener clistener){
            taskCompleteListener = clistener;
        }

    }
    public interface OnBatchCompleteListener{
        void onComplete(@NonNull Task<Void> task);
    }
    public interface OnTaskCompleteListener{
        void onComplete(Task<DocumentSnapshot> task);
    }
}
