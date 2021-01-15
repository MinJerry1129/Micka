package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

import static java.util.Locale.getDefault;

public class MainDriverActivity extends AppCompatActivity implements LocationListener , OnMapReadyCallback {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private DatabaseReference mBookRef;
    private LocationManager locationmanager;
    private LatLng myLocation;
    private LatLng targetLocation;
    private SupportMapFragment mapFragment;
    private String joinStatus;
    private Button _setJoin;
    private Geocoder geocoder;
    private List<Address> addresses;

    private LinearLayout _layoutConfirm;
    private TextView _startAddress;
    private TextView _price;
    private Button _confirm;
    private Button _cancel;
    private ImageView _phoneCall;
    private ImageView _phoneWaze;

    private LatLng startLocation;
    private LatLng endLocation;
    private String phoneNumber;
    private Integer price;
    private String bookingStatus;
    private String ride_uuid;
    private String remove_status="yes";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_driver);
        geocoder = new Geocoder(this, getDefault());
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        _layoutConfirm = (LinearLayout)findViewById(R.id.layout_confirm);
        _layoutConfirm.setVisibility(View.GONE);
        _startAddress = (TextView)findViewById(R.id.start_address);
        _price = (TextView)findViewById(R.id.txt_price);
        _confirm = (Button) findViewById(R.id.btn_booking);
        _cancel = (Button) findViewById(R.id.btn_cancel);
        _phoneCall = (ImageView) findViewById(R.id.btn_call);
        _phoneWaze = (ImageView) findViewById(R.id.btn_waze);
        _phoneCall.setVisibility(View.GONE);
        _phoneWaze.setVisibility(View.GONE);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        _setJoin = (Button)findViewById(R.id.btn_join);
        setupView();

        _phoneWaze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try
                {
//                    String url = "https://waze.com/ul?ul?dir_first=poi&navigate=yes&to=ll."+ myLocation.latitude + ","+myLocation.longitude  +"&from=ll." +targetLocation.latitude + "," +targetLocation.longitude;
                    String url = "https://waze.com/ul?ll="+targetLocation.latitude+","+targetLocation.longitude+"&z=10";
                    Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
                    startActivity( intent );
                }
                catch ( ActivityNotFoundException ex  )
                {
                    Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( "market://details?id=com.waze" ) );
                    startActivity(intent);
                }
            }
        });

        _phoneCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(MainDriverActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainDriverActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);

                }else{
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                }
            }
        });
        _cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remove_status = "no";
                mDatabase.getReference("ride/"+ride_uuid).removeValue();
            }
        });

        _setJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d("joinStatus:::", joinStatus);
                if(joinStatus.equals("on")){
                    mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");
                }else if(joinStatus.equals("off")){
                    mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("on");
                }
            }
        });
        locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = locationmanager.getBestProvider(new Criteria(), true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 30, this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }
    private void setupView(){
        mRef= mDatabase.getReference("user/"+mAuth.getUid()+"/join");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                joinStatus = dataSnapshot.getValue(String.class);
                if(joinStatus != null){
                    Log.d("joinStatus11111:::", joinStatus);
                    Common.getInstance().setJon_status(joinStatus);
                    if(joinStatus.equals("on")){
                        _setJoin.setBackgroundResource(R.drawable.back_pink_button);
                        _setJoin.setText("OffLine");
                    }else {
                        _setJoin.setBackgroundResource(R.drawable.back_blue_button);
                        _setJoin.setText("OnLine");
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mBookRef= mDatabase.getReference("ride");
        mBookRef.orderByChild("driver").equalTo(mAuth.getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("datasnapshot value:", dataSnapshot.getKey());
                String bookingStatus1 = dataSnapshot.child("status").getValue(String.class);
                if (bookingStatus1 != null){
                    if(bookingStatus1.equals("waiting")||bookingStatus1.equals("accept")||bookingStatus1.equals("pickup")||bookingStatus1.equals("pay")){
                        ride_uuid = dataSnapshot.getKey();
                        bookingStatus = bookingStatus1;
                        Double start_latitude = dataSnapshot.child("startlat").getValue(Double.class);
                        Double start_longitude = dataSnapshot.child("startlong").getValue(Double.class);
                        Double end_latitude = dataSnapshot.child("endlat").getValue(Double.class);
                        Double end_longitude = dataSnapshot.child("endlong").getValue(Double.class);
                        phoneNumber = dataSnapshot.child("passengernumber").getValue(String.class);
                        startLocation = new LatLng(start_latitude, start_longitude);
                        endLocation = new LatLng(end_latitude, end_longitude);
                        targetLocation = startLocation;
                        String start_address = getLocationAddress(startLocation);
                        String end_address = getLocationAddress(endLocation);
                        _startAddress.setText(start_address);
                        price = dataSnapshot.child("price").getValue(int.class);
                        _price.setText(String.valueOf(price) + "€");
                        if (bookingStatus.equals("waiting")){
                            mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");
                            _confirm.setText("Accept");
                            _confirm.setEnabled(true);
                            _price.setTextColor(Color.parseColor("#ff2773"));
                            _phoneCall.setVisibility(View.GONE);
                            _phoneWaze.setVisibility(View.GONE);
                            _confirm.setBackgroundResource(R.drawable.back_pink_button);
                        }else if (bookingStatus.equals("accept")){
                            mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");
                            _setJoin.setVisibility(View.INVISIBLE);
                            _confirm.setText("Pickup");
                            _confirm.setEnabled(true);
                            _price.setTextColor(Color.parseColor("#1478f2"));
                            _phoneCall.setVisibility(View.VISIBLE);
                            _phoneWaze.setVisibility(View.VISIBLE);
                            _confirm.setBackgroundResource(R.drawable.back_blue_button);
                        }else if (bookingStatus.equals("pickup")){
                            mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");
                            _setJoin.setVisibility(View.INVISIBLE);
                            _confirm.setText("Paid");
                            _confirm.setEnabled(false);
                            _startAddress.setText(end_address);
                            targetLocation = endLocation;
                            _price.setTextColor(Color.parseColor("#1478f2"));
                            _phoneCall.setVisibility(View.VISIBLE);
                            _phoneWaze.setVisibility(View.VISIBLE);
                            _confirm.setBackgroundResource(R.drawable.back_blue_button);
                        }else if (bookingStatus.equals("pay")){
                            mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");
                            _setJoin.setVisibility(View.INVISIBLE);
                            _confirm.setText("Paid");
                            _confirm.setEnabled(true);
                            _startAddress.setText(end_address);
                            targetLocation = endLocation;
                            _price.setTextColor(Color.parseColor("#1478f2"));
                            _phoneCall.setVisibility(View.VISIBLE);
                            _phoneWaze.setVisibility(View.VISIBLE);
                            _confirm.setBackgroundResource(R.drawable.back_blue_button);
                        }else if (bookingStatus.equals("paid")){
                            mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("on");
                            _setJoin.setVisibility(View.VISIBLE);
                            _confirm.setEnabled(true);
                            _confirm.setText("Complete");
                            _startAddress.setText(end_address);
                            targetLocation = endLocation;
                            _price.setTextColor(Color.parseColor("#1478f2"));
                            _phoneCall.setVisibility(View.VISIBLE);
                            _phoneWaze.setVisibility(View.VISIBLE);
                            _confirm.setBackgroundResource(R.drawable.back_blue_button);
                        }

                        _layoutConfirm.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ride_uuid = dataSnapshot.getKey();
                bookingStatus = dataSnapshot.child("status").getValue(String.class);
                Double start_latitude = dataSnapshot.child("startlat").getValue(Double.class);
                Double start_longitude = dataSnapshot.child("startlong").getValue(Double.class);
                Double end_latitude = dataSnapshot.child("endlat").getValue(Double.class);
                Double end_longitude = dataSnapshot.child("endlong").getValue(Double.class);

                price = dataSnapshot.child("price").getValue(int.class);
                if ((bookingStatus != null)&&(price != null)&&(start_latitude != null)&&(start_longitude != null)&&(end_latitude != null)&&(end_longitude != null)){
                    _layoutConfirm.setVisibility(View.VISIBLE);
                    startLocation = new LatLng(start_latitude, start_longitude);
                    endLocation = new LatLng(end_latitude, end_longitude);
                    targetLocation = startLocation;
                    String start_address = getLocationAddress(startLocation);
                    String end_address = getLocationAddress(endLocation);
                    phoneNumber = dataSnapshot.child("passengernumber").getValue(String.class);
                    _startAddress.setText(start_address);
                    if (bookingStatus.equals("waiting")){
                        mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");
                        _confirm.setText("Accept");
                        _confirm.setEnabled(true);
                        _price.setTextColor(Color.parseColor("#ff2773"));
                        _phoneCall.setVisibility(View.GONE);
                        _phoneWaze.setVisibility(View.GONE);
                        _confirm.setBackgroundResource(R.drawable.back_pink_button);
                    }else if (bookingStatus.equals("accept")){
                        mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");
                        _setJoin.setVisibility(View.INVISIBLE);
                        _confirm.setText("Pickup");
                        _confirm.setEnabled(true);
                        _price.setTextColor(Color.parseColor("#1478f2"));
                        _phoneCall.setVisibility(View.VISIBLE);
                        _phoneWaze.setVisibility(View.VISIBLE);
                        _confirm.setBackgroundResource(R.drawable.back_blue_button);
                    }else if (bookingStatus.equals("pickup")){
                        mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");
                        _setJoin.setVisibility(View.INVISIBLE);
                        _confirm.setText("Paid");
                        _confirm.setEnabled(false);
                        _startAddress.setText(end_address);
                        targetLocation = endLocation;
                        _price.setTextColor(Color.parseColor("#1478f2"));
                        _phoneCall.setVisibility(View.VISIBLE);
                        _phoneWaze.setVisibility(View.VISIBLE);
                        _confirm.setBackgroundResource(R.drawable.back_blue_button);
                    }else if (bookingStatus.equals("pay")){
                        mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");
                        _setJoin.setVisibility(View.INVISIBLE);
                        _confirm.setText("Complete");
                        _confirm.setEnabled(true);
                        _startAddress.setText(end_address);
                        targetLocation = endLocation;
                        _price.setTextColor(Color.parseColor("#1478f2"));
                        _phoneCall.setVisibility(View.VISIBLE);
                        _phoneWaze.setVisibility(View.VISIBLE);
                        _confirm.setBackgroundResource(R.drawable.back_blue_button);
                    }else if (bookingStatus.equals("paid")){
                        mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("on");
                        _setJoin.setVisibility(View.VISIBLE);
                        _confirm.setEnabled(true);
                        _confirm.setText("Complete");
                        _startAddress.setText(end_address);
                        targetLocation = endLocation;
                        _price.setTextColor(Color.parseColor("#1478f2"));
                        _phoneCall.setVisibility(View.VISIBLE);
                        _phoneWaze.setVisibility(View.VISIBLE);
                        _confirm.setBackgroundResource(R.drawable.back_blue_button);
//                        _layoutConfirm.setVisibility(View.GONE);
                    }else if (bookingStatus.equals("complete")){
                        mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("on");
                        _setJoin.setVisibility(View.VISIBLE);
                        _layoutConfirm.setVisibility(View.GONE);
                    }

                    _price.setText(String.valueOf(price)+ "€");

                }
                Log.d("datasnapshot value:", "adfadsf");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (ride_uuid.equals(dataSnapshot.getKey())){
                    _layoutConfirm.setVisibility(View.GONE);
                    _setJoin.setVisibility(View.VISIBLE);
//                    refresh();
                }
                if(remove_status.equals("yes")){
                    Toast.makeText(MainDriverActivity.this, "Client Cancel the Ride", Toast.LENGTH_LONG).show();
                }
                remove_status = "yes";
                mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("on");
                Log.d("datasnapshot value:", "adfadsf");
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        _confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookingRide();
            }
        });
    }

    public void refresh() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }
    private void bookingRide(){
        if (bookingStatus.equals("waiting")){
            mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");
            mDatabase.getReference("ride/"+ride_uuid+"/status").setValue("accept");
        }else if (bookingStatus.equals("accept")){
            mDatabase.getReference("ride/"+ride_uuid+"/status").setValue("pickup");
        }else if (bookingStatus.equals("pickup")){
//            mDatabase.getReference("ride/"+uniqueId+"/status").setValue("pay");
        }else if (bookingStatus.equals("pay")){
            mDatabase.getReference("ride/"+ride_uuid+"/status").setValue("paid");
            mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("on");
        }else if (bookingStatus.equals("paid")){
            mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("on");
            mDatabase.getReference("ride/"+ride_uuid+"/status").setValue("complete");
//            _layoutConfirm.setVisibility(View.GONE);
        }else if (bookingStatus.equals("complete")){
            mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("on");
            _layoutConfirm.setVisibility(View.GONE);
        }
    }
    private String getLocationAddress(LatLng location){
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            String city = addresses.get(0).getLocality();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            String throughFare = addresses.get(0).getThoroughfare();

            return knownName + " " + throughFare + ", " + postalCode + " " + city;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "No";
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(myLocation));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,14));
    }
    @Override
    public void onLocationChanged(Location location) {
        mDatabase.getReference("user/"+mAuth.getUid()+"/latitude").setValue(location.getLatitude());
        mDatabase.getReference("user/"+mAuth.getUid()+"/longitude").setValue(location.getLongitude());
        myLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_profile:
                    moveToProfile();
                    return true;
                case R.id.navigation_history:
                    moveToRideHistory();
                    return true;
                case R.id.navigation_payment:
                    moveToPaymentHistory();
                    return true;
                case R.id.navigation_promos:
                    moveToPromos();
                    return true;
                case R.id.navigation_setting:
                    moveToSetting();
                    return true;
            }
            return false;
        }
    };
    private void moveToProfile(){
//        Intent intent=new Intent(this, UserProfileActivity.class);
//        startActivity(intent);
    }
    private void moveToRideHistory(){
//        Intent intent=new Intent(this, RideHistoryActivity.class);
//        startActivity(intent);
    }
    private void moveToPaymentHistory(){
//        Intent intent=new Intent(this, PaymentHistoryActivity.class);
//        startActivity(intent);
    }
    private void moveToPromos(){
//        Intent intent=new Intent(this, PromosActivity.class);
//        startActivity(intent);
    }
    private void moveToSetting(){
        Intent intent=new Intent(this, DriverSettingActivity.class);
        startActivity(intent);
        finish();
    }
}