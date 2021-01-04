package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private DatabaseReference mdriverTokenRef;
    private DatabaseReference mdriverPhoneRef;
    private DatabaseReference mRideCheckRef;
    private DatabaseReference mUserRef;

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
    private String drivertoken = "no";
    private String driverPhone = "no";
    private String pay_type;
    private Double payPrice;

    private OkHttpClient httpClient = new OkHttpClient();
    String currentDateandTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_driver);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mStartLocation = (TextView)findViewById(R.id.start_location);
        mEndLocation = (TextView)findViewById(R.id.end_location);
        mCancel = (Button)findViewById(R.id.btn_cancel);

        BitmapDrawable bitmapdraw1 = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_start);
        Bitmap b1 = bitmapdraw1.getBitmap();
        start_smallMarker = Bitmap.createScaledBitmap(b1, width, height, false);
        BitmapDrawable bitmapdraw2 = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_end);
        Bitmap b2 = bitmapdraw2.getBitmap();
        end_smallMarker = Bitmap.createScaledBitmap(b2, width, height, false);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        startView();

        mapFragment.getMapAsync(this);

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
                mDatabase.getReference("ride/"+uniqueId).removeValue();
                sendNotification("Passenger Cancel the request");
                finish();
            }
        });

        mUserRef = mDatabase.getReference("user/"+mAuth.getUid()+"/phonenumber");
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userPhone = dataSnapshot.getValue(String.class);
                requestRide();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mdriverTokenRef = mDatabase.getReference("user/"+driverUid+"/phonetoken");
        mdriverTokenRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                drivertoken = dataSnapshot.getValue(String.class);
                requestRide();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mdriverPhoneRef = mDatabase.getReference("user/"+driverUid+"/phonenumber");
        mdriverPhoneRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                driverPhone = dataSnapshot.getValue(String.class);
                requestRide();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        mRideCheckRef = mDatabase.getReference("ride/"+uniqueId+"/status");
        mRideCheckRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String statusRide = dataSnapshot.getValue(String.class);
                if(statusRide != null){
                    if (statusRide.equals("accept")){
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

    private void requestRide(){
        if(!userPhone.equals("no") && !driverPhone.equals("no") && !drivertoken.equals("no")){
            mDatabase.getReference("ride/"+uniqueId+"/id").setValue(uniqueId);
            mDatabase.getReference("ride/"+uniqueId+"/driver").setValue(driverUid);
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
            mDatabase.getReference("ride/"+uniqueId+"/drivernumber").setValue(driverPhone);
            Common.getInstance().setRide_uuid(uniqueId);
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
        payMap.put("to",drivertoken);
        itemMap.put("body","From:" + Common.getInstance().getStart_address() + "\nto: " + Common.getInstance().getEnd_address() );
        itemMap.put("Title",Title);
        payMap.put("notification",itemMap);
        String json = new Gson().toJson(payMap);
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(mediaType, json);
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(body)
                .addHeader("Authorization", "key=AAAADo3gnEI: APA91bF5Mgx1w0i2bmi_rP7U9v2ZF1ZVvvW4vjbQtjGdN1SjwVxF06yubBs5M1gNrkkj98XvUdCdxgwxrqM8EL5bTHUCoFd0uhd7fxlfxwxrqM8EL5bTGuCoFd0uhd7fxlfwxwxrqM8EL5bTHuCoFx20uoh")
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
        mDatabase.getReference("ride/"+uniqueId).removeValue();
        finish();
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