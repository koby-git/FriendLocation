package com.koby.friendlocation;

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

public class LocationProviderSingleton {

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL = 6000; // Every 60 seconds.

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    public static final long FASTEST_UPDATE_INTERVAL = 3000; // Every 30 seconds

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 5; // Every 5 minutes.


    public static LocationProviderSingleton ourInstance = null;

    private static final String TAG = LocationProviderSingleton.class.getName();
    private final Context mContext;

     /**
     * Provides access to the Fused Location Provider API.
     */
    public FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the Location Settings API.
     */
    public SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    public LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    public LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for Location events.
     */
    public LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
    public Location mCurrentLocation;
    private String mLongitudeLabel;
    private String mLastUpdateTimeLabel;

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    public Boolean mRequestingLocationUpdates;

    /**
     * Time when the location was updated represented as a String.
     */
    public String mLastUpdateTime;

    public static LocationProviderSingleton getInstance(Context context) {
        if(ourInstance== null)
        {
            ourInstance= new LocationProviderSingleton(context);
        }
        return ourInstance;
    }

    private LocationProviderSingleton(Context context) {
        this.mContext = context;
        mRequestingLocationUpdates = false;
        mLastUpdateTime = "";
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        mSettingsClient = LocationServices.getSettingsClient(context);
        createLocationRequest();
        buildLocationSettingsRequest();

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

    public void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
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
