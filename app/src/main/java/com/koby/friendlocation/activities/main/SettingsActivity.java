package com.koby.friendlocation.activities.main;

import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.koby.friendlocation.R;
import com.koby.friendlocation.fragments.settings.SettingsFragment;

import dagger.android.support.DaggerAppCompatActivity;

public class SettingsActivity extends DaggerAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (savedInstanceState == null) {
            getSupportActionBar().setTitle(R.string.settings);
            Fragment settingsFragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.setting_container, settingsFragment).commit();
        }

    }
}
