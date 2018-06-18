package com.project109.fasttravel;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by andrew on 6/17/18.
 */

public class FTEngine {

    public String MapsApiKey = "AIzaSyB_W5sRXy3osGzbtN21qXlWcweVLR5mMvY";
    private float distanceThr = 0.0f;
    private int durationThr = 0;
    private GoogleMap mapObj;
    private TextView textObj;
    private boolean cancelationToken = false;
    private int time;

    public FTEngine(GoogleMap map, TextView text)
    {
        mapObj = map;
        textObj = text;
    }

    public int GetTimeForPoints(List<LatLng> points, int durTime)
    {
        time = durTime;
        int num = -1;
        for(int i = 0; i < points.size() - 1; i++)
        {
            GetLenForRoute(points.get(i), points.get(i+1));
            //Log.d("[DEBUG]", "distanceThr = " + distanceThr);
            if(cancelationToken){
                num = i;
                break;
            }
        }
        return num;
    }

    public void GetLenForRoute(LatLng src, LatLng dst)
    {
        String url = GetRequestUrl(src, dst);
        DownloadManager downloadMan = new DownloadManager();
        downloadMan.execute(url);
    }



    private String GetRequestUrl(LatLng origin,LatLng dest)
    {
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=walking";
        String parameters = str_origin+"&"+str_dest+"&"+sensor+"&"+mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+"&key="+MapsApiKey;

        return url;
    }

    public static String DownloadJsonFromUrl(String url)
    {
        String jsonData = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try
        {
            URL urlObj = new URL(url);
            urlConnection = (HttpURLConnection) urlObj.openConnection();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            String line = "";
            StringBuffer sb = new StringBuffer();
            while((line = br.readLine()) != null)
            {
                sb.append(line);
            }
            jsonData = sb.toString();
            iStream.close();
        }
        catch(Exception ex)
        {
            Log.d("[DEBUG]", ex.getMessage());
        }
        finally
        {
            urlConnection.disconnect();
        }
        return jsonData;
    }

    private List<LatLng> DecodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len)
        {
            int b, shift = 0, result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do
            {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    private class DownloadManager extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... url)
        {
            String jsonData = "";

            try
            {
                jsonData = DownloadJsonFromUrl(url[0]);
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

            JsonDataParser parserTask = new JsonDataParser();
            parserTask.execute(s);
        }
    }

    private class JsonDataParser extends AsyncTask<String,  Integer, List<List<HashMap<String,String>>>>
    {
        @Override
        protected List<List<HashMap<String,String>>> doInBackground(String... jsonData)
        {
            List<List<HashMap<String, String>>> routes = null;

            try
            {
                JSONObject jObject = new JSONObject(jsonData[0]);
                routes = new ArrayList<List<HashMap<String,String>>>() ;
                JSONArray jRoutes = null;
                JSONArray jLegs = null;
                JSONArray jSteps = null;
                JSONObject jDistance = null;
                JSONObject jDuration = null;

                try {
                    jRoutes = jObject.getJSONArray("routes");
                    for(int i=0;i<jRoutes.length();i++){
                        jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");

                        List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

                        for(int j=0;j < jLegs.length();j++)
                        {

                            jDistance = ((JSONObject) jLegs.get(j)).getJSONObject("distance");
                            HashMap<String, String> hmDistance = new HashMap<String, String>();
                            hmDistance.put("distance", jDistance.getString("text"));

                            jDuration = ((JSONObject) jLegs.get(j)).getJSONObject("duration");
                            HashMap<String, String> hmDuration = new HashMap<String, String>();
                            hmDuration.put("duration", jDuration.getString("text"));

                            path.add(hmDistance);

                            path.add(hmDuration);

                            jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");

                            for(int k=0;k<jSteps.length();k++)
                            {
                                String polyline = "";
                                polyline = (String)((JSONObject)((JSONObject)jSteps.get(k)).get("polyline")).get("points");
                                List<LatLng> list = DecodePolyline(polyline);

                                for(int l=0;l<list.size();l++)
                                {
                                    HashMap<String, String> hm = new HashMap<String, String>();
                                    hm.put("lat", Double.toString(((LatLng)list.get(l)).latitude) );
                                    hm.put("lng", Double.toString(((LatLng)list.get(l)).longitude) );
                                    path.add(hm);
                                }
                            }
                        }
                        routes.add(path);
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
            catch(JSONException ex)
            {
                ex.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            String distance = "";
            String duration = "";

            // Traversing through all the routes
            for(int i=0;i<result.size();i++)
            {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for(int j=0;j<path.size();j++)
                {
                    HashMap<String,String> point = path.get(j);

                    if(j==0)
                    {
                        distance = (String)point.get("distance");
                        continue;
                    }
                    else if(j==1)
                    {
                        duration = (String)point.get("duration");
                        continue;
                    }
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(8);
                lineOptions.color(Color.RED);
            }
            int curTimeAdd = GetTimeFromString(duration);
            if(durationThr + curTimeAdd < time) {
                distanceThr += GetDistanceFromString(distance);
                durationThr += curTimeAdd;
                mapObj.addPolyline(lineOptions);
                textObj.setText("Distance: " + distanceThr + " m\nTime: " + durationThr + " min");
            }
            else cancelationToken = true;
        }
    }

    private float GetDistanceFromString(String distance)
    {
        float distinKm = Float.parseFloat(distance.substring(0, distance.indexOf(" ")));
        String dist = distance.substring(distance.indexOf(" "), distance.length());
        if(dist.equals(" km"))
            return distinKm * 1000;
        else return distinKm;
    }

    private int GetTimeFromString(String time)
    {
        int hours = 0, minutes = 0;
        int hindex = time.indexOf('h');
        if(hindex != -1)
        {
            hours = Integer.parseInt(time.substring(0, hindex-1));
        }
        int mindex = time.indexOf('m');
        if(mindex != -1 && mindex > 0)
        {
            String sanitize = time.substring(mindex - 2, mindex - 1);
            sanitize = sanitize.replace(" ", "");
            minutes = Integer.parseInt(sanitize);
        }
        if(mindex != -1 && mindex > 2) {
            String sanitize = time.substring(mindex - 3, mindex - 1);
            sanitize = sanitize.replace(" ", "");
            minutes = Integer.parseInt(sanitize);
        }
        return (hours*60)+minutes;
    }
}