package com.koby.friendlocation.activities.main;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import com.koby.friendlocation.R;
import com.koby.friendlocation.fragments.profile.GroupProfileFragment;

public class AddGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        if (savedInstanceState == null) {
            Fragment groupProfileFragment = new GroupProfileFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.group_profile_container, groupProfileFragment).commit();
        }
    }
}
