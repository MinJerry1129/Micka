package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;

import java.util.concurrent.TimeUnit;

public class ConfirmActivity extends AppCompatActivity{
//    OtpView _confirmCode;
    EditText _confirmCode;
    private String _phonenumber;
    private String verificationId;
    private FirebaseAuth mAuth;
    private TextView _phone_number;
    private TextView _sendCode;
    Button _btn_confirm;
    TextView _wrongCode;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);
        mAuth = FirebaseAuth.getInstance();
        _phonenumber = getIntent().getStringExtra("phonenumber");
        _confirmCode = (EditText) findViewById(R.id.confirm_code);
        _phone_number = (TextView)findViewById(R.id.phone_number);
        _btn_confirm = (Button)findViewById(R.id.btn_confrim);
        _wrongCode = (TextView)findViewById(R.id.txt_wrongcode);
        _sendCode = (TextView)findViewById(R.id.send_again);

        _phone_number.setText(_phonenumber);
        progressDialog = new ProgressDialog(this, R.style.AppTheme_Bright_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.setCancelable(false);
        _sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerificationCode(_phonenumber);
            }
        });

        sendVerificationCode(_phonenumber);
        ConfirmCode();
    }
    private void sendVerificationCode(String phonenumber){
//        PhoneAuthOptions options =
//                PhoneAuthOptions.newBuilder(mAuth)
//                        .setPhoneNumber(phonenumber)       // Phone number to verify
//                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
//                        .setActivity(this)                 // Activity (for callback binding)
//                        .setCallbacks(mCallBack)          // OnVerificationStateChangedCallbacks
//                        .build();
//        PhoneAuthProvider.verifyPhoneNumber(options);

//        progressDialog.show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phonenumber,60, TimeUnit.SECONDS, TaskExecutors.MAIN_THREAD, mCallBack);
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            progressDialog.dismiss();
            String code = phoneAuthCredential.getSmsCode();
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(ConfirmActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void verifyCode(String code){
        progressDialog.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful()){
                            _wrongCode.setVisibility(View.GONE);
                            FirebaseUser user = task.getResult().getUser();
                            Log.d("user uid:", user.getUid());
                            Intent intent = new Intent(ConfirmActivity.this, CheckPasswordActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }else{
                            _wrongCode.setVisibility(View.VISIBLE);
                            _confirmCode.getText().clear();
                            Toast.makeText(ConfirmActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("Error:", task.getException().getMessage());
                        }
                    }
                });
    }

    private void ConfirmCode(){
        _btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyCode(_confirmCode.getText().toString());
            }
        });

//        _confirmCode.setOtpCompletionListener(new OnOtpCompletionListener() {
//            @Override
//            public void onOtpCompleted(final String otp) {
////                Toast.makeText(ConfirmActivity.this, otp, Toast.LENGTH_SHORT).show();
//                Handler handler=new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        verifyCode(otp);
////                        Intent intent = new Intent(ConfirmActivity.this, CheckPasswordActivity.class);
////                        startActivity(intent);
////                        finish();
////                        _confirmCode.getText().clear();
//                    }
//                },1000);
//
//            }
//        });
    }
}