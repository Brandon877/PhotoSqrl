package com.gmoney.photosqrl;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class TransferConditionsRunnable implements Runnable {
    String targetSSID = "ATT957x715";

    Context mContext;
    Nutz sqrlNutz;
    SqrlClientSocket sqrlSocket;
    public TransferConditionsRunnable(Context context) {
        mContext = context;
        sqrlNutz = new Nutz(mContext);
    }

    public void run() {
        String ssid = getWifiName();
    }

    public void attemptTransfer() {
        SqrlClientSocket socket = new SqrlClientSocket(mContext, sqrlNutz);
        if (socket.connectedToServer) {
            sqrlSocket = socket;
        }
        else {
            sqrlSocket = null;
        }
    }

    public String getWifiName() {
        WifiManager wifiMgr = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        String name = wifiInfo.getSSID();
        return name;
    }
}
