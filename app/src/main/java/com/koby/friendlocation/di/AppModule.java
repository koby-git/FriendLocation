package com.koby.friendlocation.di;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

    @Singleton
    @Provides
    @Nullable
    static FirebaseUser provideFirebaseUser(FirebaseAuth firebaseAuth){
        return firebaseAuth.getCurrentUser();
    }

    @Provides
    static FirebaseFirestore provideFirebaseFirestore(){
        return FirebaseFirestore.getInstance();
    }
}
