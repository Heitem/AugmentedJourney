package com.heitem.augmentedjourney;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.heitem.adapters.HomeAdapter;
import com.heitem.ar.ArActivity;
import com.heitem.data_localization.GPSTracker;
import com.heitem.data_localization.GooglePlace;
import com.heitem.networking.NetworkManager;
import com.heitem.networking.RequestGetJson;
import com.heitem.others.DividerItemDecoration;
import com.heitem.utils.Helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.heitem.utils.Constants.GOOGLE_KEY;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private LinearLayout cFL;
    private Button refresh;
    private HomeAdapter adapter;
    private RecyclerView recyclerView;
    private GoogleApiClient googleApiClient;
    private List<GooglePlace> venuesList = new ArrayList<>();
    private GPSTracker gpsTracker;
    private static final int REQUEST_PLACE_PICKER = 1;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);

        cFL = findViewById(R.id.connectionFailedLayout);
        cFL.setVisibility(View.GONE);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter = new HomeAdapter(this);
        recyclerView.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(v -> {
            gpsTracker = new GPSTracker(MainActivity.this);
            loadData();
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            try {
                startActivityForResult(builder.build(MainActivity.this), REQUEST_PLACE_PICKER);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        });

        //Code pour Setting API Dialog
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
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
                            status.startResolutionForResult(MainActivity.this, 1000);
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

        gpsTracker = new GPSTracker(this);

        loadData();
    }

    private void loadData() {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + gpsTracker.getLatitude() + "," + gpsTracker.getLongitude() + "&language=fr&radius=10000&key=" + GOOGLE_KEY;

        NetworkManager.getInstance().get(MainActivity.this, url, new RequestGetJson.OnGetRequestListener() {
            @Override
            public void onGetRequestSuccess(String response) {
                cFL.setVisibility(View.GONE);
                venuesList = Helpers.INSTANCE.parseGooglePlace(response);

                if (venuesList.size() != 0) {
                    Collections.sort(venuesList);
                    adapter.setData(venuesList.subList(0, 5));
                } else {
                    cFL.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onGetRequestError(VolleyError volleyError) {
                Log.e(TAG, volleyError.networkResponse.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    //Resultat du Place Picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_PLACE_PICKER && resultCode == Activity.RESULT_OK) {
			/*final CharSequence name = place.getName();
			final CharSequence address = place.getAddress();*/
            String attributions = PlacePicker.getAttributions(data);
            if (attributions == null) {
                attributions = "";
            }
            Intent i = new Intent(getApplicationContext(), ArActivity.class);
            Place place = PlacePicker.getPlace(this, data);
            com.heitem.data_localization.Place p = new com.heitem.data_localization.Place(place.getId(), place.getName(), place.getAddress(), place.getLatLng().latitude, place.getLatLng().longitude);
            i.putExtra("Place", p);
            Toast.makeText(this, "Vous avez basculer vers le mode Réalité Augmentée.\nCible : " + place.getName(), Toast.LENGTH_LONG).show();
            startActivity(i);
			/*mViewName.setText(name);
			mViewAddress.setText(address);
			mViewAttributions.setText(Html.fromHtml(attributions));*/

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}