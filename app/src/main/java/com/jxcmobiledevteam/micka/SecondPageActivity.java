package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.util.Locale.getDefault;

public class SecondPageActivity extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap GMap;
    LocationManager locationmanager;
    SupportMapFragment mapFragment;
    Geocoder geocoder;
    List<Address> addresses;
    LatLng my_location;
    Button _end_confirm;
    EditText _end_location;
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
        setContentView(R.layout.activity_second_page);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        _end_confirm = (Button)findViewById(R.id.end_btn_confrim);
        _end_location = (EditText)findViewById(R.id.end_location);

        locationmanager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = locationmanager.getBestProvider(new Criteria(), true);
        my_location = new LatLng(48.85299,2.34288);
        Location LocationGps;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }else{
            LocationGps = locationmanager.getLastKnownLocation(provider);
        }
        if (LocationGps != null) {
            my_location = new LatLng(LocationGps.getLatitude(), LocationGps.getLongitude());
            Common.getInstance().setEnd_location(my_location);
        }else{
            my_location = new LatLng(48.8499,2.3512);
        }
        try {
            addresses = geocoder.getFromLocation(my_location.latitude, my_location.longitude, 1);
            String city = addresses.get(0).getLocality();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();
            String throughFare = addresses.get(0).getThoroughfare();
            Log.d("location", String.valueOf(addresses));
            _end_location.setText(knownName + " " +throughFare+ ", " +postalCode + " " +city);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        _end_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(getBaseContext());
                startActivityForResult(intent,101);
            }
        });
        _end_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endConfirm();
            }
        });
    }

    public void endConfirm(){
        Common.getInstance().setEnd_location(my_location);
        Log.d("end123123_location:", String.valueOf(my_location));
        Intent intent = new Intent(SecondPageActivity.this, PaymentActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK && data != null){
            Place place = Autocomplete.getPlaceFromIntent(data);
            _end_location.setText(place.getAddress());
            my_location = place.getLatLng();
            mapFragment.getMapAsync(this);
            Log.d("place", "place:"+place.getAddress());
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        GMap = googleMap;
        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(my_location));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(my_location,14));

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
                    _end_location.setText(knownName + " " +throughFare+ ", " +postalCode + " " +city);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(shop_address);
                googleMap.clear();
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                googleMap.addMarker(markerOptions);
            }
        });
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(SecondPageActivity.this, FirstPageActivity.class);
        startActivity(intent);
        finish();
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