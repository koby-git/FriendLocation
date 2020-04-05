package com.koby.friendlocation.activities.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import com.koby.friendlocation.R;
import com.koby.friendlocation.fragments.profile.GroupProfileFragment;

import dagger.android.support.DaggerAppCompatActivity;

public class CreateGroupActivity extends DaggerAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        getSupportActionBar().setTitle("קבוצה חדשה");
        if (savedInstanceState == null) {
            Fragment groupProfileFragment = new GroupProfileFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.group_profile_container, groupProfileFragment).commit();
        }
    }
}
