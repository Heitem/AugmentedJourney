package com.heitem.data_localization;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Heitem on 29/05/2015.
 */
public class Place implements Parcelable{
    private String id;
    private CharSequence nom;
    private CharSequence adress;
    private double latitude;
    private double longitude;

    public Place(Parcel source) {
        source.writeString((String) getNom());
        source.writeString((String) getAdress());
        source.writeDouble(getLatitude());
        source.writeDouble(getLongitude());
    }

    //Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CharSequence getNom() {
        return nom;
    }

    public void setNom(CharSequence nom) {
        this.nom = nom;
    }

    public CharSequence getAdress() {
        return adress;
    }

    public void setAdress(CharSequence adress) {
        this.adress = adress;
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

    //Constructeurs
    public Place(String id, CharSequence nom, CharSequence adress, double latitude, double longitude){
        this.id = id;
        this.nom = nom;
        this.adress = adress;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString((String) getNom());
        dest.writeString((String) getAdress());
        dest.writeDouble(getLatitude());
        dest.writeDouble(getLongitude());
    }
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public Place createFromParcel(Parcel source) {
            return new Place(source);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
}
