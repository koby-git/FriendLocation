package com.koby.friendlocation.di;

import android.app.Application;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.koby.friendlocation.providers.LocationProvider;
import com.koby.friendlocation.repository.FirebaseRepository;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    @Provides
    static FirebaseAuth provideFirebaseAuth(){
        return FirebaseAuth.getInstance();
    }

    @Provides
    static FirebaseFirestore provideFirebaseFirestore(){
        return FirebaseFirestore.getInstance();
    }

    @Singleton
    @Provides
    static FirebaseRepository provideFirebaseRepository(FirebaseFirestore db,FirebaseAuth mAuth){
        return new FirebaseRepository(db,mAuth);
    }


    @Singleton
    @Provides
    static LocationProvider provideLocationProvider(Application application){
        return new LocationProvider(application);
    }
}
