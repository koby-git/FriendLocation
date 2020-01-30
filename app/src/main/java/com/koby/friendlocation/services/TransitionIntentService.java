package com.koby.friendlocation.services;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.gms.location.ActivityTransitionEvent;
import com.google.android.gms.location.ActivityTransitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.koby.friendlocation.classes.LocationConstants;

import java.util.HashMap;
import java.util.Map;

import static com.koby.friendlocation.classes.FirebaseConstants.USERS;

public class TransitionIntentService extends IntentService {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FusedLocationProviderClient fusedLocationClient;
    Location lastLocation;
//    private AddressResultReceiver resultReceiver;

    public TransitionIntentService() {
        super("TransitionIntentService");
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
//        resultReceiver = new AddressResultReceiver(new Handler());

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            ActivityTransitionResult result = ActivityTransitionResult.extractResult(intent);
            for (ActivityTransitionEvent event : result.getTransitionEvents()) {
                // chronological sequence of events....

                String stateName ="";

                Map<String,String> state = new HashMap<>();
                switch (event.getActivityType()){
                    case DetectedActivity.IN_VEHICLE:
                        stateName = "IN_VEHICLE";
                        break;
                    case DetectedActivity.ON_BICYCLE:
                        stateName = "ON_BICYCLE";
                        break;
                    case DetectedActivity.RUNNING:
                        stateName = "RUNNING";
                        break;
                    case DetectedActivity.STILL:
                        startIntentService();
                        stateName = "STILL";
                        break;
                    case DetectedActivity.WALKING:
                        stateName = "WALKING";
                        break;
                }

                state.put("state",stateName);
                db.collection(USERS).document(mAuth.getUid()).set(state, SetOptions.merge());

            }
        }
    }

    protected void startIntentService() {


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location!=null){
//                    lastLocation = location;
//                    Intent intent = new Intent(getApplicationContext(), FetchAddressIntentService.class);
//                    intent.putExtra(LocationConstants.RECEIVER, resultReceiver);
//                    intent.putExtra(LocationConstants.LOCATION_DATA_EXTRA, lastLocation);
//                    startService(intent);
                }else {
                    System.out.println("shiittttttttt");
                    db.collection("shit");
                }
            }
        });

    }
}

