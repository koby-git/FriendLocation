package com.koby.friendlocation.fragments.nameDialogFragment;

import com.koby.friendlocation.R;

public class GroupNameFragment extends NameFragment {

    //Update group profile name
    @Override
    protected void updateProfile(String name) {
        nameViewModel.setName(name);
    }

    @Override
    protected void setTitle() {
        title.setText(getContext().getString(R.string.enter_group_name));
    }
}
