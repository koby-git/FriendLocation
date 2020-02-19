package com.koby.friendlocation;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;

import java.util.prefs.PreferenceChangeEvent;

//import static com.koby.friendlocation.activities.MainActivity.FASTEST_UPDATE_INTERVAL;
//import static com.koby.friendlocation.activities.MainActivity.MAX_WAIT_TIME;
//import static com.koby.friendlocation.activities.MainActivity.UPDATE_INTERVAL;

public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;
    FirebaseAuth mAuth;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        mAuth = FirebaseAuth.getInstance();
//        SwitchPreferenceCompat notificationsSwitchPreference = findPreference("notifications");
        SwitchPreferenceCompat privacySwitchPreference = findPreference("privacy");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
//        createLocationRequest();


//        notificationsSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                if ((Boolean)newValue){
//                    requestNotificationLocationUpdates();
//                    removeLocationUpdates();
//                }else {
//                    requestLocationUpdates();
//                    removeNotificationLocationUpdates();
//                }
//                return true;
//            }
//        });

        privacySwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if ((Boolean)newValue) {
//                    removeNotificationLocationUpdates();
                    removeLocationUpdates();
                }else {
                    Utils.setRequestingLocationUpdates(getContext(),true);
                }
                return true;
            }
        });
    }

//    private void removeNotificationLocationUpdates() {
//        Log.i(TAG, "Removing location updates");
//        Utils.setRequestingLocationUpdates(getContext(), false);
//        fusedLocationClient.removeLocationUpdates(getPendingIntentNotification());
//
//    }

//    private void requestNotificationLocationUpdates() {
//
//        try {
//            Log.i(TAG, "Starting location updates");
//            Utils.setRequestingLocationUpdates(getContext(), true);
//            fusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntentNotification());
//        } catch (SecurityException e) {
//            Utils.setRequestingLocationUpdates(getContext(), false);
//            e.printStackTrace();
//        }
//    }

    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        Utils.setRequestingLocationUpdates(getContext(), false);
        fusedLocationClient.removeLocationUpdates(getPendingIntent());
    }

//    private void createLocationRequest() {
//        mLocationRequest = new LocationRequest();
//
//        // Sets the desired interval for active location updates. This interval is
//        // inexact. You may not receive updates at all if no location sources are available, or
//        // you may receive them slower than requested. You may also receive updates faster than
//        // requested if other applications are requesting location at a faster interval.
//        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
//        // less frequently than this interval when the app is no longer in the foreground.
//        mLocationRequest.setInterval(UPDATE_INTERVAL);
//
//        // Sets the fastest rate for active location updates. This interval is exact, and your
//        // application will never receive updates faster than this value.
//        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
//
//        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//
//        // Sets the maximum time when batched location updates are delivered. Updates may be
//        // delivered sooner than this interval.
//        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
//    }

//    public void requestLocationUpdates() {
//        try {
//            Log.i(TAG, "Starting location updates");
//            Utils.setRequestingLocationUpdates(getContext(), true);
//            fusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
//        } catch (SecurityException e) {
//            Utils.setRequestingLocationUpdates(getContext(), false);
//            e.printStackTrace();
//        }
//    }

    private PendingIntent getPendingIntent() {

        Intent intent = new Intent(getContext(), LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

//    private PendingIntent getPendingIntentNotification() {
//        // Note: for apps targeting API level 25 ("Nougat") or lower, either
//        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
//        // location updates. For apps targeting API level O, only
//        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
//        // started in the background in "O".
//
//        // TODO(developer): uncomment to use PendingIntent.getService().
////        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
////        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
////        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        Intent intent = new Intent(getContext(), NotificationLocationUpdatesBroadcastReceiver.class);
//        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
//        return PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    @Override
    public void onStart() {
        super.onStart();
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onStop();
    }


}
