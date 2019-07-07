package com.uroos.myapplication.model;

import android.os.Parcel;
import android.os.Parcelable;

public class School implements Parcelable{
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public School createFromParcel(Parcel in) {
            return new School(in);
        }

        public School[] newArray(int size) {
            return new School[size];
        }
    };
    private String name;
    private String address;
    private String email;
    private String start_time;
    private String end_time;
    private String phone_no;
    private double latitude;
    private double longitude;
    private String dayFrom;
    private String dayTo;

    public School (){}

    public School(String name,
                  String address,
                  String email,
                  String start_time,
                  String end_time,
                  String phone_no,
                  double latitude,
                  double longitude,
                  String dayFrom,
                  String dayTo) {
        this.name = name;
        this.address = address;
        this.email = email;
        this.start_time = start_time;
        this.end_time = end_time;
        this.phone_no = phone_no;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dayFrom=dayFrom;
        this.dayTo=dayTo;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getStart_time() {
        return start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public double getLatitude(){ return latitude; }

    public double getLongitude() {
        return longitude;
    }

    public String getDayFrom() { return dayFrom; }

    public String getDayTo() { return dayTo; }

    @Override
    public String toString() {
        return "Name is " + name + ". Phone number is: " + phone_no;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setDayFrom(String dayFrom) { this.dayFrom = dayFrom; }

    public void setDayTo(String dayTo) { this.dayTo = dayTo; }

    // Parcelling part
    public School(Parcel in){
        this.name = in.readString();
        this.address = in.readString();
        this.email = in.readString();
        this.start_time = in.readString();
        this.end_time = in.readString();
        this.phone_no = in.readString();
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.dayFrom = in.readString();
        this.dayTo = in.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.address);
        dest.writeString(this.email);
        dest.writeString(this.start_time);
        dest.writeString(this.end_time);
        dest.writeString(this.phone_no);
        dest.writeDouble(this.latitude);
        dest.writeDouble(this.longitude);
        dest.writeString(this.dayFrom);
        dest.writeString(this.dayTo);
    }
}
