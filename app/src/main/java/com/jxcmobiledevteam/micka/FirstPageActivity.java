package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Locale.getDefault;

public class FirstPageActivity extends AppCompatActivity implements OnMapReadyCallback{
    GoogleMap GMap;
    LocationManager locationmanager;
    SupportMapFragment mapFragment;
    Geocoder geocoder;
    List<Address> addresses;
    LatLng my_location;
    Button _start_confirm;
    EditText _start_location;
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    ValueEventListener valueEventListener;
    Bitmap smallMarker;
    Location mMyLocation;
    Location mTaxiLocation;
    private ArrayList<Taxi> mTaxis = new ArrayList<>();

    List<Place.Field> fields = Arrays.asList(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geocoder = new Geocoder(this, getDefault());
        setContentView(R.layout.activity_first_page);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        int height = 100;
        int width = 100;
        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_car_front);
        Bitmap b = bitmapdraw.getBitmap();
        smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        mMyLocation = new Location("User");
        mTaxiLocation = new Location("Taxi");

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        _start_confirm = (Button)findViewById(R.id.start_btn_confrim);
        _start_location = (EditText)findViewById(R.id.start_location);

        locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
        my_location = new LatLng(48.85299,2.34288);
        Location LocationGps;
        String provider = locationmanager.getBestProvider(new Criteria(), true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
//        locationmanager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 30, (LocationListener) this);
        LocationGps = locationmanager.getLastKnownLocation(provider);
        if (LocationGps != null) {
            my_location = new LatLng(LocationGps.getLatitude(), LocationGps.getLongitude());
            Common.getInstance().setStart_location(my_location);

        }else{
            my_location = new LatLng(48.85299,2.34288);
        }
        try {
            addresses = geocoder.getFromLocation(my_location.latitude, my_location.longitude, 1);
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            String throughFare = addresses.get(0).getThoroughfare();
            Log.d("location", String.valueOf(addresses));
            _start_location.setText(knownName + " " +throughFare+ ", " +postalCode + " " +city);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setupView();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        showTaxi();
//        mapFragment.getMapAsync(this);
    }

    public void onStart() {
        super.onStart();
        Log.d("start::", "starte");
    }


    public void startConfirm(){
        Common.getInstance().setStart_location(my_location);
        action();
        Intent intent = new Intent(FirstPageActivity.this, SecondPageActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
    }

    private void setupView(){
        _start_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(getBaseContext());
                startActivityForResult(intent,101);
            }
        });
        _start_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConfirm();
            }
        });
    }
    private void showTaxi(){
        mRef = mDatabase.getReference("user/");
        valueEventListener=mRef.orderByChild("type").equalTo("driver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("datasa1:", String.valueOf(dataSnapshot));
                mTaxis.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    String mUid= ds.getKey();
                    if(ds.child("join").getValue(String.class).equals("on")){
                        Double mLatitude= ds.child("latitude").getValue(Double.class);
                        Double mLongitude= ds.child("longitude").getValue(Double.class);
                        LatLng mLatLng = new LatLng(mLatitude, mLongitude);
                        Taxi _mTaxi = new Taxi(mLatLng,mUid);
                        mTaxis.add(_mTaxi);
                        Log.d("datasaLa1:", mUid);
                    }
                    Log.d("datasaLatitude1:", mUid);
                }
                mapFragment.getMapAsync(FirstPageActivity.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && data != null){
            Place place = Autocomplete.getPlaceFromIntent(data);
            _start_location.setText(place.getAddress());
            my_location = place.getLatLng();
            mapFragment.getMapAsync(this);
            Log.d("place", "place:"+place.getAddress());
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.clear();
        GMap = googleMap;
        googleMap.addMarker(new MarkerOptions().position(my_location));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(my_location,14));
        for(Taxi oneTaxi : mTaxis){
            mMyLocation.setLatitude(my_location.latitude);
            mMyLocation.setLongitude(my_location.longitude);
            mTaxiLocation.setLatitude(oneTaxi.getmTaxiLocation().latitude);
            mTaxiLocation.setLongitude(oneTaxi.getmTaxiLocation().longitude);
            if(mMyLocation.distanceTo(mTaxiLocation) < 5000){
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(oneTaxi.getmTaxiLocation());
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                googleMap.addMarker(markerOptions);
            }
        }

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                String shop_address = "";
                try {
                    addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    my_location = latLng;
                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();
                    String throughFare = addresses.get(0).getThoroughfare();
                    Log.d("location", String.valueOf(addresses));
                    _start_location.setText(knownName + " " +throughFare+ ", " +postalCode + " " +city);
                    shop_address = address;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MarkerOptions markerOption = new MarkerOptions();
                markerOption.position(latLng);
                markerOption.title(shop_address);
                googleMap.clear();
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.addMarker(markerOption);

                for(Taxi oneTaxi : mTaxis){
                    mMyLocation.setLatitude(my_location.latitude);
                    mMyLocation.setLongitude(my_location.longitude);
                    mTaxiLocation.setLatitude(oneTaxi.getmTaxiLocation().latitude);
                    mTaxiLocation.setLongitude(oneTaxi.getmTaxiLocation().longitude);
                    if(mMyLocation.distanceTo(mTaxiLocation) < 5000){
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(oneTaxi.getmTaxiLocation());
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                        googleMap.addMarker(markerOptions);
                    }
                }
            }
        });
    }
    private void action(){
        mRef.removeEventListener(valueEventListener);
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
        Intent intent=new Intent(this, UserProfileActivity.class);
        startActivity(intent);
    }
    private void moveToRideHistory(){
        Intent intent=new Intent(this, RideHistoryActivity.class);
        startActivity(intent);
    }
    private void moveToPaymentHistory(){
        Intent intent=new Intent(this, PaymentHistoryActivity.class);
        startActivity(intent);
    }
    private void moveToPromos(){
        Intent intent=new Intent(this, PromosActivity.class);
        startActivity(intent);
    }
    private void moveToSetting(){
        Intent intent=new Intent(this, UserSettingActivity.class);
        startActivity(intent);
    }
}