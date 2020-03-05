package com.koby.friendlocation.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.koby.friendlocation.R;
import com.koby.friendlocation.classes.model.Contact;
import com.koby.friendlocation.classes.adapter.SettingContactsAdapter;

import java.util.ArrayList;
import java.util.Objects;

public class GroupSettingActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<Contact> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_setting);

        Toolbar toolbar = findViewById(R.id.setting_group_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        contacts = new ArrayList<>();
        contacts = (ArrayList<Contact>) getIntent().getSerializableExtra("groupContacts");

        recyclerView = (RecyclerView) findViewById(R.id.settting_group_recyclerview);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // specify an adapter (see also next example)
        mAdapter = new SettingContactsAdapter(contacts,GroupSettingActivity.this);
        recyclerView.setAdapter(mAdapter);

    }
}
