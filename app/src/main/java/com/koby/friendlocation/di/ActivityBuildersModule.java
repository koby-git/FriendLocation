package com.koby.friendlocation.di;

import com.koby.friendlocation.activities.auth.LoginActivity;
import com.koby.friendlocation.activities.auth.RecoverPasswordActivity;
import com.koby.friendlocation.activities.auth.RegisterActivity;
import com.koby.friendlocation.activities.auth.SplashScreenActivity;
import com.koby.friendlocation.activities.main.InviteReceiveActivity;
import com.koby.friendlocation.activities.main.MainActivity;
import com.koby.friendlocation.activities.main.SettingsActivity;
import com.koby.friendlocation.activities.maps.MapsActivity;
import com.koby.friendlocation.di.auth.InviteReceiveActivityModule;
import com.koby.friendlocation.fragments.profile.BaseProfileFragment;
import com.koby.friendlocation.fragments.profile.GroupProfileFragment;
import com.koby.friendlocation.fragments.profile.UserProfileFragment;


import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    @ContributesAndroidInjector
    abstract LoginActivity contributeLoginActivity();

    @ContributesAndroidInjector
    abstract SplashScreenActivity contributeSplashScreenActivity();

    @ContributesAndroidInjector(
                    modules = {
                            InviteReceiveActivityModule.class,
                    })
    abstract InviteReceiveActivity contributeInviteReceiveActivity();

    @ContributesAndroidInjector
    abstract RegisterActivity contributeRegisterActivity();


    @ContributesAndroidInjector
    abstract RecoverPasswordActivity contributeRecoverPasswordActivity();

    @ContributesAndroidInjector(
            modules = {
                    MainActivityModule.class,
    })
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector(
            modules = {SettingsFragmentBuildersModule.class}
    )
    abstract SettingsActivity contributeSettingsActivity();

    @ContributesAndroidInjector(
            modules = {
                    MainActivityModule.class,
            })
    abstract MapsActivity contributeMapsActivity();

}
