package com.status.googleencoder;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class ResultActivity extends AppCompatActivity {

    private GoogleMap googleMap;
    private Location fromLoc, toLoc;
    PolylineOptions line;
    LatLngBounds.Builder latLngBuilder;
    LatLng start, end;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        Intent intent = getIntent();
        fromLoc = intent.getParcelableExtra("from");
        toLoc = intent.getParcelableExtra("to");
        Log.d("MYLOG", fromLoc.toString());
        Log.d("MYLOG", toLoc.toString());

        createMapView();

        (new GetDirectionTask()).execute();

    }

    private void createMapView(){

        try {
            ((MapFragment) getFragmentManager().findFragmentById(
                    R.id.mapView)).getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    googleMap = map;
                }
            });

        } catch (NullPointerException exception){
        }

    }

    class GetDirectionTask extends AsyncTask<Void, Void, Void> {

        public GetDirectionTask() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            JSONArray jsonArray;
            JSONObject jsonObject, jObject, jsonObjectLoc;
            Double lat, lng;

            line = new PolylineOptions();
            line.width(6f).color(Color.RED);
            latLngBuilder = new LatLngBounds.Builder();


            Log.d("MYLOG","https://maps.googleapis.com/maps/api/directions/json?origin=" + fromLoc.getLat() + "," + fromLoc.getLng()
                    + "&destination=" + toLoc.getLat() + "," + toLoc.getLng() + "&key=" + getResources().getString(R.string.API_KEY));
            StringBuilder buf = MainActivity.getStringJSON("\n" +
                    "https://maps.googleapis.com/maps/api/directions/json?origin=" + fromLoc.getLat() + "," + fromLoc.getLng()
                    + "&destination=" + toLoc.getLat() + "," + toLoc.getLng() + "&key=" + getResources().getString(R.string.API_KEY));
            try{
                jsonObject = new JSONObject(buf.toString());
                String status = jsonObject.getString("status");
                if(status.equals("OK")){

                    jObject = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline");
                    String points = jObject.getString("points");
                    List<LatLng> mPoints = PolyUtil.decode(points);
                    for (int i = 0; i < mPoints.size(); i++) {
                        if (i == 0) {
                            start = mPoints.get(i);
                        } else if (i == mPoints.size() - 1) {
                            end = mPoints.get(i);
                        }
                        line.add(mPoints.get(i));
                        latLngBuilder.include(mPoints.get(i));
                    }                }
                else{
                    Log.d("MYLOG","Invalid result from Google geoencoder");
                }
            }
            catch (JSONException e){};

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            googleMap.addPolyline(line);
            int size = getResources().getDisplayMetrics().widthPixels;
            LatLngBounds latLngBounds = latLngBuilder.build();
            CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
            googleMap.moveCamera(track);

            MarkerOptions startMarkerOptions = new MarkerOptions().position(start);
            googleMap.addMarker(startMarkerOptions);
            MarkerOptions endMarkerOptions = new MarkerOptions().position(end);
            googleMap.addMarker(endMarkerOptions);

        }
    }


}
