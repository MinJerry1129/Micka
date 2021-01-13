package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Locale.getDefault;

public class PaymentActivity extends AppCompatActivity implements OnMapReadyCallback{
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private ValueEventListener valueEventListener;
    private SupportMapFragment mapFragment;
    private Geocoder geocoder;
    private List<Address> start_addresses;
    private List<Address> end_addresses;
    private LatLng start_location;
    private LatLng end_location;
    private Float distance_value;
    private int price;
    private int pay_status = 0;
    private ArrayList<Taxi> mTaxis = new ArrayList<>();
    private Location _start_location;
    private Location _end_location;
    private Location _taxi_location;
    private Location _result_taxi_location;
    private TextView _txtPrice;
    private Button _start_confirm;
    private LinearLayout _layout_cash;
    private LinearLayout _layout_card;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geocoder = new Geocoder(this, getDefault());
        setContentView(R.layout.activity_payment);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        _txtPrice = (TextView)findViewById(R.id.txt_price);
        _start_confirm = (Button)findViewById(R.id.btn_confrim_pay);
        _layout_cash = (LinearLayout)findViewById(R.id.layout_cash);
        _layout_card = (LinearLayout)findViewById(R.id.layout_card);
        setupView();
        setupAction();
        getTaxiInfo();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    private void setupView(){
        start_location = Common.getInstance().getStart_location();
        end_location = Common.getInstance().getEnd_location();
        _start_location = new Location("start");
        _end_location = new Location("end");
        _taxi_location = new Location("taxi");
        _result_taxi_location = new Location("resulttaxi");
        _start_location.setLatitude(start_location.latitude);
        _start_location.setLongitude(start_location.longitude);
        _end_location.setLatitude(end_location.latitude);
        _end_location.setLongitude(end_location.longitude);
        distance_value = _start_location.distanceTo(_end_location);
//        if(distance_value < 1000.0){
//            price = 8;
//        }else{
            price = Math.round (7 + Math.round(distance_value * 1.07/1000));
//        }
        _txtPrice.setText(String.valueOf(price)+"€");
    }
    private void setupAction(){
        _start_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConfirm();
            }
        });
        _layout_cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _layout_cash.setBackgroundColor(Color.parseColor("#e2e2e2"));
                _layout_card.setBackgroundColor(Color.parseColor("#ffffff"));
                pay_status = 1;
            }
        });
        _layout_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                _layout_card.setBackgroundColor(Color.parseColor("#e2e2e2"));
                _layout_cash.setBackgroundColor(Color.parseColor("#ffffff"));
                pay_status = 2;
            }
        });
    }

    private void startConfirm(){
        if(pay_status == 0){
            Toast.makeText(PaymentActivity.this, "Select payment type.", Toast.LENGTH_LONG).show();
        }else if (pay_status == 1){
            Common.getInstance().setPay_type("cash");
            SearchDriver();
        }else{
            Common.getInstance().setPay_type("card");
            SearchDriver();
//        Intent intent = new Intent(PaymentActivity.this, CheckoutPaymentActivity.class);
//        startActivity(intent);
        }
    }
    private void SearchDriver(){
        if(mTaxis.isEmpty()){
            Toast.makeText(PaymentActivity.this,"No nearest taix.", Toast.LENGTH_LONG).show();
        }else{
            Taxi result_taxi = mTaxis.get(0);
            for(Taxi oneTaxi : mTaxis){
                _taxi_location.setLatitude(result_taxi.getmTaxiLocation().latitude);
                _taxi_location.setLongitude(result_taxi.getmTaxiLocation().longitude);
                _result_taxi_location.setLatitude(oneTaxi.getmTaxiLocation().latitude);
                _result_taxi_location.setLongitude(oneTaxi.getmTaxiLocation().longitude);
                if(_start_location.distanceTo(_taxi_location) > _start_location.distanceTo(_result_taxi_location)){
                    result_taxi = oneTaxi;
                }
            }
            Common.getInstance().setDriver_uid(result_taxi.getmTaxiUid());
            Common.getInstance().setPay_amount((double) price);
            if (pay_status == 1){
                action();
                Intent intent = new Intent(PaymentActivity.this, FindDriverActivity.class);
                startActivity(intent);
                finish();
            }else{
                action();
                Intent intent = new Intent(PaymentActivity.this, CheckoutPaymentActivity.class);
                startActivity(intent);
                finish();
            }

            Log.d("Taxi Result:", String.valueOf(result_taxi.getmTaxiUid()));
        }
    }
    private void getTaxiInfo(){
        mRef = mDatabase.getReference("user/");
        valueEventListener = mRef.orderByChild("type").equalTo("driver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("datasa:", String.valueOf(dataSnapshot));
                mTaxis.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    String mUid= ds.getKey();
                    if(ds.child("join").getValue(String.class).equals("on")){
                        Double mLatitude= ds.child("latitude").getValue(Double.class);
                        Double mLongitude= ds.child("longitude").getValue(Double.class);
                        LatLng mLatLng = new LatLng(mLatitude, mLongitude);
                        Taxi _mTaxi = new Taxi(mLatLng,mUid);
                        mTaxis.add(_mTaxi);
                        Log.d("datasaLa:", mUid);
                    }
                    Log.d("datasaLatitude:", mUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        MarkerOptions start_markerOptions = new MarkerOptions();
        MarkerOptions end_markerOptions = new MarkerOptions();
        try {
            start_addresses = geocoder.getFromLocation(start_location.latitude, start_location.longitude, 1);
            end_addresses = geocoder.getFromLocation(end_location.latitude, end_location.longitude, 1);
            String start_city = start_addresses.get(0).getLocality();
            String start_postalCode = start_addresses.get(0).getPostalCode();
            String start_knownName = start_addresses.get(0).getFeatureName();
            String start_throughFare = start_addresses.get(0).getThoroughfare();

            String end_city = end_addresses.get(0).getLocality();
            String end_postalCode = end_addresses.get(0).getPostalCode();
            String end_knownName = end_addresses.get(0).getFeatureName();
            String end_throughFare = end_addresses.get(0).getThoroughfare();

            start_markerOptions.position(start_location);
            end_markerOptions.position(end_location);

            Common.getInstance().setStart_address(start_knownName + " " +start_throughFare+ ", " +start_postalCode + " " +start_city);
            Common.getInstance().setEnd_address(end_knownName + " " +end_throughFare+ ", " +end_postalCode + " " +end_city);

            start_markerOptions.title(start_knownName + " " +start_throughFare+ ", " +start_postalCode + " " +start_city);
            end_markerOptions.title(end_knownName + " " +end_throughFare+ ", " +end_postalCode + " " +end_city);
            googleMap.clear();
            googleMap.addMarker(start_markerOptions);
            googleMap.addMarker(end_markerOptions);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start_location,14));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onBackPressed() {
        action();
        Intent intent = new Intent(PaymentActivity.this, SecondPageActivity.class);
        startActivity(intent);
        finish();
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