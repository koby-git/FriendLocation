package com.koby.friendlocation;

import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationUtils {

    private FusedLocationProviderClient fusedLocationClient;

    private static final LocationUtils ourInstance = new LocationUtils();

    public static LocationUtils getInstance() {
        return ourInstance;
    }

    private LocationUtils() {}

    public void InitFusedLocationProviderClient(Context context){

    }

//    private void get(){
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient();
//    }


}
