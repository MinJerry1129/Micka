package com.jxcmobiledevteam.micka;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class SplashActivity extends AppCompatActivity {
    String androidDeviceId = "";
    private int SPLASH_TIME = 1000;
    private boolean firstFlag;
    private boolean secondFlag;
    private static final int REQUEST_LOCATION = 111;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]
                    {ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }else{
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    moveToLogin();
                }
            },1000);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
//        Log.d("grantResultsLength", Integer.toString(requestCode));
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            Handler handler=new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    moveToLogin();
                }
            },1000);
        }else{
            new AlertDialog.Builder(SplashActivity.this)
                    .setMessage(getResources().getString(R.string.permission_deny))
                    .setCancelable(false)
                    .setPositiveButton("ok", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finishAffinity();
                            System.exit(0);
                        }
                    }).show();
        }
//        Toast.makeText(this, "test", Toast.LENGTH_SHORT).show();
    }
    private void moveToLogin(){
        Intent intent = new Intent(SplashActivity.this, CheckUserActivity.class);
        startActivity(intent);
        finish();
    }
}