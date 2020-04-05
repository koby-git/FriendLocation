package com.koby.friendlocation.di;

import com.koby.friendlocation.activities.auth.LoginActivity;
import com.koby.friendlocation.activities.auth.RecoverPasswordActivity;
import com.koby.friendlocation.activities.auth.RegisterActivity;
import com.koby.friendlocation.activities.auth.SplashScreenActivity;
import com.koby.friendlocation.activities.main.CreateGroupActivity;
import com.koby.friendlocation.activities.main.InviteReceiveActivity;
import com.koby.friendlocation.activities.main.MainActivity;
import com.koby.friendlocation.activities.main.SettingsActivity;
import com.koby.friendlocation.activities.maps.GroupSettingActivity;
import com.koby.friendlocation.activities.maps.MapsActivity;
import com.koby.friendlocation.di.auth.InviteReceiveActivityModule;
import com.koby.friendlocation.di.auth.RegisterFragmentBuildersModule;
import com.koby.friendlocation.di.main.CreateGroupActivityBuildersModule;
import com.koby.friendlocation.di.main.SettingsFragmentBuildersModule;
import com.koby.friendlocation.fragments.profile.BaseProfileFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    //Auth
    @ContributesAndroidInjector
    abstract LoginActivity contributeLoginActivity();

    @ContributesAndroidInjector
    abstract SplashScreenActivity contributeSplashScreenActivity();

    @ContributesAndroidInjector
    abstract RecoverPasswordActivity contributeRecoverPasswordActivity();

    @ContributesAndroidInjector(modules = {RegisterFragmentBuildersModule.class})
    abstract RegisterActivity contributeRegisterActivity();

    //Main
    @ContributesAndroidInjector()
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector(modules = CreateGroupActivityBuildersModule.class)
    abstract CreateGroupActivity contributeCreateGroupActivity();

    @ContributesAndroidInjector(modules = {SettingsFragmentBuildersModule.class})
    abstract SettingsActivity contributeSettingsActivity();

    @ContributesAndroidInjector(modules = {InviteReceiveActivityModule.class})
    abstract InviteReceiveActivity contributeInviteReceiveActivity();

    //Maps
    @ContributesAndroidInjector
    abstract MapsActivity contributeMapsActivity();

    @ContributesAndroidInjector
    abstract GroupSettingActivity contributeGroupSettingActivity();

    @ContributesAndroidInjector
    abstract BaseProfileFragment contributeBaseProfileFragment();

}
