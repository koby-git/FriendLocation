package com.koby.friendlocation.fragments.nameDialogFragment;

public class GroupNameFragment extends NameFragment {

    @Override
    public void setCurrentName() {

    }

    @Override
    protected void updateProfile(String name) {
        nameViewModel.setName(name);
    }


}
