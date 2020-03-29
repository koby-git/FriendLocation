package com.koby.friendlocation.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.google.firebase.auth.FirebaseAuth;
import com.koby.friendlocation.R;
import com.koby.friendlocation.activities.auth.LoginActivity;
import com.koby.friendlocation.providers.LocationProvider;
import com.koby.friendlocation.utils.Utils;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import dagger.android.support.AndroidSupportInjection;

import static android.app.Activity.RESULT_OK;
import static com.koby.friendlocation.providers.CameraProvider.REQUSET_PHOTO_FROM_GALLERY;

public class SettingsFragment extends PreferenceFragmentCompat implements HasAndroidInjector {

    @Inject
    DispatchingAndroidInjector<Object> androidInjector;

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }

    @Inject
    LocationProvider locationProvider;

    @Inject
    FirebaseAuth mAuth;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        SwitchPreference privacySwitchPreference = findPreference("location");

        Preference logoutPreference = findPreference("logout");
        logoutPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                //Sign out
                mAuth.signOut();
                //Remove location tracker
                locationProvider.removeLocationUpdates();
                //Move to login activity
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
                return false;
            }
        });

        privacySwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean)newValue) {
                System.out.println("location enabled");
                Utils.setRequestingLocationUpdates(getContext(),true);
                locationProvider.requestLocationUpdates();
            }else {
                System.out.println("location disable");
                Utils.setRequestingLocationUpdates(getContext(),false);
                locationProvider.removeLocationUpdates();
            }
            return true;
        });
    }


}
