package com.jxcmobiledevteam.micka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;
    private EditText firstName;
    private EditText lastName;
    private EditText emailAddress;
    private Button btnConfirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        firstName = (EditText)findViewById(R.id.user_firstname);
        lastName = (EditText)findViewById(R.id.user_secondname);
        emailAddress = (EditText)findViewById(R.id.user_email);
        btnConfirm = (Button)findViewById(R.id.btn_confirm);
        setupView();

    }
    private void setupView(){
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputData();
            }
        });
    }
    private void InputData(){
        if(!validate()){
            Toast.makeText(SignUpActivity.this,"Input Info.", Toast.LENGTH_LONG).show();
            return;
        }
        mDatabase.getReference("user/"+mAuth.getUid()+"/firstname").setValue(firstName.getText().toString());
        mDatabase.getReference("user/"+mAuth.getUid()+"/secondname").setValue(lastName.getText().toString());
        mDatabase.getReference("user/"+mAuth.getUid()+"/email").setValue(emailAddress.getText().toString());
        startApp();
    }
    private void startApp(){
        Intent intent = new Intent(SignUpActivity.this, FirstPageActivity.class);
        startActivity(intent);
        finish();
    }

    public boolean validate() {
        boolean valid = true;
        String userfirstname = firstName.getText().toString();
        String usersecondname = lastName.getText().toString();
        String useremailAddress = emailAddress.getText().toString();
        if (userfirstname.isEmpty()) {
            firstName.setError("Input Firstname");
            valid = false;
        } else {
            firstName.setError(null);
        }
        if (usersecondname.isEmpty()) {
            lastName.setError("Input Lastname");
            valid = false;
        } else {
            lastName.setError(null);
        }

        if (useremailAddress.isEmpty()) {
            emailAddress.setError("Input Email");
            valid = false;
        } else {
            String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(useremailAddress);
            if (matcher.matches()){
                emailAddress.setError(null);
            }else{
                emailAddress.setError("Input Correct  email address");
                valid = false;
            }
        }
        return valid;
    }
}