package com.koby.friendlocation.classes.model;

public class LocationDoc {

    private double latitude;
    private double longitude;

    public LocationDoc() {
    }

    public LocationDoc(String userUid, double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
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
