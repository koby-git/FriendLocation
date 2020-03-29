package com.koby.friendlocation.fragments.nameDialogFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.koby.friendlocation.R;
import com.koby.friendlocation.viewmodel.NameViewModel;
import com.koby.friendlocation.repository.FirebaseRepository;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public abstract class NameFragment extends BottomSheetDialogFragment {

    @BindView(R.id.fragment_name_edit_text)
    EditText usernameEditText;

    FirebaseAuth mAuth;
    FirebaseRepository firebaseRepository;
    NameViewModel nameViewModel;
    Unbinder unbinder;
    String currentName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);
        setHasOptionsMenu(true);
        nameViewModel = ViewModelProviders.of(requireActivity()).get(NameViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_name, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        unbinder = ButterKnife.bind(this,view);

        mAuth = FirebaseAuth.getInstance();
        firebaseRepository = FirebaseRepository.getInstance();

        setCurrentName();

        usernameEditText.requestFocus();
        usernameEditText.setText(currentName);
    }

    @OnClick(R.id.fragment_name_confirm_button)
    public void confirm(){
        String username = usernameEditText.getText().toString();
        if(username.equals(currentName) || username.isEmpty()){
            return;
        }else {
            updateProfile(username);
            dismiss();
        }
    }

    @OnClick({R.id.fragment_name_cancel_button})
    public void cancel(){
        dismiss();
    }

    public abstract void setCurrentName();
    protected abstract void updateProfile(String username);
}
