/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.koby.friendlocation;


import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.koby.friendlocation.activities.MainActivity;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.koby.friendlocation.classes.FirebaseConstants.USERS;

/**
 * Utility methods used in this sample.
 */
public  class Utils {

    final static String KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested";
    final static String KEY_LOCATION_UPDATES_RESULT = "location-update-result";
    final static String CHANNEL_ID = "channel_01";
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 45;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 41;

    public static void setRequestingLocationUpdates(Context context, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
                .apply();
    }

    public static boolean getRequestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false);
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    static void sendNotification(Context context, String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(context, MainActivity.class);

        notificationIntent.putExtra("from_notification", true);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle("Location update")
                .setContentText(notificationDetails)
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            // Create the channel for the notification
            NotificationChannel mChannel =
                    new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel);

            // Channel ID
            builder.setChannelId(CHANNEL_ID);
        }

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }


    /**
     * Returns the title for reporting about a list of {@link Location} objects.
     *
     * @param context The {@link Context}.
     */
    static String getLocationResultTitle(Context context, List<Location> locations) {
//        String numLocationsReported = context.getResources().getQuantityString(
//                R.plurals.num_locations_reported, locations.size(), locations.size());
//        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(new Date());

        Location mLocation = getLastLocation(locations);
        String locationResualtTitle = "You are at " + getCompleteAddressString(context,mLocation.getLatitude(), mLocation.getLongitude());
        return locationResualtTitle;
    }

    /**
     * Returns te text for reporting about a list of  {@link Location} objects.
     *
     * @param locations List of {@link Location}s.
     */
    private static String getLocationResultText(Context context, List<Location> locations) {
        if (locations.isEmpty()) {
            return "unknow";
        }
        StringBuilder sb = new StringBuilder();
        for (Location location : locations) {
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(", ");
            sb.append(location.getLongitude());
            sb.append(")");
            sb.append("\n");
        }
        return sb.toString();
    }


    static void setLocationUpdatesResult(Context context, List<Location> locations) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        int lastLocation = locations.size()-1;
        Map mapLocation = new HashMap();

        System.out.println(mAuth.getUid()+"666");
        System.out.println("latitude" + locations.get(lastLocation).getLatitude());
        System.out.println("longitude" + locations.get(lastLocation).getLongitude());
        mapLocation.put("latitude", locations.get(lastLocation).getLatitude());
        mapLocation.put("longitude" , locations.get(lastLocation).getLongitude());

        db.collection(USERS).document(mAuth.getUid())
                .set(mapLocation, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                System.out.println("777");
                Toast.makeText(context, "database  success", Toast.LENGTH_SHORT).show();
            }
        });


//        PreferenceManager.getDefaultSharedPreferences(context)
//                .edit()
//                .putString(KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(context, locations)
//                        + "\n" + getLocationResultText(context, locations))
//                .apply();
    }

    static String getLocationUpdatesResult(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_LOCATION_UPDATES_RESULT, "");
    }

    static String getCompleteAddressString(Context context, double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder();

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return strAdd;
    }

    static Location getLastLocation(List<Location> locations){
        int lastLocation = locations.size()-1;
        return locations.get(lastLocation);
    }

    public static Boolean checkPermissions(Context context){
            int fineLocationPermissionState = ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION);

        System.out.println("fine location: " + fineLocationPermissionState);

            int backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_BACKGROUND_LOCATION);

        System.out.println("background location: " + backgroundLocationPermissionState);

        System.out.println(PackageManager.PERMISSION_GRANTED );
            return (fineLocationPermissionState == PackageManager.PERMISSION_GRANTED) &&
                    (backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED);
    }

    public static Boolean checkFineLocationPermission(Context context){
         if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            return false;
         }else {
             return true;
         }
    }

    public static void requestFineLocationPermission(Activity activity){
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                , LOCATION_PERMISSION_REQUEST_CODE);
    };
            // Permission to access the location is missing

    public static void requestPermissions(Activity activity){
        ActivityCompat.requestPermissions(activity,
                new String[] {
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION },
                REQUEST_PERMISSIONS_REQUEST_CODE);
    }
}