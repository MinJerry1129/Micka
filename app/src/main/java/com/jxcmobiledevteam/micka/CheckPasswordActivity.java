package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class CheckPasswordActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;
    private Button _confirm;
    private EditText _password;
    private String _str_password;
    private TextView _wrongPass;
    private ProgressDialog progressDialog;
    private String firstlogin="no";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_password);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabase.getReference("user/"+mAuth.getUid()+"/status").setValue("off");
        _confirm= (Button)findViewById(R.id.btn_confrim);
        _password= (EditText)findViewById(R.id.txt_password);
        _wrongPass = (TextView)findViewById(R.id.txt_password);
        _confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPassword();
            }
        });
        progressDialog = new ProgressDialog(this, R.style.AppTheme_Bright_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.setCancelable(false);
    }
    private void checkPassword(){
        progressDialog.show();
        _str_password = _password.getText().toString();
        mRef = mDatabase.getReference("user/"+mAuth.getUid()+"/password");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                progressDialog.dismiss();
                String value = dataSnapshot.getValue(String.class);
                if(value !=null){
                    if(firstlogin.equals("no")){
                        if(value.equals(_str_password)){
                            _wrongPass.setVisibility(View.GONE);
                            mDatabase.getReference("user/"+mAuth.getUid()+"/status").setValue("on");
                            mDatabase.getReference("user/"+mAuth.getUid()+"/type").setValue("user");
                            mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");
                            startApp();
                        }else{
                            _wrongPass.setVisibility(View.VISIBLE);
                            Toast.makeText(CheckPasswordActivity.this, "Password is wrong", Toast.LENGTH_LONG).show();
                            Log.d("password:", "Password is wrong");
                        }
                    }else{
                        startSignup();
                    }

                    Log.d("password:", value);
                }else{
                    mDatabase.getReference("user/"+mAuth.getUid()+"/enable").setValue("no");
                    mDatabase.getReference("user/"+mAuth.getUid()+"/phonenumber").setValue(Common.getInstance().getPhonenumber());
                    mDatabase.getReference("user/"+mAuth.getUid()+"/phonetoken").setValue(FirebaseInstanceId.getInstance().getToken());

                    mDatabase.getReference("user/"+mAuth.getUid()+"/status").setValue("on");
                    mDatabase.getReference("user/"+mAuth.getUid()+"/type").setValue("user");
                    mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");
                    mDatabase.getReference("user/"+mAuth.getUid()+"/latitude").setValue(0.0);
                    mDatabase.getReference("user/"+mAuth.getUid()+"/longitude").setValue(0.0);
                    mDatabase.getReference("user/"+mAuth.getUid()+"/firstname").setValue("");
                    mDatabase.getReference("user/"+mAuth.getUid()+"/secondname").setValue("");
                    mDatabase.getReference("user/"+mAuth.getUid()+"/email").setValue("");
                    firstlogin = "yes";
                    mRef.setValue(_str_password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void startApp(){
        Intent intent = new Intent(CheckPasswordActivity.this, FirstPageActivity.class);
        startActivity(intent);
        finish();
    }

    private void startSignup(){
        Intent intent = new Intent(CheckPasswordActivity.this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(CheckPasswordActivity.this, PhoneActivity.class);
        startActivity(intent);
        finish();
    }
}