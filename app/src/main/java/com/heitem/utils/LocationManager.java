package com.heitem.utils;

import android.Manifest;
import android.app.Activity;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class LocationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient googleApiClient;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location currentLocation;
    private int locationRequestCode = 1000;
    private CurrentLocationListener locationListener;

    private LocationManager() {
    }

    private static LocationManager instance;

    public static LocationManager getInstance() {
        if (instance == null) {
            instance = new LocationManager();
        }
        return instance;
    }

    public void setLocationListener(CurrentLocationListener locationListener) {
        this.locationListener = locationListener;
    }

    public void showSettingsClientDialog(Activity activity) {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(activity).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
            googleApiClient.connect();

            LocationRequest locationRequest1 = LocationRequest.create();
            locationRequest1.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest1.setInterval(30 * 1000);
            locationRequest1.setFastestInterval(5 * 1000);

            LocationRequest locationRequest2 = LocationRequest.create();
            locationRequest2.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationRequest2.setInterval(30 * 1000);
            locationRequest2.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest1).addLocationRequest(locationRequest2);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(result1 -> {
                final Status status = result1.getStatus();
                final LocationSettingsStates state = result1.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(activity, 1000);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            });
        }
    }

    public void configureLocation(Activity activity) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(10 * 1000); // 10 seconds
        locationRequest.setFastestInterval(5 * 1000); // 5 seconds

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        currentLocation = location;
                        if (fusedLocationClient != null) {
                            fusedLocationClient.removeLocationUpdates(locationCallback);
                        }
                    }
                }
            }
        };

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, permissions, locationRequestCode);

        } else {
            requestLocation(activity);
        }
    }

    public void requestLocation(Activity activity) {
        fusedLocationClient.getLastLocation().addOnSuccessListener(activity, location -> {
            if (location != null) {
                if (locationListener != null) locationListener.onCurrentLocationReceived(location);
            } else {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public interface CurrentLocationListener {
        void onCurrentLocationReceived(Location currentLocation);
    }
}
