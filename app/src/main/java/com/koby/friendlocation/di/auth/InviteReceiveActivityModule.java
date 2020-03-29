package com.koby.friendlocation.di.auth;

import com.koby.friendlocation.model.Group;

import dagger.Module;
import dagger.Provides;

@Module
public class InviteReceiveActivityModule {

    @Provides
    static Group provideGroup(){
        return new Group();
    }
}
