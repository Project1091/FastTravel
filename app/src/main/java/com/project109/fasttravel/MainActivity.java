package com.project109.fasttravel;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener {

    final int LOCATION_PERMISSION_REQUEST_CODE = 1252;
    private float gpsLat;
    private float gpsLon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText ed = (EditText)findViewById(R.id.editText);

        Button b = (Button)findViewById(R.id.button);
        CheckBox chk1 = (CheckBox)findViewById(R.id.checkBox2);
        CheckBox chk2 = (CheckBox)findViewById(R.id.checkBox3);
        CheckBox chk3 = (CheckBox)findViewById(R.id.checkBox4);
        final ArrayList<Integer> tagArr = new ArrayList<Integer>();
        tagArr.add(chk1.isChecked() ? 0 : 1);
        tagArr.add(chk2.isChecked() ? 0 : 2);
        tagArr.add(chk3.isChecked() ? 0 : 3);
        b.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, TravelMapActivity.class);
                    intent.putExtra("gpsLat", gpsLat);
                    intent.putExtra("gpsLon", gpsLon);
                    intent.putExtra("time", Integer.parseInt(ed.getText().toString()));
                    intent.putExtra("tags", tagArr);
                startActivity(intent);
            }
        });
        StartGps();
    }

    private void StartGps()
    {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= 23) { // Marshmallow

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            }
            return;
        }

        locationManager.requestLocationUpdates(locationProvider, 0, 0, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        gpsLat = (float)location.getLatitude();
        gpsLon = (float)location.getLongitude();
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
