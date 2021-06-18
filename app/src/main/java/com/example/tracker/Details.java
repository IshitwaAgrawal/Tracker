package com.example.tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Details extends AppCompatActivity {
    private EditText n, e, num;
    private Button submit;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    public String s;
    private double latitude;
    private double longitude;
    private boolean isGPSEnabled = true;
    private int PERMISSION_ID = 44;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        n = findViewById(R.id.name1);
        e = findViewById(R.id.email1);
        num = findViewById(R.id.num1);
        progressBar = findViewById(R.id.progressBar2);
        submit = findViewById(R.id.Save_details);
        mAuth = FirebaseAuth.getInstance();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check();
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if(checkPermissions()){
            if(isLocationEnabled()){
                mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Location> task) {
                        Location location = task.getResult();
                        if(location==null){
                            requestNewLocationData();
                        } else{
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                });
            }else{
                showToast("Please Turn on your location...");
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }else{
            requestsPermission();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(){
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
        }
    };

    public void back(View v) {
        Intent i = new Intent(Details.this, MainActivity.class);
        startActivity(i);
    }

    private void moveToHome(){
        Intent i = new Intent(Details.this,home.class);
        startActivity(i);
    }

    private void check() {
        String Phone = num.getText().toString().trim();
        String email = e.getText().toString().trim();
        String name = n.getText().toString().trim();
        getLastLocation();
        if (Phone.isEmpty()) {
            num.setError("Required");
            num.requestFocus();

        } else if (Phone.length() != 10) {
            num.setError("Number should be 10 digits long");
            num.requestFocus();
        } else if (email.isEmpty()) {
            e.setError("Required");
            e.requestFocus();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            e.setError("Invalid email");
            e.requestFocus();
            return;
        } else if (name.isEmpty()) {
            n.setError("Required");
            n.requestFocus();
        } else {
            if(isGPSEnabled)
                updateUserProfile(name,email,Phone);
        }
    }
    private void updateUserProfile(String name,String email,String Phone){
        progressBar.setVisibility(View.VISIBLE);
        String uid;
        if(mAuth.getCurrentUser()!=null)
            uid = mAuth.getCurrentUser().getUid().toString();
        else
            uid = UUID.randomUUID().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> mp = new HashMap<>();
        mp.put("id", uid);
        mp.put("name", name);
        mp.put("email", email);
        mp.put("phone", Phone);
        mp.put("disabled", false);
        mp.put("timestamp", FieldValue.serverTimestamp());
        db.collection("users").document(uid).set(mp)
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Log.d("FAILURE",e.getMessage());
                }
            })
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void unused) {
                      Log.d("SUCCESS","Document written....");
                      updateLocation(uid,db);
                  }
              }
            );
        }
    private void updateLocation(String uid,FirebaseFirestore db){
        Map<String,Object> location = new HashMap<>();
        location.put("latitude",latitude);
        location.put("longitude",longitude);
        db.collection("/location").document(uid).set(location)
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull @NotNull Exception e) {
                    Log.d("FAILURE",e.getMessage());
                }
            })
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    progressBar.setVisibility(View.GONE);
                    moveToHome();
                    Log.d("SUCCESS","Location uploaded...");
                }
            }
        );
    }
    public void showToast(String msg){
        Toast.makeText(Details.this,msg,Toast.LENGTH_SHORT).show();
    }
    private boolean checkPermissions(){
        return ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED&&ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;
    }
    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    private void requestsPermission(){
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        },PERMISSION_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_ID){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(checkPermissions()){
            getLastLocation();
        }
    }
}