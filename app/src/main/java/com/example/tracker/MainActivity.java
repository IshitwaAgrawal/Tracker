package com.example.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private EditText n,e,otp;
    private Button submit;

    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private PhoneAuthProvider.ForceResendingToken forceResendingToken;
    private String mVerificationId;
    private boolean flag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        n = findViewById(R.id.num);
        e = findViewById(R.id.email);
        otp = findViewById(R.id.otpEntered);
        progressBar = findViewById(R.id.progressBar);
        submit = findViewById(R.id.Save);

        mAuth = FirebaseAuth.getInstance();
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!flag) check();
                else checkOtp();
            }
        });
    }

    private void checkOtp() {
        progressBar.setVisibility(View.VISIBLE);
        String otp_entered = otp.getText().toString();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId,otp_entered);
        signInWithOTP(credential);
    }

    private void signInWithOTP(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    Intent i = new Intent(MainActivity.this,Details.class);
                    startActivity(i);
                }
                else {
                    otp.setError("Entered OTP is incorrect!");
                    otp.setText("");
                    progressBar.setVisibility(View.GONE);
                    flag = false;
                }
            }
        });
    }

    private void check() {
        String Phone = n.getText().toString().trim();
        String email=e.getText().toString().trim();
        if (Phone.isEmpty()) {
            n.setError("Required");
            n.requestFocus();

        } else if (Phone.length() != 10) {
            n.setError("Number should be 10 digits long");
            n.requestFocus();
        }
        else if (email.isEmpty()) {
            e.setError("Required");
            e.requestFocus();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            e.setError("Invalid email");
            e.requestFocus();
            return;
        }
        else {
            progressBar.setVisibility(View.VISIBLE);
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(@NonNull @NotNull PhoneAuthCredential phoneAuthCredential) {
                    signInWithOTP(phoneAuthCredential);
                }

                @Override
                public void onVerificationFailed(@NonNull @NotNull FirebaseException e) {

                }

                @Override
                public void onCodeSent(@NonNull @NotNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    progressBar.setVisibility(View.GONE);
                    otp.setVisibility(View.VISIBLE);
                    forceResendingToken = forceResendingToken;
                    submit.setText("Validate OTP");
                    mVerificationId = s;
                    flag = !flag;
                }
            };
            PhoneAuthOptions phoneAuthOptions = PhoneAuthOptions.newBuilder(mAuth)
                    .setPhoneNumber("+91"+Phone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(this)
                    .setCallbacks(mCallbacks)
                    .build();
            PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions);
        }
    }

    private void forg() {
        Intent i=new Intent(MainActivity.this, track_details.class);
        startActivity(i);
    }

}