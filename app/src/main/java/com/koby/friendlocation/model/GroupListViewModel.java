package com.koby.friendlocation.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.koby.friendlocation.repository.FirebaseRepository;

import java.util.List;

public class GroupListViewModel extends ViewModel {

    FirebaseRepository firebaseRepository;

    public GroupListViewModel() {
        this.firebaseRepository = FirebaseRepository.getInstance();
    }

    private MutableLiveData<List<Group>> groups;

    public LiveData<List<Group>> getGroups() {
        if (groups == null) {
            groups = new MutableLiveData<>();
            loadGroups();
        }
        return groups;
    }

    private void loadGroups() {
//        groups = firebaseRepository.getGroups();
    }

}
