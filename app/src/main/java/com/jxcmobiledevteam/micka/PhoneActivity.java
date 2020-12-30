package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.executor.TaskExecutor;

import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

import javax.xml.validation.Validator;

public class PhoneActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    Button _confirmBtn;
    EditText _phoneNumber;
    CountryCodePicker _cCodePicker;
    int _valide_number = 0;
    String _phone_Number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);
        mAuth = FirebaseAuth.getInstance();

        _confirmBtn = (Button)findViewById(R.id.btn_confrim);
        _phoneNumber = (EditText) findViewById(R.id.et_phone);
        _cCodePicker = (CountryCodePicker) findViewById(R.id.ccp);
        assignViews();
        _confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmPhone();
            }
        });
    }

    public void assignViews(){
        _cCodePicker.registerCarrierNumberEditText(_phoneNumber);
        _cCodePicker.setPhoneNumberValidityChangeListener(new CountryCodePicker.PhoneNumberValidityChangeListener() {
            @Override
            public void onValidityChanged(boolean isValidNumber) {
                if(isValidNumber){
                    _phone_Number = _cCodePicker.getFullNumberWithPlus();
                    _valide_number = 1;
//                    Toast.makeText(PhoneActivity.this, _phone_Number, Toast.LENGTH_SHORT).show();
                }else{
                    _valide_number = 0;
                }
            }
        });
    }

    public void confirmPhone(){
        if(_valide_number == 1){
            Common.getInstance().setPhonenumber(_phone_Number);
            Intent intent = new Intent(getBaseContext(), ConfirmActivity.class);
            intent.putExtra("phonenumber", _phone_Number);
            startActivity(intent);
            finish();
        }else{
            Toast.makeText(PhoneActivity.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
        }
    }
}