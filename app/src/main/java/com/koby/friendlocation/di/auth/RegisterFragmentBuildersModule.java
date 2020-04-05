package com.koby.friendlocation.di.auth;

import com.koby.friendlocation.fragments.profile.RegisterFragment;
import com.koby.friendlocation.fragments.profile.RegisterProfileFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class RegisterFragmentBuildersModule {

    @ContributesAndroidInjector
    abstract RegisterFragment contributeRegisterFragment();

    @ContributesAndroidInjector
    abstract RegisterProfileFragment contributeRegisterProfileFragment();
}
