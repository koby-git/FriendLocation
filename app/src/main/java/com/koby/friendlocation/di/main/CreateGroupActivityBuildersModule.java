package com.koby.friendlocation.di.main;
import com.koby.friendlocation.fragments.nameDialogFragment.GroupNameFragment;
import com.koby.friendlocation.fragments.profile.GroupProfileFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class CreateGroupActivityBuildersModule {

    @ContributesAndroidInjector
    abstract GroupProfileFragment contributeGroupProfileFragment();

    @ContributesAndroidInjector
    abstract GroupNameFragment contributeGroupNameFragment();

}
