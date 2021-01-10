package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserProfileActivity extends AppCompatActivity {
    private EditText _firstName;
    private EditText _secondName;
    private EditText _phoneNumber;
    private EditText _emailAddress;
    private Button _saveChange;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        _firstName = (EditText) findViewById(R.id.user_firstname);
        _secondName = (EditText) findViewById(R.id.user_secondname);
        _phoneNumber = (EditText) findViewById(R.id.user_phonenumber);
        _emailAddress = (EditText) findViewById(R.id.user_email);
        _saveChange = (Button) findViewById(R.id.btn_savechange);
        _saveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setChange();
            }
        });
        setReady();
    }

    public void setReady(){
        mRef = mDatabase.getReference("user/"+mAuth.getUid());
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String firstName = snapshot.child("firstname").getValue(String.class);
                String secondName = snapshot.child("secondname").getValue(String.class);
                String phoneNumber = snapshot.child("phonenumber").getValue(String.class);
                String emailAddress = snapshot.child("email").getValue(String.class);
                if(firstName != null){
                    _firstName.setText(firstName);
                }
                if(secondName != null){
                    _secondName.setText(secondName);
                }
                if(phoneNumber != null){
                    _phoneNumber.setText(phoneNumber);
                }
                if(emailAddress != null){
                    _emailAddress.setText(emailAddress);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    public void setChange(){
        if(!validate()){
            Toast.makeText(this,"Input Empty Field", Toast.LENGTH_LONG).show();
            return;
        }
        String firstName = _firstName.getText().toString();
        String secondName = _secondName.getText().toString();
        String emailAddress = _emailAddress.getText().toString();
        mDatabase.getReference("user/"+mAuth.getUid()+"/firstname").setValue(firstName);
        mDatabase.getReference("user/"+mAuth.getUid()+"/secondname").setValue(secondName);
        mDatabase.getReference("user/"+mAuth.getUid()+"/email").setValue(emailAddress);
        Toast.makeText(this,"Update Success", Toast.LENGTH_LONG).show();
    }
    public Boolean validate(){
        boolean valid = true;
        String userfirstname = _firstName.getText().toString();
        String usersecondname = _secondName.getText().toString();
        String useremailAddress = _emailAddress.getText().toString();
        if (userfirstname.isEmpty()) {
            _firstName.setError("Input Firstname");
            valid = false;
        } else {
            _firstName.setError(null);
        }
        if (usersecondname.isEmpty()) {
            _secondName.setError("Input Lastname");
            valid = false;
        } else {
            _secondName.setError(null);
        }

        if (useremailAddress.isEmpty()) {
            _emailAddress.setError("Input Email");
            valid = false;
        } else {
            String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(useremailAddress);
            if (matcher.matches()){
                _emailAddress.setError(null);
            }else{
                _emailAddress.setError("Input Correct  email address");
                valid = false;
            }
        }
        return valid;
    }
}