package com.project109.fasttravel;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andrew on 6/17/18.
 */

public class FTEngine {

    public String MapsApiKey = "AIzaSyDoZCP6k279KJ-BY30zvZG3rIKJtfvf0bQ";

    public float GetTimeForPoints(List<LatLng> points, float speed)
    {
        float totalLen = 0.0f;
        for(int i = 0; i < points.size() - 1; i++)
        {
            totalLen += GetLenForRoute(points.get(i), points.get(i+1));
        }
        return totalLen/speed;
    }

    public float GetLenForRoute(LatLng src, LatLng dst)
    {
        return 0.0f;
    }



    private String GetRequestUrl(LatLng origin,LatLng dest)
    {
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters+"&key="+MapsApiKey;

        return url;
    }

    private List<LatLng> DecodePolyline(String encoded)
    {
        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}
