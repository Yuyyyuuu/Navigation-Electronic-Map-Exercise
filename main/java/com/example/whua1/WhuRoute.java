package com.example.whua1;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class WhuRoute {
    public String m_strRoute;
    private Polyline routeGeometry;
    WhuRoute(String strRoute)
    {
        try {
            m_strRoute = strRoute;
            JSONArray featuresArray = new JSONObject(strRoute).getJSONArray("features");

            //create the polyline from the point collection
            routeGeometry = new Polyline();
            routeGeometry.getOutlinePaint().setColor(Color.GREEN);
            for(int i=0; i<featuresArray.length(); i++)
            {
                JSONObject jfeature = featuresArray.getJSONObject(i);
                if(jfeature.has("geometry"))
                {
                    JSONObject geometryObject = jfeature.getJSONObject("geometry");
                    JSONArray pathsArray = geometryObject.getJSONArray("coordinates");
                    for(int j=0; j<pathsArray.length(); j++)
                    {
                        JSONArray pointArray = pathsArray.getJSONArray(j);
                        //投影转换
                        GeoPoint gpt = WebMecator.webMecatorxy2bl(pointArray.getDouble(0),pointArray.getDouble(1));
                        routeGeometry.addPoint(gpt);
                    }


                }

            }

            // create and add points to the point collection

        }
        catch (JSONException e) {
            e.printStackTrace();
            m_strRoute = e.getMessage();
        }

    }
    Polyline getRouteGeometry()
    {
        return routeGeometry;
    }

}
