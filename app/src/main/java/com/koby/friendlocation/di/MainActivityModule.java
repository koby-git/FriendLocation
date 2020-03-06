package com.koby.friendlocation.di;

import com.koby.friendlocation.repository.FirebaseRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class MainActivityModule {

    @Provides
    static FirebaseRepository provideFirebaseRepository(){
        return FirebaseRepository.getInstance();
    }
}
