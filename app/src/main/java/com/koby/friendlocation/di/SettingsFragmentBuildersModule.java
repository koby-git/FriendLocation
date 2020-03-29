package com.koby.friendlocation.di;

import com.koby.friendlocation.fragments.SettingsFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class SettingsFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract SettingsFragment contributeSettingsFragment();
}
