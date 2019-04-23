package com.heitem.augmentedjourney;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.heitem.adapters.HomeAdapter;
import com.heitem.ar.ArActivity;
import com.heitem.data_localization.GooglePlace;
import com.heitem.networking.NetworkManager;
import com.heitem.networking.RequestGetJson;
import com.heitem.others.DividerItemDecoration;
import com.heitem.utils.Helpers;
import com.heitem.utils.LocationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.heitem.utils.Constants.GOOGLE_KEY;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private LinearLayout cFL;
    private Button refresh;
    private HomeAdapter adapter;
    private RecyclerView recyclerView;
    private GoogleApiClient googleApiClient;
    private List<GooglePlace> venuesList = new ArrayList<>();
    private static final int REQUEST_PLACE_PICKER = 1;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.app_name);

        cFL = findViewById(R.id.connectionFailedLayout);
        cFL.setVisibility(View.GONE);
        swipeRefreshLayout = findViewById(R.id.srl);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter = new HomeAdapter(this);
        recyclerView.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        refresh = findViewById(R.id.refresh);
        refresh.setOnClickListener(v -> {
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

        swipeRefreshLayout.setOnRefreshListener(this::loadData);

        LocationManager.getInstance().showSettingsClientDialog(this);

        LocationManager.getInstance().setLocationListener(currentLocation -> {
            MainActivity.this.currentLocation = currentLocation;
            loadData();
        });

        LocationManager.getInstance().configureLocation(this);
    }

    private void loadData() {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&language=fr&radius=10000&key=" + GOOGLE_KEY;

        NetworkManager.getInstance().get(this, url, new RequestGetJson.OnGetRequestListener() {
            @Override
            public void onGetRequestSuccess(String response) {
                cFL.setVisibility(View.GONE);
                venuesList = Helpers.INSTANCE.parseGooglePlace(response);

                if (venuesList.size() != 0) {
                    Collections.sort(venuesList);
                    if (venuesList.size() > 5) adapter.setData(venuesList.subList(0, 5));
                    else adapter.setData(venuesList);
                    swipeRefreshLayout.setRefreshing(false);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationManager.getInstance().requestLocation(this);
            } else Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
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

    //Place Picker activity result
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