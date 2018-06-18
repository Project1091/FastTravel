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
            for(int j = 0; j < jObject.length(); j++)
            {
                LatLng src = new LatLng(Float.parseFloat(((JSONObject) jObject.get(j)).getString("Latitude")), Float.parseFloat(((JSONObject) jObject.get(j)).getString("Longitude")));
                routeList.add(FindNextClosestPoint(src, jObject));
                j--;
            }

            for(int i = 0; i < routeList.size(); i++)
            {
                LatLng point = new LatLng(Float.parseFloat(routeList.get(i).getString("Latitude")), Float.parseFloat(routeList.get(i).getString("Longitude")));
                pointList.add(point);
                mMap.addMarker(new MarkerOptions().position(point).title(routeList.get(i).getString("Name")));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 13.0f));
            }

            eng.GetTimeForPoints(pointList, time);

        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }

    }

    public JSONObject FindNextClosestPoint(LatLng src, JSONArray points)
    {
        ArrayList<Float> distances = new ArrayList<Float>();

        try
        {
            for(int i = 0; i < points.length(); i++)
            {
                float dist = (float)Math.sqrt(Math.pow((((JSONObject)points.get(i)).getDouble("Latitude") - src.latitude),2) + Math.pow((((JSONObject)points.get(i)).getDouble("Longitude") - src.longitude),2));
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
        String parameters = "&la=" + curGpsLat + "&lo=" + curGpsLon + "&ti=" + time;
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
