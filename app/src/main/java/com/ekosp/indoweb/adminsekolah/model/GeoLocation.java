package com.ekosp.indoweb.adminsekolah.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Eko Setyo Purnomo on 06-Apr-18.
 * Find me on https://ekosp.com
 * Or email me directly to ekosetyopurnomo@gmail.com
 */

public class GeoLocation implements Parcelable{

    private String address;
    private String city;
    private String state;
    private String county;
    private String postalCode;
    private String knownName;
    private float distance;

    protected GeoLocation(Parcel in) {
        address = in.readString();
        city = in.readString();
        state = in.readString();
        county = in.readString();
        postalCode = in.readString();
        knownName = in.readString();
        distance = in.readFloat();
    }

    public GeoLocation(String address, String city, String state, String county, String postalCode, String knownName, float distance) {
        this.address = address;
        this.city = city;
        this.state = state;
        this.county = county;
        this.postalCode = postalCode;
        this.knownName = knownName;
        this.distance = distance;
    }

    public static final Creator<GeoLocation> CREATOR = new Creator<GeoLocation>() {
        @Override
        public GeoLocation createFromParcel(Parcel in) {
            return new GeoLocation(in);
        }

        @Override
        public GeoLocation[] newArray(int size) {
            return new GeoLocation[size];
        }
    };

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getKnownName() {
        return knownName;
    }

    public void setKnownName(String knownName) {
        this.knownName = knownName;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(address);
        parcel.writeString(city);
        parcel.writeString(state);
        parcel.writeString(county);
        parcel.writeString(postalCode);
        parcel.writeString(knownName);
        parcel.writeFloat(distance);
    }
}
