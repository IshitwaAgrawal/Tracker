package com.example.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TrackDetails extends AppCompatActivity {
    private EditText source,destination,time;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_details);

        //the fields user has to share when sharing the location.
        Button share_button = (Button)findViewById(R.id.share_button);
        source = (EditText) findViewById(R.id.source_detail);
        destination = (EditText) findViewById(R.id.destination_detail);
        time = (EditText) findViewById(R.id.time_detail);

        mAuth = FirebaseAuth.getInstance();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        share_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(source.getText()!=null && destination.getText()!=null && time.getText()!=null)
                    storeDetails(userId);
                else{
                    String k = "This field is required";
                    if(source.getText()==null)
                        source.setError(k);
                    if(destination.getText()==null)
                        destination.setError(k);
                    if(time.getText()==null)
                        time.setError(k);
                }
            }
        });

    }

    private void storeDetails(String id){

        Map<String,String> data = new HashMap<>();
        data.put("Source",source.getText().toString());
        data.put("Destination",destination.getText().toString());
        data.put("Time",time.getText().toString());

        FirebaseFirestore f = FirebaseFirestore.getInstance();

        f.collection("location_details").document(id).set(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ERROR","FAILURE IN UPLOADING DATA.");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("SUCCESS","SUCCESSFULLY UPLOADED DATA.");
                        showToast("INFORMATION SAVED SUUCESSFULLY...");
                        Intent i = new Intent(TrackDetails.this,home.class);
                        startActivity(i);
                    }
                });
    }

    public void back(View v) {
        Intent i=new Intent(TrackDetails.this,MainActivity.class);
        startActivity(i);
    }
    public void showToast(String msg){
        Toast.makeText(TrackDetails.this,msg,Toast.LENGTH_SHORT).show();
    }
//    public void otp(View v) {
//        String Phone = n.getText().toString().trim();
//        if (Phone.isEmpty()) {
//            n.setError("Required");
//            n.requestFocus();
//
//        } else if (Phone.length()!=10) {
//            n.setError("Number should be 10 digits long");
//            n.requestFocus();
//        } else {
//            b.setVisibility(View.VISIBLE);
//            e.setVisibility(View.VISIBLE);
//            n.setVisibility(View.INVISIBLE);
//            r.setVisibility(View.INVISIBLE);
//        }
//    }


}