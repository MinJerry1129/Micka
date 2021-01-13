package com.jxcmobiledevteam.micka;

import com.google.android.gms.maps.model.LatLng;

public class Taxi {
    private LatLng mTaxiLocation;
    private String mTaxiUid;
    private String mTaxiPhone;

    public Taxi(LatLng taxiLocation, String taxiUid, String taxiPhone){
        mTaxiLocation=taxiLocation;
        mTaxiUid = taxiUid;
        mTaxiPhone = taxiPhone;
    }

    public LatLng getmTaxiLocation() {
        return mTaxiLocation;
    }
    public String getmTaxiUid() {
        return mTaxiUid;
    }
    public String getmTaxiPhone() {return mTaxiPhone;}
}
