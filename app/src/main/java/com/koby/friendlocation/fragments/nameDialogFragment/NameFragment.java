package com.koby.friendlocation.fragments.nameDialogFragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.koby.friendlocation.R;
import com.koby.friendlocation.viewmodel.NameViewModel;
import com.koby.friendlocation.repository.FirebaseRepository;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasAndroidInjector;
import dagger.android.support.AndroidSupportInjection;

public abstract class NameFragment extends BottomSheetDialogFragment implements HasAndroidInjector {

    @Inject
    DispatchingAndroidInjector<Object> androidInjector;

    @Inject
    @Nullable
    FirebaseUser firebaseUser;

    @Inject FirebaseRepository firebaseRepository;

    @BindView(R.id.fragment_name_edit_text)
    EditText usernameEditText;

    @BindView(R.id.fragment_name_title)
    TextView title;
    
    NameViewModel nameViewModel;
    String currentName;
    private Unbinder unbinder;

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
        setTitle();
        usernameEditText.requestFocus();
        usernameEditText.setText(currentName);
    }

    protected abstract void setTitle();

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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public AndroidInjector<Object> androidInjector() {
        return androidInjector;
    }
    
    protected abstract void updateProfile(String username);
}
