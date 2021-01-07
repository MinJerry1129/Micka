package com.jxcmobiledevteam.micka;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class UserSettingActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    DatabaseReference mRef;

    Button _btn_help;
    Button _btn_mode;
    Button _btn_logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();

        _btn_help = (Button)findViewById(R.id.btn_help);
        _btn_mode = (Button)findViewById(R.id.btn_mode);
        _btn_logout = (Button)findViewById(R.id.btn_logout);
        _btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openHelp();
            }
        });
        _btn_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMode();
            }
        });
        _btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });
    }
    public void openHelp(){
        Intent intent = new Intent(UserSettingActivity.this, HelpActivity.class);
        startActivity(intent);
    }
    public void logOut(){
        mAuth.signOut();
        Intent intent = new Intent(UserSettingActivity.this, CheckUserActivity.class);
        startActivity(intent);
        finish();
    }

    public void openMode(){
        mRef = mDatabase.getReference("user/"+mAuth.getUid() + "/enable");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String driver_enabled = dataSnapshot.getValue(String.class);
                if(driver_enabled.equals("yes")){
                    String phone_token = FirebaseInstanceId.getInstance().getToken();
                    Log.d("phone token::", phone_token);
                    Common.getInstance().setPhone_token(phone_token);
                    mDatabase.getReference("user/"+mAuth.getUid()+"/type").setValue("driver");
                    mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("on");
                    mDatabase.getReference("user/"+mAuth.getUid()+"/phonetoken").setValue(phone_token);

                    Intent intent = new Intent(UserSettingActivity.this, MainDriverActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(UserSettingActivity.this, "You are not enable to driver", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}