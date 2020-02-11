package com.koby.friendlocation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.LinearLayout;

public class SettingsActivity extends AppCompatActivity {

    public LinearLayout profileLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            Fragment settingsFragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.setting_container, settingsFragment).commit();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        Fragment settingsFragment = new SettingsFragment();
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager
//                .beginTransaction()
////                .addToBackStack(null) // your manage backstack here
//                .replace(R.id.setting_container, settingsFragment)
//                .commit();
//    }





}
