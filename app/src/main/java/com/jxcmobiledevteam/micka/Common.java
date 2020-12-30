package com.jxcmobiledevteam.micka;

import com.google.android.gms.maps.model.LatLng;
import com.stripe.android.model.PaymentMethodCreateParams;

public class Common {
    private LatLng start_location;
    private LatLng end_location;
    private String phonenumber;
    private String jon_status;
    private Double pay_amount;
    private String phone_token;
    private String driver_uid;
    private String pay_type;
    private String start_address;
    private String end_address;
    private String pay_status;
    private String ride_uuid;
    private PaymentMethodCreateParams params;
    private String paymentIntentClientSecret;


    private static Common instance = new Common();

    public static Common getInstance() {return instance;}

    public LatLng getStart_location() {return start_location;}

    public void setStart_location(LatLng start_location) {this.start_location = start_location;}

    public LatLng getEnd_location() {return end_location;}

    public void setEnd_location(LatLng end_location) {this.end_location = end_location;}

    public String getPhonenumber() {return phonenumber;}

    public void setPhonenumber(String phonenumber) {this.phonenumber = phonenumber;}

    public String getJon_status() {return jon_status;}

    public void setJon_status(String jon_status) {this.jon_status = jon_status;}

    public Double getPay_amount() {return pay_amount;}

    public void setPay_amount(Double pay_amount) { this.pay_amount = pay_amount;}

    public String getPhone_token() { return phone_token;}

    public void setPhone_token(String phone_token) { this.phone_token = phone_token;}

    public String getDriver_uid() { return driver_uid;}

    public void setDriver_uid(String driver_uid) { this.driver_uid = driver_uid;}

    public String getPay_type() { return pay_type;}

    public void setPay_type(String pay_type) { this.pay_type = pay_type; }

    public String getStart_address() { return start_address; }

    public void setStart_address(String start_address) { this.start_address = start_address; }

    public void setEnd_address(String end_address) { this.end_address = end_address; }

    public String getEnd_address() { return end_address; }

    public String getPay_status() {return pay_status; }

    public void setPay_status(String pay_status) { this.pay_status = pay_status; }

    public String getRide_uuid() { return ride_uuid; }

    public void setRide_uuid(String ride_uuid) { this.ride_uuid = ride_uuid; }

    public PaymentMethodCreateParams getParams() {return params;}

    public void setParams(PaymentMethodCreateParams params) {this.params = params;}

    public String getPaymentIntentClientSecret() { return paymentIntentClientSecret;}

    public void setPaymentIntentClientSecret(String paymentIntentClientSecret) {this.paymentIntentClientSecret = paymentIntentClientSecret;}
}
