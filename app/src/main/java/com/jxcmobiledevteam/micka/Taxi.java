package com.jxcmobiledevteam.micka;

import com.google.android.gms.maps.model.LatLng;

public class Taxi {
    private LatLng mTaxiLocation;
    private String mTaxiUid;
    private String mTaxiPhone;
    private String mTaxiToken;


    public Taxi(LatLng taxiLocation, String taxiUid, String taxiPhone, String taxiToken){
        mTaxiLocation=taxiLocation;
        mTaxiUid = taxiUid;
        mTaxiPhone = taxiPhone;
        mTaxiToken = taxiToken;
    }

    public LatLng getmTaxiLocation() {
        return mTaxiLocation;
    }
    public String getmTaxiUid() {
        return mTaxiUid;
    }
    public String getmTaxiPhone() {return mTaxiPhone;}
    public String getmTaxiToken() {return mTaxiToken;}
}
