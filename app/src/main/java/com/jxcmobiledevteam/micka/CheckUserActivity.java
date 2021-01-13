package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.os.Handler;
import android.util.Log;

import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CheckUserActivity extends AppCompatActivity implements LocationListener {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private LocationManager locationmanager;
    private ChildEventListener childEventListener;
    String user_status = "";
    String user_type ="";
    String user_enable ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_user);
        if(!Places.isInitialized()){
//            Places.initialize(getBaseContext(),"AIzaSyDx0lfU-akX0HiFDtEUUIJ99rugOB95Ip4");
            Places.initialize(getBaseContext(),"AIzaSyB8RD2Pu5w7bv-UrhWA5dN1Brzdo-yf1SI");
        }
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = locationmanager.getBestProvider(new Criteria(), true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, this);
        checkUser();
    }
    @Override
    public void onBackPressed() {
    }
    private void checkUser(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            Log.d("user uid:", currentUser.getUid());
            mRef = mDatabase.getReference("user/"+mAuth.getUid());

            childEventListener = mRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    if(dataSnapshot.getKey().equals("status")) {
                        user_status = (String) dataSnapshot.getValue();
                    }

                    if(dataSnapshot.getKey().equals("type")) {
                        user_type = (String) dataSnapshot.getValue();
                    }
                    if(dataSnapshot.getKey().equals("enable")) {
                        user_enable = (String) dataSnapshot.getValue();
                    }

                    if(user_status.equals("on")){
                        if(user_type.equals("driver")){
                            if(user_enable.equals("yes")){
                                Handler handler=new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        action();
                                        Intent intent = new Intent(getBaseContext(), MainDriverActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                },5000);
                            }else{
                                Handler handler=new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        action();
                                        Intent intent = new Intent(getBaseContext(), FirstPageActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                },5000);
                            }


                        }else{
                            Handler handler=new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    action();
                                    Intent intent = new Intent(getBaseContext(), FirstPageActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            },5000);

                        }
                    }else{
                        Handler handler=new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                action();
                                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        },5000);
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }else{
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            },5000);
        }
    }

    private void action(){
        mRef.removeEventListener(childEventListener);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Location:::", String.valueOf( location.getLatitude()));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d("providerEnabled:", s);
    }

    @Override
    public void onProviderDisabled(String s) {
    }
}