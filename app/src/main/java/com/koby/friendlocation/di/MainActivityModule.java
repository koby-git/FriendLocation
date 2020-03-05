package com.koby.friendlocation.di;

import com.google.firebase.firestore.FirebaseFirestore;
import com.koby.friendlocation.FirebaseRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class MainActivityModule {

    @Provides
    static FirebaseRepository provideFirebaseRepository(){
        return FirebaseRepository.getInstance();
    }


}
