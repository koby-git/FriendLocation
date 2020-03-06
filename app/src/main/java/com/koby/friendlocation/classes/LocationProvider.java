package com.koby.friendlocation.classes;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.koby.friendlocation.services.LocationUpdatesBroadcastReceiver;
import com.koby.friendlocation.utils.Utils;

public class LocationProvider {

    private static final String TAG = LocationProvider.class.getName();
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL = 500; // Every 5 seconds.

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    public static final long FASTEST_UPDATE_INTERVAL = 300; // Every 3 seconds

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5; // Every 25 seconds.

    public static LocationProvider ourInstance = null;

    private final Context mContext;

     /**
     * Provides access to the Fused Location Provider API.
     */
    public FusedLocationProviderClient mFusedLocationClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    public LocationRequest mLocationRequest;

    public static LocationProvider getInstance(Context context) {
        if(ourInstance== null)
        {
            ourInstance= new LocationProvider(context);
        }
        return ourInstance;
    }

    private LocationProvider(Context context) {
        this.mContext = context;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        createLocationRequest();

    }

    public void requestLocationUpdates() {
        try {
            Log.i(TAG, "Starting location updates");
            Utils.setRequestingLocationUpdates(mContext, true);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent());
        } catch (SecurityException e) {
            Utils.setRequestingLocationUpdates(mContext, false);
            e.printStackTrace();
        }
    }

    public void removeLocationUpdates() {
        Log.i(TAG, "Removing location updates");
        Utils.setRequestingLocationUpdates(mContext, false);

        mFusedLocationClient.removeLocationUpdates(getPendingIntent());
    }

    private PendingIntent getPendingIntent() {

        Intent intent = new Intent(mContext, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    public void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);

        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


}
