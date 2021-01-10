package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.common.reflect.TypeToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.PaymentIntentResult;
import com.stripe.android.Stripe;
import com.stripe.android.model.ConfirmPaymentIntentParams;
import com.stripe.android.model.PaymentIntent;
import com.stripe.android.model.PaymentMethodCreateParams;
import com.stripe.android.view.CardInputWidget;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserRideActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private DatabaseReference mRideRef;
    private TextView _driverName;
    private TextView _startAddress;
    private TextView _endAddress;
    private TextView _carNumber;
    private ImageView _callPhone;
    private Button _btnBook;
    private Button _btnCancel;
    private SupportMapFragment mapFragment;

    private int height = 100;
    private int width = 100;
    private Bitmap start_smallMarker;
    private Bitmap end_smallMarker;
    private String driverUid;
    private String phoneToken;
    private String phoneNumber;
    private String bookingStatus="none";
    private String uniqueId;
    private String pay_type;
    private Boolean checkdriver = true;
    private String startAddress;
    private String endAddress;
    private LatLng userLocation;
    private LatLng driverLocation;
    private double driverLat;
    private double driverLong;

    String firstname = "";
    String secondName = "";

    //payment
    private static final String BACKEND_URL = "https://www.micka-vtc.fr/stripe/";
    private OkHttpClient httpClient = new OkHttpClient();
    private String paymentIntentClientSecret;
    private Stripe stripe;
    private Double pay_amount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_ride);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        pay_amount = Common.getInstance().getPay_amount();
        stripe = new Stripe(
                getApplicationContext(),
                Objects.requireNonNull("pk_test_51HzziMAAOlrKKdKYJNlU1BeTmyfGvsW5fi0rtA5wLJLvzEj0FdPK6hSyAxcqPqDynKFqLdXw5CcdHXHDJVgRihhR00318CxHuF")
        );

        driverUid = Common.getInstance().getDriver_uid();
        pay_type = Common.getInstance().getPay_type();
        startAddress =Common.getInstance().getStart_address();
        endAddress = Common.getInstance().getEnd_address();
        uniqueId = Common.getInstance().getRide_uuid();
        userLocation = Common.getInstance().getStart_location();

        startView();
    }
    private void startView(){
        _driverName = (TextView)findViewById(R.id.txt_drivername);
        _carNumber = (TextView)findViewById(R.id.txt_carnumber);
        _startAddress = (TextView)findViewById(R.id.start_address);
        _endAddress = (TextView)findViewById(R.id.end_address);
        _callPhone = (ImageView)findViewById(R.id.img_phone);
        _btnBook = (Button)findViewById(R.id.btn_booking);
        _btnCancel = (Button)findViewById(R.id.btn_cancel);

        double amount = pay_amount*100;
        Map<String,Object> payMap = new HashMap<>();
        Map<String,Object> itemMap = new HashMap<>();
        List<Map<String,Object>> itemList = new ArrayList<>();
        payMap.put("currency","eur");
        itemMap.put("id","Micka Taxi");
        itemMap.put("amount",amount);
        itemList.add(itemMap);
        payMap.put("items",itemList);
        String json = new Gson().toJson(payMap);
        MediaType mediaType = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json, mediaType);
        Request request = new Request.Builder()
                .url(BACKEND_URL + "create.php")
                .post(body)
                .build();
        httpClient.newCall(request)
                .enqueue(new UserRideActivity.PayCallback(this));


        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        BitmapDrawable bitmapdraw1 = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_start);
        Bitmap b1 = bitmapdraw1.getBitmap();
        start_smallMarker = Bitmap.createScaledBitmap(b1, width, height, false);
        BitmapDrawable bitmapdraw2 = (BitmapDrawable)getResources().getDrawable(R.drawable.ic_car_front);
        Bitmap b2 = bitmapdraw2.getBitmap();
        end_smallMarker = Bitmap.createScaledBitmap(b2, width, height, false);

        _startAddress.setText(startAddress);
        _endAddress.setText(endAddress);



        mRef = mDatabase.getReference("user/"+driverUid);
        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.getKey().equals("firstname")){
                    firstname = dataSnapshot.getValue().toString();
                    _driverName.setText(firstname + secondName);
                }
                if (dataSnapshot.getKey().equals("secondname")){
                    secondName = dataSnapshot.getValue().toString();
                    _driverName.setText(firstname + secondName);
                }
                if (dataSnapshot.getKey().equals("latitude")){
                    driverLat = dataSnapshot.getValue(double.class);
                }
                if (dataSnapshot.getKey().equals("longitude")){
                    driverLong = dataSnapshot.getValue(double.class);
                    driverLocation = new LatLng(driverLat, driverLong);
                    mapFragment.getMapAsync(UserRideActivity.this);
                }
                if (dataSnapshot.getKey().equals("phonenumber")){
                    phoneNumber = dataSnapshot.getValue().toString();
                }
                if (dataSnapshot.getKey().equals("phonetoken")){
                    phoneToken = dataSnapshot.getValue().toString();
                }
                if (dataSnapshot.getKey().equals("carnum")){
                    _carNumber.setText(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getKey().equals("latitude")){
                    driverLat = dataSnapshot.getValue(double.class);
                    driverLocation = new LatLng(driverLat, driverLong);
                    mapFragment.getMapAsync(UserRideActivity.this);
                }
                if (dataSnapshot.getKey().equals("longitude")){
                    driverLong = dataSnapshot.getValue(double.class);
                    driverLocation = new LatLng(driverLat, driverLong);
                    mapFragment.getMapAsync(UserRideActivity.this);
                }
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


        mRideRef = mDatabase.getReference("ride/"+uniqueId);
        mRideRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                bookingStatus = dataSnapshot.child("status").getValue(String.class);
                if (bookingStatus == null){
                    finish();
                }else{
                    if (bookingStatus.equals("waiting")){
                        _btnBook.setText("Waiting");
                        _btnBook.setEnabled(false);
                        _btnCancel.setEnabled(true);
                    }else if (bookingStatus.equals("accept")){
                        _btnBook.setText("accepted");
                        _btnBook.setEnabled(false);
                        _btnCancel.setEnabled(true);
                        Toast.makeText(UserRideActivity.this, "Driver accept your booking", Toast.LENGTH_LONG).show();
                    }else if (bookingStatus.equals("pickup")){
                        userLocation = Common.getInstance().getEnd_location();
                        _btnBook.setText("P a y");
                        _btnBook.setEnabled(true);
                        mapFragment.getMapAsync(UserRideActivity.this);
                    }else if (bookingStatus.equals("pay")){
                        userLocation = Common.getInstance().getEnd_location();
                        _btnBook.setText("Paid");
                        _btnBook.setEnabled(false);
                        _btnCancel.setEnabled(false);
                        mapFragment.getMapAsync(UserRideActivity.this);
                    }else if (bookingStatus.equals("paid")){
                        userLocation = Common.getInstance().getEnd_location();
                        _btnBook.setEnabled(true);
                        _btnBook.setText("Complete");
                        _btnCancel.setEnabled(false);
                        mapFragment.getMapAsync(UserRideActivity.this);
                    }else if (bookingStatus.equals("complete")){
                        userLocation = Common.getInstance().getEnd_location();
                        _btnBook.setEnabled(true);
                        _btnBook.setText("Complete");
                        _btnCancel.setEnabled(false);
                        mapFragment.getMapAsync(UserRideActivity.this);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        _callPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(UserRideActivity.this,
                        Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(UserRideActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 1);

                }else{
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                    startActivity(intent);
                }
            }
        });
        _btnBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bookingRide();
            }
        });
        _btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabase.getReference("ride/"+uniqueId).removeValue();
                sendNotification("Passenger Cancel Requst.");
                finish();
            }
        });
    }
    private void bookingRide(){
        if(!checkdriver){

        }else{
            if(bookingStatus.equals("none")){
            }else if (bookingStatus.equals("waiting")){
            }else if (bookingStatus.equals("accept")){
                Toast.makeText(UserRideActivity.this, "Driver accept your booking", Toast.LENGTH_LONG).show();
            }else if (bookingStatus.equals("pickup")){
                if(pay_type.equals("card")){
                    payFunction();
                }else{
                    mDatabase.getReference("ride/"+uniqueId+"/status").setValue("pay");
                }
            }else if (bookingStatus.equals("pay")){
            }else if (bookingStatus.equals("paid")){
                mDatabase.getReference("ride/"+uniqueId+"/status").setValue("complete");
            }else if (bookingStatus.equals("complete")){
                finish();
            }
        }
    }

    private void payFunction(){
        PaymentMethodCreateParams params = Common.getInstance().getParams();
        ConfirmPaymentIntentParams confirmParams = ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(params, paymentIntentClientSecret);
        Log.d("adfadfa:::", String.valueOf(confirmParams));
        stripe.confirmPayment(UserRideActivity.this, confirmParams);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        MarkerOptions start_markerOptions = new MarkerOptions();
        MarkerOptions end_markerOptions = new MarkerOptions();
        start_markerOptions.position(userLocation);
        end_markerOptions.position(driverLocation);

        start_markerOptions.icon(BitmapDescriptorFactory.fromBitmap(start_smallMarker));
        end_markerOptions.icon(BitmapDescriptorFactory.fromBitmap(end_smallMarker));

        googleMap.addMarker(start_markerOptions);
        googleMap.addMarker(end_markerOptions);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation,14));
    }

    private void sendNotification(String Title){
        Map<String,Object> payMap = new HashMap<>();
        Map<String,Object> itemMap = new HashMap<>();
        payMap.put("to",phoneToken);
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

    }

    private void displayAlert(@NonNull String title,
                              @Nullable String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message);

        builder.setPositiveButton("Ok", null);
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, new UserRideActivity.PaymentResultCallback(this));
    }

    private void onPaymentSuccess(@NonNull final Response response) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> responseMap = gson.fromJson(
                Objects.requireNonNull(response.body()).string(),
                type
        );

        paymentIntentClientSecret = responseMap.get("clientSecret");
    }

    private final class PayCallback implements Callback {
        @NonNull private final WeakReference<UserRideActivity> activityRef;

        PayCallback(@NonNull UserRideActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            final UserRideActivity activity = activityRef.get();
            Log.d("failure::::", e.getMessage());
            if (activity == null) {
                return;
            }
            Log.d("failure::::", e.getMessage());
//            Toast.makeText(CheckoutPaymentActivity.this, "Error: " + e.toString(), Toast.LENGTH_LONG ).show();
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull final Response response)
                throws IOException {
            final UserRideActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            if (!response.isSuccessful()) {
                Toast.makeText(activity, "Error: " + response.toString(), Toast.LENGTH_LONG).show();
            } else {
                activity.onPaymentSuccess(response);
            }
        }
    }

    private static final class PaymentResultCallback
            implements ApiResultCallback<PaymentIntentResult> {
        @NonNull private final WeakReference<UserRideActivity> activityRef;
        public FirebaseDatabase mDatabase;
        PaymentResultCallback(@NonNull UserRideActivity activity) {
            activityRef = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(@NonNull PaymentIntentResult result) {
            mDatabase = FirebaseDatabase.getInstance();
            final UserRideActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            PaymentIntent paymentIntent = result.getIntent();
            PaymentIntent.Status status = paymentIntent.getStatus();
            if (status == PaymentIntent.Status.Succeeded) {
                // Payment completed successfully
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                activity.displayAlert(
                        "Payment completed",
                        "Paid " + Common.getInstance().getPay_amount() + "€"
                );

                mDatabase.getReference("ride/"+Common.getInstance().getRide_uuid()+"/status").setValue("pay");
            } else if (status == PaymentIntent.Status.RequiresPaymentMethod) {
                // Payment failed – allow retrying using a different payment method
                activity.displayAlert(
                        "Payment failed",
                        Objects.requireNonNull(paymentIntent.getLastPaymentError()).getMessage()
                );
            }
        }

        @Override
        public void onError(@NonNull Exception e) {
            final UserRideActivity activity = activityRef.get();
            if (activity == null) {
                return;
            }

            // Payment request failed – allow retrying using the same payment method
            activity.displayAlert("Error", e.toString());
        }
    }
}