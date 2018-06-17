package com.project109.fasttravel;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class TravelMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        TextView textBox = (TextView) findViewById(R.id.durText);

        LatLng sydney = new LatLng(50.41671, 30.56902);
        LatLng sydney1 = new LatLng(50.44670, 30.52750);
        LatLng sydney2 = new LatLng(50.41670, 30.56752);
        LatLng sydney3 = new LatLng(50.42665, 30.53387);
        LatLng sydney4 = new LatLng(50.44112, 30.51448);
        LatLng sydney5 = new LatLng(50.44850, 30.58196);
        FTEngine eng = new FTEngine(mMap, textBox);
        List<LatLng> pointList = new ArrayList<LatLng>();
        pointList.add(sydney);
        pointList.add(sydney1);
        pointList.add(sydney2);
        pointList.add(sydney3);
        pointList.add(sydney4);
        pointList.add(sydney5);
        eng.GetTimeForPoints(pointList, 4.0f);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Test"));
        mMap.addMarker(new MarkerOptions().position(sydney1).title("Test1"));
        mMap.addMarker(new MarkerOptions().position(sydney2).title("Test2"));
        mMap.addMarker(new MarkerOptions().position(sydney3).title("Test3"));
        mMap.addMarker(new MarkerOptions().position(sydney4).title("Test4"));
        mMap.addMarker(new MarkerOptions().position(sydney5).title("Test5"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13.0f));
    }
}
