package com.koby.friendlocation.activities.main;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koby.friendlocation.R;
import com.koby.friendlocation.classes.model.Group;

import java.io.File;
import java.util.UUID;

import static com.koby.friendlocation.CameraProvider.REQUSET_PHOTO_FROM_GALLERY;
import static com.koby.friendlocation.classes.constant.FirebaseConstants.GROUPS;
import static com.koby.friendlocation.classes.constant.FirebaseConstants.USERS;

public class AddGroupActivity extends AppCompatActivity {

    private static final int REQUEST_OK = 2;
    EditText groupNameEditText;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FloatingActionButton profileFab;
    FirebaseStorage mStorageRef = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        profileFab = findViewById(R.id.profile_fab);
        groupNameEditText = findViewById(R.id.group_name_et);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        profileFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, REQUSET_PHOTO_FROM_GALLERY);
                }else {
                    ActivityCompat.requestPermissions(AddGroupActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_OK);
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_OK && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, REQUSET_PHOTO_FROM_GALLERY);

        } else {
            Toast.makeText(this, "please provide permission", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUSET_PHOTO_FROM_GALLERY && resultCode == RESULT_OK && data != null) {

            // Let's read picked image data - its URI
            Uri pickedImage = data.getData();
            // Let's read picked image path using content resolver
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            File f = new File(imagePath);
            Uri contentUri = Uri.fromFile(f);

            final StorageReference ref = mStorageRef.getReference().child("users/" +mAuth.getCurrentUser().getUid()+".jpg");
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
                        Uri downloadUri = task.getResult();
                        updateGroup(downloadUri);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });
            cursor.close();
        }
    }

    private void updateGroup(Uri downloadUri) {

//        db.collection(GROUPS).document()
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.group_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.action_done:
                String groupName = groupNameEditText.getText().toString().trim();

                if (groupName.isEmpty()){
                    groupNameEditText.setError("נא להקליד את שם הקבוצה");
                }else {

                    WriteBatch batch = db.batch();

                    String groupInviteCode = UUID.randomUUID().toString().substring(0,6);
                    String groupUid = UUID.randomUUID().toString();
                    Group group = new Group(groupName,groupUid,groupInviteCode);
                    group.addUser(mAuth.getUid());

                    DocumentReference groupRef = db.collection(GROUPS).document(groupUid);
                    DocumentReference userRef = db.collection(USERS).document(mAuth.getUid());

                    batch.set(groupRef,group);
                    batch.update(userRef,"groupsUid",FieldValue.arrayUnion(groupUid));

                    batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(AddGroupActivity.this, "קבוצה חדשה נוצרה", Toast.LENGTH_SHORT).show();
//                                startActivity(new Intent(AddGroupActivity.this, MainActivity.class));
                                finish();
                            }else {
                                Toast.makeText(AddGroupActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
        }
        return super.onOptionsItemSelected(item);
    }
}
