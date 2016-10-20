package com.heitem.augmentedjourney;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.heitem.others.DividerItemDecoration;
import com.heitem.adapters.ResultAdapter;
import com.heitem.data_localization.GPSTracker;
import com.heitem.data_localization.GooglePlace;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Heitem on 27/05/2015.
 */
public class SearchActivity extends Activity {

    GPSTracker g;
    final static String GOOGLE_KEY = "AIzaSyCEg-2Tle5lJEiM7DNnH85I5nXfPQNU_s8";
    public List<GooglePlace> venuesList = Collections.EMPTY_LIST;
    protected ResultAdapter adapter;
    private RecyclerView rv;
    String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        g = new GPSTracker(getApplicationContext());
        rv = (RecyclerView)findViewById(R.id.rv);
        //rv.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        rv.setItemAnimator(new DefaultItemAnimator());

        venuesList = new ArrayList<>();
        venuesList.add(new GooglePlace("Chargement des données..."));
        adapter = new ResultAdapter(getApplicationContext(), venuesList);
        rv.setAdapter(adapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(layoutManager);

        handleIntent(getIntent());

        new googleplaces().execute();

        if(venuesList.size() == 0){
            venuesList.clear();
            venuesList.add(new GooglePlace("Aucune données trouvées !"));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.global, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
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
            //use the query to search your data somehow

        }
    }
    private class googleplaces extends AsyncTask<View, Void, String> {

        String temp;
        ProgressDialog p;

        @Override
        protected String doInBackground(View... urls) {

            // make Call to the url
            temp = makeCall("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + g.getLatitude() + "," + g.getLongitude() + "&language=fr&keyword=" + query + "&radius=50000&key=" + GOOGLE_KEY);

            //print the call in the console
            System.out.println("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" + g.getLatitude() + "," + g.getLongitude() + "&language=fr&keyword=" + query + "&radius=50000&key=" + GOOGLE_KEY);

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

                // parse Google places search result
                venuesList = parseGoogleParse(temp);

                System.out.println(venuesList);

                if(venuesList.size() != 0) {
                    //Collections.sort(venuesList);

                    Thread t = new Thread(new Runnable() {
                        public void run() {
                            for(int i = 0; i < venuesList.size(); i++){
                                //Log.d("LLLLLLLLLLLLLL", "https://maps.googleapis.com/maps/api/place/photo?photo_reference=" + venuesList.get(i).getPhoto_reference() + "&maxheight=300&maxwidth=1440&key=" + GOOGLE_KEY);
                                final Bitmap b;
                                String url = venuesList.get(i).getIcon();
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
                    t.setPriority(2);
                    t.start();

                    //Show the ArrayList content
                    System.out.println(venuesList);

                    adapter = new ResultAdapter(getApplicationContext(), venuesList);
                    rv.setAdapter(adapter);
                    final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                    layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    rv.setLayoutManager(layoutManager);
                }
                else {
                    //ll.setVisibility(View.VISIBLE);
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

        ArrayList<GooglePlace> temp = new ArrayList<GooglePlace>();
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
}