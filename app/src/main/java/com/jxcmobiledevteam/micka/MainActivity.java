package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity{
    Button _loginPhone;
    Button _loginFacebook;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        _loginPhone = (Button) findViewById(R.id.btn_phone);
        _loginFacebook = (Button) findViewById(R.id.btn_facebook);
        _loginPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginPhone();
            }
        });
    }

    private void loginPhone(){
        Intent intent = new Intent(getBaseContext(), PhoneActivity.class);
        startActivity(intent);
    }
    @Override
    public void onBackPressed() {
    }
}