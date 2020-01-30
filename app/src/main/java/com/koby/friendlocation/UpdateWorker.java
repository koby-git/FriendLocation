package com.koby.friendlocation;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.koby.friendlocation.activities.MapsActivity;
import com.koby.friendlocation.classes.LocationConstants;
import com.koby.friendlocation.model.LocationDoc;
import com.koby.friendlocation.services.AddressResultReceiver;
import com.koby.friendlocation.services.FetchAddressIntentService;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.koby.friendlocation.classes.FirebaseConstants.GROUPS;
import static com.koby.friendlocation.classes.FirebaseConstants.USERS;

public class UpdateWorker extends Worker {

    public static final String GROUP = "group";

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
//    private AddressResultReceiver resultReceiver;
    private Location mLastLocation;

    public UpdateWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        this.context = context;
//        resultReceiver = new AddressResultReceiver(new Handler());
    }

    @NonNull
    @Override
    public Result doWork() {
        setLocation();
        return Result.success();
    }


//    protected void startIntentService() {
//        Intent intent = new Intent(context, FetchAddressIntentService.class);
//        intent.putExtra(LocationConstants.RECEIVER, resultReceiver);
//        intent.putExtra(LocationConstants.LOCATION_DATA_EXTRA, mLastLocation);
//        context.startService(intent);
//    }

    private void setLocation() {


        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.app_name);
            String description = context.getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(context.getString(R.string.app_name), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.app_name))
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle("New Location Update")
                .setContentText("From worker")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle().bigText("From worker"));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1002, builder.build());


        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {


                            // Create the NotificationChannel, but only on API 26+ because
                            // the NotificationChannel class is new and not in the support library
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                CharSequence name = context.getString(R.string.app_name);
                                String description = context.getString(R.string.app_name);
                                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                                NotificationChannel channel = new NotificationChannel(context.getString(R.string.app_name), name, importance);
                                channel.setDescription(description);
                                // Register the channel with the system; you can't change the importance
                                // or other notification behaviors after this
                                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                                notificationManager.createNotificationChannel(channel);
                            }

                            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getString(R.string.app_name))
                                    .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                                    .setContentTitle("New Location Update")
                                    .setContentText("You are at " + getCompleteAddressString(location.getLatitude(), location.getLongitude()))
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText("You are at " + getCompleteAddressString(location.getLatitude(), location.getLongitude())));

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                            // notificationId is a unique int for each notification that you must define
                            notificationManager.notify(1001, builder.build());


                            mLastLocation = location;
                            Map mapLocation = new HashMap();
//                            startIntentService();
                            mapLocation.put("latitude", location.getLatitude());
                            mapLocation.put("longitude" , location.getLongitude());

                            db.collection(USERS).document(user.getUid())
                                    .set(mapLocation,SetOptions.merge());
                        }
                    }
                });
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
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
}
