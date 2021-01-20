package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.util.Locale.getDefault;

public class FindDriverActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private DatabaseReference mdriverPhoneRef;
    private DatabaseReference mRideCheckRef;
    private DatabaseReference mRideCheckRef1;
    private DatabaseReference mUserRef;
    private ValueEventListener mValue_driverPhone;
    private ValueEventListener mValue_driverCheck;
    private ChildEventListener mValue_driverCheck1;
    private ValueEventListener mValue_user;
    private ValueEventListener mValue_ref;
    private Location _taxi_location;
    private Location _result_taxi_location;
    private Location _start_location;
    Taxi result_taxi;

    private ArrayList<Taxi> mTaxis = new ArrayList<>();
    private ArrayList<Taxi> mCheckTaxis = new ArrayList<>();
    private ArrayList<Taxi> mCurrentTaxis = new ArrayList<>();

    private TextView mStartLocation;
    private TextView mEndLocation;
    private Button mCancel;
    private SupportMapFragment mapFragment;

    private String start_addresses;
    private String end_addresses;
    private LatLng start_location;
    private LatLng end_location;
    private Bitmap start_smallMarker;
    private Bitmap end_smallMarker;
    private String uniqueId;
    private String driverUid;
    private int height = 100;
    private int width = 100;

    private String userPhone = "no";
    private String driverPhone = "no";
    private String pay_type;
    private Double payPrice;
    private String cancel_status = "driver";

    private OkHttpClient httpClient = new OkHttpClient();
    String currentDateandTime;
    private String time_status = "first";
    Timer timer = new Timer();

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if(time_status.equals("first")){
                time_status = "second";
                timerHandler.postDelayed(this, 10000);
            }else{
                mDatabase.getReference("ride/"+uniqueId).removeValue();
                SearchDriver();
                timerHandler.postDelayed(this, 60000);
            }
            Log.d("timer working::", "timer worksing now");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStartLocation = (TextView)findViewById(R.id.start_location);
        mEndLocation = (TextView)findViewById(R.id.end_location);
        _start_location = new Location("start");
        _taxi_location = new Location("taxi");
        _result_taxi_location = new Location("resulttaxi");
//        mCheckTaxis.clear();

        mCancel = (Button)findViewById(R.id.btn_cancel);
        mCancel.setEnabled(true);
        BitmapDrawable bitmapdraw1 = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_start);
        Bitmap b1 = bitmapdraw1.getBitmap();
        start_smallMarker = Bitmap.createScaledBitmap(b1, width, height, false);
        BitmapDrawable bitmapdraw2 = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_end);
        Bitmap b2 = bitmapdraw2.getBitmap();
        end_smallMarker = Bitmap.createScaledBitmap(b2, width, height, false);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        startView();
        getTaxiInfo();
        mapFragment.getMapAsync(this);
        timerHandler.postDelayed(timerRunnable, 0);

    }

    private void startView(){
        start_location = Common.getInstance().getStart_location();
        end_location = Common.getInstance().getEnd_location();
        start_addresses = Common.getInstance().getStart_address();
        end_addresses = Common.getInstance().getEnd_address();
        pay_type = Common.getInstance().getPay_type();
        payPrice = Common.getInstance().getPay_amount();

        mStartLocation.setText(start_addresses);
        mEndLocation.setText(end_addresses);
        uniqueId = UUID.randomUUID().toString();
        driverUid = Common.getInstance().getDriver_uid();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());
        currentDateandTime = sdf.format(new Date());
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel_status = "user";
                mDatabase.getReference("ride/"+uniqueId).removeValue();
                sendNotification("Passenger Cancel the request");
                action();

                Intent intent = new Intent(FindDriverActivity.this, PaymentActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mUserRef = mDatabase.getReference("user/"+mAuth.getUid()+"/phonenumber");
        mValue_user = mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userPhone = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mdriverPhoneRef = mDatabase.getReference("user/"+driverUid+"/phonenumber");
        mValue_driverPhone = mdriverPhoneRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                driverPhone = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mRideCheckRef1 = mDatabase.getReference("ride/");
        mValue_driverCheck1 = mRideCheckRef1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot.getKey().equals(uniqueId)){
                    if(cancel_status.equals("user")){

                    }else{
                        action();
                        timerHandler.postDelayed(timerRunnable, 5);
                        Toast.makeText(FindDriverActivity.this, "Driver Cancel the request!", Toast.LENGTH_LONG).show();

                    }
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        mRideCheckRef = mDatabase.getReference("ride/"+uniqueId+"/status");
        mValue_driverCheck = mRideCheckRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String statusRide = dataSnapshot.getValue(String.class);
                if(statusRide != null){
                    if (statusRide.equals("accept")){
                        action();
                        Intent intent = new Intent(FindDriverActivity.this, UserRideActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    private void getTaxiInfo(){
        mRef = mDatabase.getReference("user/");
        mValue_ref = mRef.orderByChild("type").equalTo("driver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("datasa:", String.valueOf(dataSnapshot));
                mTaxis.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    String mUid= ds.getKey();
                    if(ds.child("join").getValue(String.class).equals("on")){
                        Double mLatitude= ds.child("latitude").getValue(Double.class);
                        Double mLongitude= ds.child("longitude").getValue(Double.class);
                        String mPhonenumber= ds.child("phonenumber").getValue(String.class);
                        String mPhoneToken= ds.child("phonetoken").getValue(String.class);

                        LatLng mLatLng = new LatLng(mLatitude, mLongitude);
                        Taxi _mTaxi = new Taxi(mLatLng,mUid,mPhonenumber,mPhoneToken);
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

    private void SearchDriver(){
        mCancel.setBackgroundResource(R.drawable.back_blue_button);
        mCurrentTaxis.clear();
        if(mTaxis.isEmpty()){
            Toast.makeText(FindDriverActivity.this,"No nearest taxi.", Toast.LENGTH_LONG).show();
            Log.d("no near:", "No nearest taxi");
        }else{
            if(!mCheckTaxis.isEmpty()){
                for(Taxi checkTaxi : mTaxis){
                    int i = 0;
                    for (Taxi mCheckTaxi : mCheckTaxis){
                        if(checkTaxi.getmTaxiUid().equals(mCheckTaxi.getmTaxiUid())){
                            i = 1;
                        }
                    }
                    if(i == 0){
                        mCurrentTaxis.add(checkTaxi);
                    }
                }
            }else{
                for(Taxi checkTaxi : mTaxis){
                    mCurrentTaxis.add(checkTaxi);
                }
            }
            if (mCurrentTaxis.isEmpty()){
                Toast.makeText(FindDriverActivity.this,"No nearest taxi.", Toast.LENGTH_LONG).show();
                action();
                Intent intent = new Intent(FindDriverActivity.this, PaymentActivity.class);
                startActivity(intent);
                finish();
//                finish();
            }else{
                result_taxi = mCurrentTaxis.get(0);
                for(Taxi oneTaxi : mCurrentTaxis){
                    Log.d("result taxi::", String.valueOf(result_taxi.getmTaxiLocation().latitude));
                    _taxi_location.setLatitude(result_taxi.getmTaxiLocation().latitude);
                    _taxi_location.setLongitude(result_taxi.getmTaxiLocation().longitude);
                    _result_taxi_location.setLatitude(oneTaxi.getmTaxiLocation().latitude);
                    _result_taxi_location.setLongitude(oneTaxi.getmTaxiLocation().longitude);
                    if(_start_location.distanceTo(_taxi_location) > _start_location.distanceTo(_result_taxi_location)){
                        result_taxi = oneTaxi;
                    }
                }
                mCheckTaxis.add(result_taxi);
                requestRide();
            }
        }
    }

    private void requestRide(){
        if(!userPhone.equals("no") && !driverPhone.equals("no")){
            mDatabase.getReference("ride/"+uniqueId+"/id").setValue(uniqueId);
            mDatabase.getReference("ride/"+uniqueId+"/driver").setValue(result_taxi.getmTaxiUid());
            mDatabase.getReference("ride/"+uniqueId+"/passenger").setValue(mAuth.getUid());
            mDatabase.getReference("ride/"+uniqueId+"/startlat").setValue(start_location.latitude);
            mDatabase.getReference("ride/"+uniqueId+"/startlong").setValue(start_location.longitude);
            mDatabase.getReference("ride/"+uniqueId+"/endlat").setValue(end_location.latitude);
            mDatabase.getReference("ride/"+uniqueId+"/endlong").setValue(end_location.longitude);
            mDatabase.getReference("ride/"+uniqueId+"/price").setValue(payPrice);
            mDatabase.getReference("ride/"+uniqueId+"/date").setValue(currentDateandTime);
            mDatabase.getReference("ride/"+uniqueId+"/paytype").setValue(pay_type);
            mDatabase.getReference("ride/"+uniqueId+"/status").setValue("waiting");
            mDatabase.getReference("ride/"+uniqueId+"/passengernumber").setValue(userPhone);
            mDatabase.getReference("ride/"+uniqueId+"/drivernumber").setValue(result_taxi.getmTaxiPhone());
            Common.getInstance().setRide_uuid(uniqueId);
            Common.getInstance().setDriver_uid(result_taxi.getmTaxiUid());
            sendNotification("Passenger Request a ride.");
            mCancel.setEnabled(true);
            mCancel.setTextColor(Color.parseColor("#ffffff"));
            mCancel.setBackgroundResource(R.drawable.back_pink_button);
            Log.d("status:", "Get all data");
        }
    }
    private void sendNotification(String Title){
        Map<String,Object> payMap = new HashMap<>();
        Map<String,Object> itemMap = new HashMap<>();
        payMap.put("to",result_taxi.getmTaxiToken());
        itemMap.put("body","From:" + Common.getInstance().getStart_address() + "\nto: " + Common.getInstance().getEnd_address() );
        itemMap.put("Title",Title);
//        itemMap.put("sound","default");
        payMap.put("notification",itemMap);
        String json = new Gson().toJson(payMap);
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(body)
                .addHeader("Authorization", "key=AAAADo3gnEI:APA91bF5Mgx1w0i2bmi_rP7U9v2ZF1ZVvvW4vjbQtjGdN1SjwVxF06yubBs5M1gNrkkj98XvUdCdxgwxrqM8EL5tGMX20uhbUo1wohLTuCFeu942AEjFby7J1LTdsdrdHlx_Io3fv0aA")
                .addHeader("Content-Type", "application/json")
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("error:", e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.d("response:", response.toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Cancel the request")
                .setMessage("You should cancel the request.");

        builder.setPositiveButton("Ok", null);
        builder.create().show();
    }
    private void action(){
        timerHandler.removeCallbacks(timerRunnable);
//        mRef.removeEventListener(mValue_ref);
//        mRideCheckRef1.removeEventListener(mValue_driverCheck1);
//        mRideCheckRef.removeEventListener(mValue_driverCheck);
//        mdriverPhoneRef.removeEventListener(mValue_driverPhone);
//        mUserRef.removeEventListener(mValue_user);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        MarkerOptions start_markerOptions = new MarkerOptions();
        MarkerOptions end_markerOptions = new MarkerOptions();
        start_markerOptions.position(start_location);
        end_markerOptions.position(end_location);
        start_markerOptions.title(start_addresses);
        end_markerOptions.title(end_addresses);

        start_markerOptions.icon(BitmapDescriptorFactory.fromBitmap(start_smallMarker));
        end_markerOptions.icon(BitmapDescriptorFactory.fromBitmap(end_smallMarker));

        googleMap.addMarker(start_markerOptions);
        googleMap.addMarker(end_markerOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(start_location,14));
    }
}