package com.jxcmobiledevteam.micka;

import com.google.android.gms.maps.model.LatLng;

public class Taxi {
    private LatLng mTaxiLocation;
    private String mTaxiUid;

    public Taxi(LatLng taxiLocation, String taxiUid){
        mTaxiLocation=taxiLocation;
        mTaxiUid = taxiUid;
    }

    public LatLng getmTaxiLocation() {
        return mTaxiLocation;
    }
    public String getmTaxiUid() {
        return mTaxiUid;
    }
}
