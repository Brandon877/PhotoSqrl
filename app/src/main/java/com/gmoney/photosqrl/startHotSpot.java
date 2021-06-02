package com.gmoney.photosqrl;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import androidx.core.content.ContextCompat;

public class startHotSpot {
    private WifiManager wifiManager;
    WifiConfiguration currentConfig;
    static WifiManager.LocalOnlyHotspotReservation hotspotReservation;

    Context context;

    public startHotSpot(Context context) {
        this.context = context;
        wifiManager = (WifiManager) this.context.getSystemService(this.context.WIFI_SERVICE);
    }

    public void beginSharingWifi() {
        if (checkLocationPermission()) {
            if (checkWifiPermission()) {
                wifiManager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {
                    @Override
                    public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                        super.onStarted(reservation);
                        hotspotReservation = reservation;
                        currentConfig = hotspotReservation.getWifiConfiguration();

                        Log.v("DANG", "THE PASSWORD IS: "
                                + currentConfig.preSharedKey
                                + " \n SSID is : "
                                + currentConfig.SSID);
                        //hotspotDetailsDialog();
                    }

                    @Override
                    public void onStopped() {
                        super.onStopped();
                        Log.v("DANG", "Local Hotspot Stopped");
                    }

                    @Override
                    public void onFailed(int reason) {
                        super.onFailed(reason);
                        Log.v("DANG", "Local Hotspot failed to start");
                    }
                }, new Handler());
            }
        }
    }

    public void shutDownHotSpot() {
        if (hotspotReservation != null) {
            hotspotReservation.close();
        }
    }

    public boolean checkLocationPermission() {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            else{
                return true;
            }
        }

    public boolean checkWifiPermission() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        else{
            return true;
        }
    }


}
