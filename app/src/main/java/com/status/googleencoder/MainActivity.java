package com.status.googleencoder;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private DelayAutoCompleteTextView txtFrom, txtTo;
    private Location fromLoc, toLoc;
    private GoogleMap googleMapFrom, googleMapTo, googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        createTabs();

        txtFrom = (DelayAutoCompleteTextView) findViewById(R.id.txtFrom);
        txtFrom.setThreshold(4);
        txtFrom.setAdapter(new LocationAutoCompleteAdapter(this));
        txtFrom.setLoadingIndicator((ProgressBar) findViewById(R.id.progress_barFrom));
        txtFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Location location = (Location) adapterView.getItemAtPosition(position);
                txtFrom.setText(location.getName());
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                fromLoc = location;
                createMapView("from");
            }
        });

        txtTo = (DelayAutoCompleteTextView) findViewById(R.id.txtTo);
        txtTo.setThreshold(4);
        txtTo.setAdapter(new LocationAutoCompleteAdapter(this));
        txtTo.setLoadingIndicator((ProgressBar) findViewById(R.id.progress_barTo));
        txtTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Location location = (Location) adapterView.getItemAtPosition(position);
                txtTo.setText(location.getName());
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                toLoc = location;
                createMapView("to");
            }
        });

        Button btnResult = (Button) findViewById(R.id.btnResult);
        btnResult.setOnClickListener(this);

    }

    private void createTabs() {
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
        // инициализация
        tabHost.setup();

        TabHost.TabSpec tabSpec;

        // создаем вкладку и указываем тег
        tabSpec = tabHost.newTabSpec("tag1");
        // название вкладки
        tabSpec.setIndicator(getResources().getString(R.string.text_tab1));
        // указываем id компонента из FrameLayout, он и станет содержимым
        tabSpec.setContent(R.id.tabFrom);
        // добавляем в корневой элемент
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setIndicator(getResources().getString(R.string.text_tab2));
        tabSpec.setContent(R.id.tabTo);
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTabByTag("tag1");

        // обработчик переключения вкладок
        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
            public void onTabChanged(String tabId) {
//                Toast.makeText(getBaseContext(), "tabId = " + tabId, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void createMapView(final String typeLocation){

        googleMap = typeLocation.equals("from")?googleMapFrom:googleMapTo;
        int viewRes = typeLocation.equals("from")?R.id.mapViewFrom:R.id.mapViewTo;

        try {
                ((MapFragment) getFragmentManager().findFragmentById(
                        viewRes)).getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap map) {
                        googleMap = map;
                        googleMap.clear();
                        if(typeLocation.equals("from")){
                            googleMapFrom = googleMap;
                        }
                        else {
                            googleMapTo = googleMap;
                        }
                        addMarker(typeLocation);
                    }
                });

        } catch (NullPointerException exception){
        }

    }

    private void addMarker(String typeLocation){

        googleMap = typeLocation.equals("from")?googleMapFrom:googleMapTo;
        Location location = typeLocation.equals("from")?fromLoc:toLoc;
        //устанавливаем позицию и масштаб отображения карты
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLat(), location.getLng()))
                .zoom(9)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);

        if(googleMap != null){
            googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLat(), location.getLng()))
                    .title("Mark")
                    .draggable(false)
            );
        }
    }

    @Override
    public void onClick(View v) {
        Intent newActivity = new Intent(this,ResultActivity.class);
        newActivity.putExtra("from", fromLoc);
        newActivity.putExtra("to", toLoc);
        startActivity(newActivity);
    }

    public static StringBuilder getStringJSON(String urlReqest) {
        BufferedReader reader = null;
        StringBuilder buf=new StringBuilder();
        try {
            URL url=new URL(urlReqest);
            HttpsURLConnection c=(HttpsURLConnection)url.openConnection();
            c.setRequestMethod("GET");
            c.setReadTimeout(10000);
            c.connect();
            reader = new BufferedReader(new InputStreamReader(c.getInputStream()));

            String line=null;
            while ((line=reader.readLine()) != null) {
                buf.append(line + "\n");
            }
        }
        catch (Exception e){}
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            }
            catch (IOException e){}
        }

        return buf;
    }

}