package com.example.tracker.ui.home;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.tracker.Details;
import com.example.tracker.Location;
import com.example.tracker.R;
import com.example.tracker.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private EditText destination_id;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        Button track = (Button) root.findViewById(R.id.track);
        destination_id = (EditText) root.findViewById(R.id.name1);

        track.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchAndSendLocation();
            }
        });
        final TextView textView = binding.textHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    private void fetchAndSendLocation(){
        String dest_id = destination_id.getText().toString();
        Log.d("Destination",dest_id);
        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("location").document(dest_id);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Intent i=new Intent(getActivity(), Location.class);
                    Map<String,Object> location = task.getResult().getData();
                    if(location!=null){
                        Geocoder g = new Geocoder(getActivity());
                        List<Address> l = null;
                        try {
                            l = g.getFromLocation(Double.parseDouble(location.get("latitude").toString()),Double.parseDouble(location.get("longitude").toString()),5);
                            Log.d("Latitude",location.get("latitude").toString());
                            Log.d("Longitude",location.get("longitude").toString());
                            i.putExtra("address",l.get(0).getAddressLine(0));
                            i.putExtra("latitude",location.get("latitude").toString());
                            i.putExtra("longitude",location.get("longitude").toString());
                            startActivity(i);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                Toast.makeText(binding.getRoot().getContext(),"Error in Fetching location...",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}