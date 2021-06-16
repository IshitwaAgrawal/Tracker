package com.example.tracker.ui.license;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.tracker.databinding.FragmentLicenseBinding;

public class LicenseFragment extends Fragment {

    private com.example.tracker.ui.license.LicenseViewModel LicenseViewModel;
    private FragmentLicenseBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LicenseViewModel =
                new ViewModelProvider(this).get(com.example.tracker.ui.license.LicenseViewModel.class);

        binding = FragmentLicenseBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textLic;
        LicenseViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}