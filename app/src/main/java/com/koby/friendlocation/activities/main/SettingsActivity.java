package com.koby.friendlocation.activities.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.koby.friendlocation.R;
import com.koby.friendlocation.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            Fragment settingsFragment = new SettingsFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.setting_container, settingsFragment).commit();
        }

    }
}
