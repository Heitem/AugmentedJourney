package com.heitem.augmentedjourney;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.heitem.others.DividerItemDecoration;
import com.heitem.ui.CameraActivity;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /*private String[] navMenuTitles;
    private TypedArray navMenuIcons;*/
    private LinearLayout ll;
    private Button refresh;
    protected HomeAdapter adapter;
    private RecyclerView rc;
    private GoogleApiClient googleApiClient;
    public List<GooglePlace> venuesList = Collections.EMPTY_LIST;
    final static String GOOGLE_KEY = "AIzaSyAg4vmpzmWqxkKGnecJYAxOIUZlvN9esV4";
    GPSTracker g;
    private static final int REQUEST_PLACE_PICKER = 1;
    private RelativeLayout desc;
    public com.heitem.data_localization.Place p;
    private TextView ranking;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //For navigation Drawer
        /*navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items); // load titles from strings.xml
        navMenuIcons = getResources().obtainTypedArray(R.array.nav_drawer_icons);// load icons from strings.xml
        set(navMenuTitles, navMenuIcons);*/

        desc = (RelativeLayout)findViewById(R.id.desc);

        ll = (LinearLayout)findViewById(R.id.cn);
        ll.setVisibility(View.GONE);
        rc = (RecyclerView)findViewById(R.id.rc);
        rc.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        rc.setItemAnimator(new DefaultItemAnimator());
        venuesList = new ArrayList<>();
        venuesList.add(new GooglePlace("Chargement des données..."));
        venuesList.add(new GooglePlace("Chargement des données..."));
        venuesList.add(new GooglePlace("Chargement des données..."));
        venuesList.add(new GooglePlace("Chargement des données..."));
        venuesList.add(new GooglePlace("Chargement des données..."));
        adapter = new HomeAdapter(getApplicationContext(), venuesList);
        rc.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rc.setLayoutManager(layoutManager);
        ranking = (TextView)findViewById(R.id.ranking);


        refresh = (Button)findViewById(R.id.refresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                g = new GPSTracker(getApplicationContext());
                new googleplaces().execute();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                Context context = getApplicationContext();
                try {
                    startActivityForResult(builder.build(MainActivity.this), REQUEST_PLACE_PICKER);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
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

            LocationRequest  locationRequest2 = LocationRequest.create();
            locationRequest2.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            locationRequest2.setInterval(30 * 1000);
            locationRequest2.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest1).addLocationRequest(locationRequest2);

            //**************************
            builder.setAlwaysShow(true); //this is the key ingredient
            //**************************

            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    final LocationSettingsStates state = result.getLocationSettingsStates();
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
                }
            });
        }
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        g = new GPSTracker(getApplicationContext());
        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.NONE)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024)
                .writeDebugLogs().build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP

        new googleplaces().execute();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about) {
            Intent i = new Intent(MainActivity.this, AboutActivity.class);
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
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    private class googleplaces extends AsyncTask<View, Void, String> {

        String temp;
        //ProgressDialog p;

        @Override
        protected String doInBackground(View... urls) {

            // make Call to the url
            temp = makeCall("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + g.getLatitude() + "," + g.getLongitude() + "&language=fr&radius=10000&key=" + GOOGLE_KEY);

            //print the call in the console
            System.out.println("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + g.getLatitude() + "," + g.getLongitude() + "&language=fr&radius=10000&key=" + GOOGLE_KEY);

            return "";
        }

        @Override
        protected void onPreExecute() {
            // we can start a progress bar here
            // Showing progress dialog
            /*p = new ProgressDialog(MainActivity.this);
            p.setIcon(R.mipmap.ic_launcher);
            p.setTitle("Veuillez Patienter...");
            p.setMessage("Chargement des données en cours...");
            p.setCancelable(false);
            p.show();*/
        }

        @Override
        protected void onPostExecute(String result) {
            if (temp == null) {
                // we have an error to the call
                // we can also stop the progress bar
            } else {
                // all things went right

                ll.setVisibility(View.GONE);

                // parse Google places search result
                venuesList = parseGoogleParse(temp);

                if(venuesList.size() != 0) {
                    Collections.sort(venuesList);

                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            for(int i = 0; i < 5; i++){
                                Log.d("LLLLLLLLLLLLLL", "https://maps.googleapis.com/maps/api/place/photo?photo_reference=" + venuesList.get(i).getPhoto_reference() + "&maxheight=300&maxwidth=1440&key=" + GOOGLE_KEY);
                                final Bitmap b;
                                String url = "https://maps.googleapis.com/maps/api/place/photo?photo_reference=" + venuesList.get(i).getPhoto_reference() + "&maxheight=300&maxwidth=1440&key=" + GOOGLE_KEY;
                                ImageLoader imageLoader = ImageLoader.getInstance();
                                DisplayImageOptions options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).build();
                                b = imageLoader.loadImageSync(url, options);
                                venuesList.set(i, venuesList.get(i)).setImage(b);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });

                            }
                        }
                    });
                    t.setPriority(1);
                    t.start();

                    //Show the ArrayList content
                    System.out.println(venuesList);

                    //OnClickListener sur RecyclerView de la page d'acceuil
                    adapter = new HomeAdapter(getApplicationContext(), venuesList);
                    if(venuesList.size() > 0) {
                        adapter.SetOnItemClickListener(new HomeAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Intent i = new Intent(getApplicationContext(), CameraActivity.class);
                                i.putExtra("O", venuesList.get(position));
                                Toast.makeText(getApplicationContext(), "Vous avez basculer vers le mode Réalité Augmentée.\nCible : " + venuesList.get(position).getName(), Toast.LENGTH_LONG).show();
                                startActivity(i);
                            }
                        });
                    }
                    rc.setAdapter(adapter);
                    final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    rc.setLayoutManager(layoutManager);
                }
                else {
                    ll.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public static String makeCall(String url) {

        // string buffers the url
        StringBuffer buffer_string = new StringBuffer(url);
        String replyString = "";

        // instanciate an HttpClient
        HttpClient httpclient = new DefaultHttpClient();
        // instanciate an HttpGet
        HttpGet httpget = new HttpGet(buffer_string.toString());

        try {
            // get the responce of the httpclient execution of the url
            HttpResponse response = httpclient.execute(httpget);
            InputStream is = response.getEntity().getContent();

            // buffer input stream the result
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(20);
            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            // the result as a string is ready for parsing
            replyString = new String(baf.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(replyString);

        // trim the whitespaces
        return replyString.trim();
    }

    private static ArrayList<GooglePlace> parseGoogleParse(final String response) {

        ArrayList<GooglePlace> temp = new ArrayList<>();
        try {

            // make an jsonObject in order to parse the response
            JSONObject jsonObject = new JSONObject(response);

            // make an jsonObject in order to parse the response
            if (jsonObject.has("results")) {

                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for(int i = 0; i < jsonArray.length(); i++){
                    GooglePlace poi = new GooglePlace();
                    poi.setName(jsonArray.getJSONObject(i).getString("name"));
                    if (jsonArray.getJSONObject(i).has("vicinity")) {
                        poi.setAdress(jsonArray.getJSONObject(i).getString("vicinity"));
                    }
                    if (jsonArray.getJSONObject(i).has("rating")) {
                        poi.setRating((float)jsonArray.getJSONObject(i).getDouble("rating"));
                    }
                    poi.setLatitude(jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                    poi.setLongitude(jsonArray.getJSONObject(i).getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                    if (jsonArray.getJSONObject(i).has("photos")) {
                        JSONArray photoArray = jsonArray.getJSONObject(i).getJSONArray("photos");
                        for (int j = 0; j < photoArray.length(); j++) {
                            if(photoArray.getJSONObject(j).has("photo_reference")) {
                                poi.setPhoto_reference(photoArray.getJSONObject(j).getString("photo_reference"));
                            }
                        }
                    }
                    if (jsonArray.getJSONObject(i).has("icon")) {
                        poi.setIcon(jsonArray.getJSONObject(i).getString("icon"));
                    }
                    temp.add(poi);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            //temp.add(new GooglePlace());
        }
        return temp;

    }

    //Resultat du Place Picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_PLACE_PICKER && resultCode == Activity.RESULT_OK) {

            // The user has selected a place. Extract the name and address.
            final Place place = PlacePicker.getPlace(data, this);
            p = new com.heitem.data_localization.Place(place.getId(), place.getName(), place.getAddress(), place.getLatLng().latitude, place.getLatLng().longitude);
			/*final CharSequence name = place.getName();
			final CharSequence address = place.getAddress();*/
            String attributions = PlacePicker.getAttributions(data);
            if (attributions == null) {
                attributions = "";
            }
            Toast.makeText(this, "Vous avez basculer vers le mode Réalité Augmentée.\nCible : " + place.getName(), Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), ArActivity.class);
            i.putExtra("Place", p);
            startActivity(i);
			/*mViewName.setText(name);
			mViewAddress.setText(address);
			mViewAttributions.setText(Html.fromHtml(attributions));*/

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private Boolean getBooleanPreferences(String sKey, Boolean sDefault){
        preferences = getSharedPreferences("Pref", MODE_PRIVATE);
        return preferences.getBoolean(sKey, sDefault);
    }

    private void setBooleanPreferences(String sKey, Boolean sValue){
        preferences = getSharedPreferences("Pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(sKey, sValue);
        editor.apply();
    }

    @Override
    public void onStop(){
        super.onStop();
        //setBooleanPreferences("Pref", false);
        writeToFile("false");
    }

    private void writeToFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput("config.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }


    private String readFromFile() {

        String ret = "";

        try {
            InputStream inputStream = openFileInput("config.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
            return ret;
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
            return "not";
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
            return "not";
        }
    }
    public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h= (int) (newHeight*densityMultiplier);
        int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));

        photo=Bitmap.createScaledBitmap(photo, w, h, true);

        return photo;
    }
}