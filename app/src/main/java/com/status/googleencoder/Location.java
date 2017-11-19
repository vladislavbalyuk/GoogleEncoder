package com.status.googleencoder;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable{
    private String name;
    private double lat, lng;

    public Location(String name, double lat, double lng){
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName(){
        return name;
    }

    public double getLat(){
        return lat;
    }

    public double getLng(){
        return lng;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }

    public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>(){
        public Location createFromParcel(Parcel in){
            return new Location(in);
        }

        public Location[] newArray(int size){
            return new Location[size];
        }
    };

    public Location(Parcel parcel){
        name = parcel.readString();
        lat = parcel.readDouble();
        lng = parcel.readDouble();
    }

    @Override
    public String toString(){
        return name + ", " + lng + ", " + lat;
    }
}
