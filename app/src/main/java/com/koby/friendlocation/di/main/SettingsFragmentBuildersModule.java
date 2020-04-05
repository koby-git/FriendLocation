package com.koby.friendlocation.di.main;

import com.koby.friendlocation.fragments.profile.UserProfileFragment;
import com.koby.friendlocation.fragments.settings.SettingsFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class SettingsFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract SettingsFragment contributeSettingsFragment();

    @ContributesAndroidInjector
    abstract UserProfileFragment contributeUserProfileFragment();
}
