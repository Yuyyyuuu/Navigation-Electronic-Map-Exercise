package com.example.whua1;
import static org.osmdroid.views.util.constants.MathConstants.DEG2RAD;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.util.TileSystemWebMercator;
import org.osmdroid.util.constants.GeoConstants;
import org.osmdroid.views.util.constants.MathConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WhuRouteTask implements Runnable{
    protected String  m_strNAServer;
    protected String m_strLayerName;
    protected WhuRouteListner m_routelistner = null;
    protected GeoPoint startPoint;
    protected GeoPoint endPoint;
    protected String strUrl = "";
    protected ExecutorService executorService = Executors.newCachedThreadPool();
    public WhuRouteTask(String strNAServer,String layerName, WhuRouteListner routelistner)
    {
        m_strNAServer = strNAServer;
        m_strLayerName = layerName;
        m_routelistner = routelistner;
    }
    public void setStops(GeoPoint startGPT,GeoPoint endGPT)
    {
        startPoint = startGPT;
        endPoint = endGPT;
    }
    public void solveRouteAsync() {
        strUrl = m_strNAServer + "/ows?service=WFS";
        strUrl += "&version=1.0.0";
        strUrl += "&request=GetFeature";
        strUrl += "&maxFeatures=50";
        strUrl += "&typeName="+m_strLayerName;
        strUrl += "&outputFormat=application/json";

        double[] startxy = WebMecator.webMecatorbl2xy(startPoint);
        double[] endxy = WebMecator.webMecatorbl2xy(endPoint);
        String strStops = "&viewparams=x1:"+startxy[0]+";y1:"+startxy[1]+";";
        strStops += "x2:"+endxy[0]+";y2:"+endxy[1]+";";
        strUrl += strStops;
        executorService.execute(this);
    }
    @Override
    public void run() {
        StringBuffer resultBuffer = new StringBuffer();
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(strUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setConnectTimeout(8000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(8000);
            httpURLConnection.setRequestProperty("Accept-Charset", "utf-8");
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.connect();

            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader reader = null;

            String tempLine = null;
            //响应失败
            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }

            try {
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(inputStreamReader);
                while ((tempLine = reader.readLine()) != null) {
                    resultBuffer.append(tempLine);
                }

            } finally {

                if (reader != null) {
                    reader.close();
                }

                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            // e.printStackTrace();
            resultBuffer.append("other:" + e.toString());
        } finally {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        //调用路径响应接口
        if(m_routelistner!=null) {
            m_routelistner.onRoute(new WhuRoute(resultBuffer.toString()));
        }
    }

}
