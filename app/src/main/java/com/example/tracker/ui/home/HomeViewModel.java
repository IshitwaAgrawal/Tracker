package com.example.tracker.ui.home;

import android.widget.Button;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> mText;


    public HomeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("1234567890");
        if(FirebaseAuth.getInstance().getCurrentUser()!=null) {
            mText.setValue(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
        }
    }

    public LiveData<String> getText() {
        return mText;
    }
}