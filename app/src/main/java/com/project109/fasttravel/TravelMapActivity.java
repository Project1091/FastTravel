package com.project109.fasttravel;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TravelMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private float curGpsLat;
    private float curGpsLon;
    private ArrayList<Integer> tags;
    private int time;
    private TextView textBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_map);
        textBox = (TextView) findViewById(R.id.durText);
        Intent intent = getIntent();
        curGpsLat = intent.getFloatExtra("gpsLat", 0.0f);
        curGpsLon = intent.getFloatExtra("gpsLon", 0.0f);
        time = intent.getIntExtra("time", 0);
        tags = intent.getIntegerArrayListExtra("tags");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng point = new LatLng(curGpsLat, curGpsLon);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.star);
        mMap.addMarker(new MarkerOptions().position(point).title("Стартовая точка").icon(icon));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 13.0f));

        GetLocationData();
    }

    public void GetLocationData()
    {
        String url = GetRequestUrl();
        DownloadManager downloadMan = new DownloadManager();
        downloadMan.execute(url);
    }

    public void BuildRouteFromJson(String jsonData)
    {
        ArrayList<JSONObject> routeList = new ArrayList<JSONObject>();
        FTEngine eng = new FTEngine(mMap, textBox);
        List<LatLng> pointList = new ArrayList<LatLng>();

        try
        {
            jsonData = '[' + jsonData + ']';
            jsonData = jsonData.replace("}{", "},{");
            JSONArray jObject = new JSONArray(jsonData);
            routeList.add(FindNextClosestPoint(new LatLng(curGpsLat, curGpsLon), jObject));
            for(int j = 0; j < jObject.length(); j++)
            {
                LatLng src = new LatLng(Float.parseFloat(routeList.get(routeList.size()-1).getString("Latitude")), Float.parseFloat(routeList.get(routeList.size()-1).getString("Longitude")));
                routeList.add(FindNextClosestPoint(src, jObject));
                j--;
            }

            pointList.add(new LatLng(curGpsLat, curGpsLon));

            for(int i = 0; i < routeList.size(); i++)
            {
                LatLng point = new LatLng(Float.parseFloat(routeList.get(i).getString("Latitude")), Float.parseFloat(routeList.get(i).getString("Longitude")));
                pointList.add(point);
            }

            int cutoff = eng.GetTimeForPoints(pointList, time);
            //wait(5000);

            if(cutoff == -1)
                cutoff = pointList.size();

            for(int i = 1; i < cutoff; i++)
            {
                mMap.addMarker(new MarkerOptions().position(pointList.get(i)).title("#" + i + " " + routeList.get(i-1).getString("Name")));
            }


        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }

    public JSONObject FindNextClosestPoint(LatLng src, JSONArray points)
    {
        ArrayList<Double> distances = new ArrayList<Double>();

        try
        {
            for(int i = 0; i < points.length(); i++)
            {
                LatLng dst = new LatLng(((JSONObject)points.get(i)).getDouble("Latitude"), ((JSONObject)points.get(i)).getDouble("Longitude"));
                double dist = getDistanceFromLatLonInKm(src, dst);

                distances.add(dist);
            }

            int minIndex = distances.indexOf(Collections.min(distances));

            JSONObject obj = (JSONObject)points.get(minIndex);
            points.remove(minIndex);

            return obj;
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return null;
    }

    private double getDistanceFromLatLonInKm(LatLng src, LatLng dst)
    {
        int R = 6371; // Radius of the earth in km
        double dLat = deg2rad(dst.latitude-src.latitude);  // deg2rad below
        double dLon = deg2rad(dst.longitude-src.longitude);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(deg2rad(src.latitude)) * Math.cos(deg2rad(dst.latitude)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    private double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

    private String GetRequestUrl()
    {
        StringBuffer tagParam = new StringBuffer();
        tagParam.append("t=");
        for(int i = 0; i < tags.size(); i++)
        {
            tagParam.append(tags.get(i));
            if(i+1 < tags.size())
                tagParam.append(',');
        }
        String parameters = "&la=" + curGpsLat + "&lo=" + curGpsLon + "&ti=" + 2000;
        String url = "http://89.185.3.253:18080/poi.php?" + tagParam.toString() + parameters;

        return url;
    }

    private class DownloadManager extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... url)
        {
            String jsonData = "";

            try
            {
                jsonData = FTEngine.DownloadJsonFromUrl(url[0]);
            }
            catch (Exception ex)
            {
                Log.d("[DEBUG]", ex.getMessage());
            }

            return jsonData;
        }

        @Override
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);

            BuildRouteFromJson(s);
        }
    }
}
