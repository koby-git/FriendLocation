package com.koby.friendlocation.di;

import com.koby.friendlocation.activities.SplashScreenActivity;
import com.koby.friendlocation.activities.auth.LoginActivity;
import com.koby.friendlocation.activities.auth.RecoverPasswordActivity;
import com.koby.friendlocation.activities.main.MainActivity;
import com.koby.friendlocation.activities.maps.MapsActivity;


import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    @ContributesAndroidInjector
    abstract LoginActivity contributeLoginActivity();

    @ContributesAndroidInjector
    abstract SplashScreenActivity contributeSplashScreenActivity();

    @ContributesAndroidInjector
    abstract RecoverPasswordActivity contributeRecoverPasswordActivity();

    @ContributesAndroidInjector(
            modules = {
                    MainActivityModule.class,
    })
    abstract MainActivity contributeMainActivity();

    @ContributesAndroidInjector(
            modules = {
                    MainActivityModule.class,
            })
    abstract MapsActivity contributeMapsActivity();
}
