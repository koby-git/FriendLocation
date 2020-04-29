package com.koby.friendlocation.repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
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

    FirebaseAuth mAuth;
    FirebaseFirestore db;
//    WriteBatch batch;

    //Constructor
    @Inject
    public FirebaseRepository(FirebaseFirestore db,FirebaseAuth mAuth) {
        this.db = db;
        this.mAuth = mAuth;
//        this.batch = db.batch();
    }

    //Set
    public TaskResult setGroup(String groupName,Uri imageUri) {

        WriteBatch batch = db.batch();
        Group group = new Group();

        //Create empty group and get the generated document uid
        FirebaseFirestore.getInstance().collection(GROUPS).add(group)
                .addOnSuccessListener(documentReference -> {

                    group.setUid(documentReference.getId());
                    group.setName(groupName);
                    group.setInviteCode(documentReference.getId());
                    if (imageUri!=null){
                        uploadGroupImage(imageUri,documentReference.getId());
                    }

                    //Write in groups collection
                    DocumentReference userNamesDR = FirebaseFirestore.getInstance().collection(GROUPS).document(group.getUid());
                    batch.update(userNamesDR, "userNames", FieldValue.arrayUnion(getCurrentUser().getDisplayName()));

                    DocumentReference groupDetailsDR = db.collection(GROUPS).document(documentReference.getId());
                    batch.set(groupDetailsDR,group,SetOptions.merge());

                    DocumentReference usersUidDR = FirebaseFirestore.getInstance().collection(GROUPS).document(group.getUid());
                    batch.update(usersUidDR, "users", FieldValue.arrayUnion(getCurrentUser().getUid()));

                    //Write in users collection
                    DocumentReference groupsUidDR = FirebaseFirestore.getInstance().collection(USERS).document(getCurrentUser().getUid());
                    batch.update(groupsUidDR, "groupsUid", FieldValue.arrayUnion(group.getUid()));

                    DocumentReference groups = FirebaseFirestore.getInstance().collection(USERS).document(getCurrentUser().getUid());
                    batch.update(groups, "groups", FieldValue.arrayUnion(group));

                    batch.commit().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            batchCompleteListener.onComplete(task);
                        }
                    });
                });
        return new TaskResult();
    }
    public TaskResult setUserProfileName(String userProfileName){
        Map<String,String> map = new HashMap();
        map.put("name",userProfileName);
        db.collection(USERS).document(mAuth.getUid()).set(map,SetOptions.merge());

        //Update name in firebaseAuth
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(userProfileName).build();

        mAuth.getCurrentUser().updateProfile(profileChangeRequest)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User profile updated.");
                        if (batchCompleteListener != null) {
                            batchCompleteListener.onComplete(task);
                        }
                    }
                });

        return new TaskResult();

    }
    public void setUserProfileImage(Uri contentUri){
        //Update user db
        Map<String,String> imageMap = new HashMap<>();
        imageMap.put("imageUri",contentUri.toString());
        db.collection(USERS).document(getCurrentUser().getUid()).set(imageMap, SetOptions.merge());
    }

    //Add
    public TaskResult addGroupUser(Group group) {

        WriteBatch batch = db.batch();

        //Write in groups collection
        DocumentReference usersUid = db.collection(GROUPS).document(group.getUid());
        batch.update(usersUid, "users", FieldValue.arrayUnion(getCurrentUser().getUid()));

        //Write in users collection
        DocumentReference groupsUid = db.collection(USERS).document(getCurrentUser().getUid());
        batch.update(groupsUid, "groupsUid", FieldValue.arrayUnion(group.getUid()));

        DocumentReference groups = db.collection(USERS).document(getCurrentUser().getUid());
        batch.update(groups, "groups", FieldValue.arrayUnion(group));

        batch.commit().addOnCompleteListener(task -> {
            batchCompleteListener.onComplete(task);
        });

        return new TaskResult();
    }

    //Get
    public FirebaseUser getCurrentUser(){
        return mAuth.getCurrentUser();
    }
    public FirestoreRecyclerOptions<Group> getGroupsOptions() {
        Query query =  db.collection(GROUPS).whereArrayContains("users", mAuth.getUid());
        return new FirestoreRecyclerOptions.Builder<Group>()
                .setQuery(query, Group.class)
                .build();
    }
    public Query getUsersQuery(String groupUid) {
        return db.collection(USERS).whereArrayContains("groupsUid",groupUid);
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
    public DocumentReference getGroupDocumentReference(String groupUid) {
        return db.collection(GROUPS).document(groupUid);
    }

    //Update
    private void updateGroupProfileImage(String groupUid, Uri groupImage) {
        Map<String, String> map = new HashMap();
        map.put("image", groupImage.toString());

        db.collection(GROUPS).document(groupUid).set(map,SetOptions.merge());
    }

    private void updateUserProfileImage(Uri userImage){
        Map<String, String> imageMap = new HashMap();
        imageMap.put("imageUri", userImage.toString());

        db.collection(USERS).document(getCurrentUser().getUid()).set(imageMap,SetOptions.merge());

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(userImage)
                .build();

        getCurrentUser().updateProfile(profileUpdates);

    }

    //Delete
    public TaskResult deleteGroup(Group group) {

        WriteBatch batch = db.batch();

        //Write in groups collection
        DocumentReference usersUidDR = FirebaseFirestore.getInstance().collection(GROUPS).document(group.getUid());
        batch.update(usersUidDR, "users", FieldValue.arrayRemove(getCurrentUser().getUid()));

        //Write in users collection
        DocumentReference groupsUidDR = FirebaseFirestore.getInstance().collection(USERS).document(getCurrentUser().getUid());
        batch.update(groupsUidDR, "groupsUid", FieldValue.arrayRemove(group.getUid()));

        DocumentReference groups = FirebaseFirestore.getInstance().collection(USERS).document(getCurrentUser().getUid());
        batch.update(groups, "groups", FieldValue.arrayUnion(group));

        batch.commit().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (batchCompleteListener != null) {
                    batchCompleteListener.onComplete(task);
                }
            }
        });

        return new TaskResult();

    }

    //Upload
    public TaskResult uploadUserImage(Uri contentUri) {

        final StorageReference ref = FirebaseStorage.getInstance().getReference().child(USERS + "/" +getCurrentUser().getUid()+".jpg");
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
        return new TaskResult();
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
