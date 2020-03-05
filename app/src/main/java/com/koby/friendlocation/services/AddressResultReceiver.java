package com.koby.friendlocation.services;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.koby.friendlocation.classes.constant.LocationConstants;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.koby.friendlocation.classes.constant.FirebaseConstants.USERS;

public class AddressResultReceiver extends ResultReceiver {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public AddressResultReceiver(Handler handler) {
        super(handler);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {

        if (resultData == null) {
            return;
        }

        // Display the address string
        // or an error message sent from the intent service.
        String addressOutput = resultData.getString(LocationConstants.RESULT_DATA_KEY);
        if (addressOutput == null) {
            addressOutput = "";
        }

//            displayAddressOutput();

        // Show a toast message if an address was found.
        if (resultCode == LocationConstants.SUCCESS_RESULT) {
//                showToast(getString(R.string.address_found));

            SimpleDateFormat formatter = new SimpleDateFormat("dd MMMM yyyy hh:mm:ss", Locale.getDefault());

            final Map map = new HashMap();
            map.put("address", addressOutput);
            map.put("date", formatter.format(Calendar.getInstance().getTime()));

//            Contact contact = new Contact();
//
//            contact.setAddress(addressOutput);
//            contact.setDate(formatter.format(Calendar.getInstance().getTime()));
//            contact.setName(mAuth.getCurrentUser().getEmail());
//
//            contactsAdapter.notifyDataSetChanged();

            db.collection(USERS).document(mAuth.getUid()).set(map, SetOptions.merge());

//            if (!contacts.isEmpty()) {
//                for (Contact c : contacts) {
//                    if (c.getName().equals(mAuth.getCurrentUser().getEmail())) {
//                        c.setAddress(addressOutput);
//                    }
//                }

            }
        }
    }

