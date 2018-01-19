package com.example.easonchang.findnattype;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import java.util.PriorityQueue;

/**
 * Created by EasonChang on 2018/1/19.
 */

public class NetworkUtils {
    private String TAG = "NetworkUtils";
    private Context context;
    public NetworkUtils(Context c){
        this.context = c;
    }
    public boolean isNetworkOnline() {
        boolean status=false;
        try{
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(0);
            if (netInfo != null && netInfo.getState()==NetworkInfo.State.CONNECTED) {
                status= true;
            }else {
                netInfo = cm.getNetworkInfo(1);
                if(netInfo!=null && netInfo.getState()==NetworkInfo.State.CONNECTED)
                    status= true;
            }
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
        return status;

    }

    /** Check if there is any connectivity to a Wifi network */
    public String ConnectionType() {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        switch (info.getType()) {
            case ConnectivityManager.TYPE_WIFI:
                return "WIFI \nSSID: "+getWiFiSSID();
            case ConnectivityManager.TYPE_MOBILE:
                return "MOBILE";
            default:
                return "UNKNOW";
        }
    }

    private String getWiFiSSID(){
        WifiManager wifiManager = (WifiManager) context.getSystemService (Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo ();
        String ssid  = info.getSSID();

        return ssid;
    }
}
