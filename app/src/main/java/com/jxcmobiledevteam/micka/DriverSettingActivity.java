package com.jxcmobiledevteam.micka;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class DriverSettingActivity extends AppCompatActivity {
    Button _btn_mode;
    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_setting);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        _btn_mode = (Button)findViewById(R.id.btn_mode);
        _btn_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Common.getInstance().getJon_status().equals("on")){
                    Toast.makeText(DriverSettingActivity.this, "Please Offline.", Toast.LENGTH_LONG ).show();
                }else{
                    mDatabase.getReference("user/"+mAuth.getUid()+"/type").setValue("user");
                    mDatabase.getReference("user/"+mAuth.getUid()+"/join").setValue("off");

                    Intent intent=new Intent(getBaseContext(), FirstPageActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent(this, MainDriverActivity.class);
        startActivity(intent);
        finish();
    }
}