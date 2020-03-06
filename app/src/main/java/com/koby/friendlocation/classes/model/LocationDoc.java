package com.koby.friendlocation.classes.model;

public class LocationDoc {

    private String date;
    private String address;
    private double latitude;
    private double longitude;

    public LocationDoc() {
    }

    public LocationDoc(String date, String address, double latitude, double longitude) {
        this.date = date;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
