package com.heitem.data_localization;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Heitem on 31/05/2015.
 */
public class GooglePlace implements Comparable, Parcelable {

    private String name;
    private String address;
    private Float rating;
    private double latitude;
    private double longitude;
    private String photo_reference;
    private String icon;
    private Bitmap image;

    public GooglePlace(String name) {
        this.name = name;
    }


    @Override
    public int compareTo(Object p1) {

        GooglePlace p = (GooglePlace) p1;

        return -1 * this.getRating().compareTo(p.getRating());
    }

    public GooglePlace() {
        this.setName("");
        this.setAddress("");
        this.setRating(0f);
        this.setLatitude(0);
        this.setLongitude(0);
        this.setPhoto_reference("");
        this.setIcon("");
        this.setImage(null);
    }

    public GooglePlace(String name, String address, Float rating, double latitude, double longitude, String pr, String icon, Bitmap image) {
        this.setName(name);
        this.setAddress(address);
        this.setRating(rating);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        this.setPhoto_reference(pr);
        this.setIcon(icon);
        this.setImage(image);
    }
    public GooglePlace(Bitmap image){
        this.setImage(image);
    }
    public GooglePlace(Parcel in){
        this.setName(in.readString());
        this.setAddress(in.readString());
        this.setRating(in.readFloat());
        this.setLatitude(in.readDouble());
        this.setLongitude(in.readDouble());
        this.setPhoto_reference(in.readString());
        this.setIcon(in.readString());
        //this.setImage(in.readParcelable(null));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getName());
        dest.writeString(getAddress());
        dest.writeFloat(getRating());
        dest.writeDouble(getLatitude());
        dest.writeDouble(getLongitude());
        dest.writeString(getPhoto_reference());
        dest.writeString(getIcon());
        dest.writeParcelable(getImage(), 1);
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public GooglePlace createFromParcel(Parcel in) {
            return new GooglePlace(in);
        }

        public GooglePlace[] newArray(int size) {
            return new GooglePlace[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
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

    public String getPhoto_reference() {
        return photo_reference;
    }

    public void setPhoto_reference(String photo_reference) {
        this.photo_reference = photo_reference;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }
}
