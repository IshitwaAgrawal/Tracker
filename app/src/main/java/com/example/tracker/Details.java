package com.example.tracker;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                check();
            }
        });
    }

    private void getLastBestLocation() {
        LocationManager mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("LOCATION","Location permission not enabled!");
            isGPSEnabled = false;
            showToast("Location not Enabled!!");
            return;
        }
        Location locationGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) { GPSLocationTime = locationGPS.getTime(); }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        try {
            if (0 < GPSLocationTime - NetLocationTime) {
                this.latitude = locationGPS.getLatitude();
                this.longitude = locationGPS.getLongitude();
            } else {
                this.latitude = locationNet.getLatitude();
                this.longitude = locationNet.getLongitude();
            }
        }catch(Exception e){
            isGPSEnabled = false;
            showToast("Location not Enabled!!");
        }
    }

    public String val() {
        return s;
    }

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
        getLastBestLocation();
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
    public void updateUserProfile(String name,String email,String Phone){
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
    public void updateLocation(String uid,FirebaseFirestore db){
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
}