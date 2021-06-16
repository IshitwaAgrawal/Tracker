package com.example.tracker.ui.license;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class LicenseViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public LicenseViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is License fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}