package com.koby.friendlocation.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.koby.friendlocation.R;
import com.koby.friendlocation.utils.Utils;
import com.koby.friendlocation.classes.LocationProviderSingleton;

public class SettingsFragment extends PreferenceFragmentCompat{

    private LocationProviderSingleton locationProviderSingleton;
    FirebaseAuth mAuth;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        mAuth = FirebaseAuth.getInstance();
        SwitchPreferenceCompat privacySwitchPreference = findPreference("privacy");
        locationProviderSingleton = LocationProviderSingleton.getInstance(getContext());

        privacySwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean)newValue) {
                Utils.setRequestingLocationUpdates(getContext(),false);
                locationProviderSingleton.removeLocationUpdates();
            }else {
                Utils.setRequestingLocationUpdates(getContext(),true);
                locationProviderSingleton.requestLocationUpdates();
            }
            return true;
        });
    }
}
