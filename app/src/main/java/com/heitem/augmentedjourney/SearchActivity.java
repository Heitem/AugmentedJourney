package com.heitem.augmentedjourney;

import android.app.SearchManager;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.VolleyError;
import com.heitem.adapters.ResultAdapter;
import com.heitem.data_localization.GooglePlace;
import com.heitem.networking.NetworkManager;
import com.heitem.networking.RequestGetJson;
import com.heitem.utils.Helpers;
import com.heitem.utils.LocationManager;
import com.heitem.utils.Log;

import java.util.ArrayList;
import java.util.List;

import static com.heitem.utils.Constants.GOOGLE_KEY;

/**
 * Created by Heitem on 27/05/2015.
 */
public class SearchActivity extends AppCompatActivity {

    private List<GooglePlace> venuesList = new ArrayList<>();
    private ResultAdapter adapter;
    private RecyclerView recyclerView;
    private String query;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("Search");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.recyclerView);
        adapter = new ResultAdapter(this);
        recyclerView.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        handleIntent(getIntent());

        LocationManager.getInstance().setLocationListener(currentLocation -> {
            SearchActivity.this.currentLocation = currentLocation;
            loadData();
        });

        LocationManager.getInstance().configureLocation(this);
    }

    private void loadData() {
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&language=fr&keyword=" + query
                + "&radius=50000&key=" + GOOGLE_KEY;

        NetworkManager.getInstance().get(this, url, new RequestGetJson.OnGetRequestListener() {

            @Override
            public void onGetRequestSuccess(String response) {
                venuesList = Helpers.INSTANCE.parseGooglePlace(response);

                if (venuesList.size() != 0) {
                    adapter.setData(venuesList);
                    progressBar.setVisibility(View.GONE);
                } else {
                    //ll.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onGetRequestError(VolleyError volleyError) {
                Log.e(volleyError.getMessage(), volleyError);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.about) {
            Intent i = new Intent(SearchActivity.this, AboutActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);
        }
    }
}