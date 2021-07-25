package com.gmoney.photosqrl;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;

public class ReadyToTransferAlarm extends BroadcastReceiver {
    String DESIRED_SSID = "ATT957x7lF";
    int alarmHour; // 24 hour format
    int alarmMinute;
    @Override
    public void onReceive(Context context, Intent intent) {
        // check wifi ssid, then begin sending photos here
        String currentSSID = getWifiName(context);
        System.out.println("WIFI NAME: " + currentSSID);
        System.out.println("DESIRED WIFI NAME: " + DESIRED_SSID);
        if (DESIRED_SSID.equals(currentSSID)) {
            System.out.println("Connected to correct ssid");
            if (filesToTransfer(context)) {
                System.out.println("There are files to transfer");
                // attempt to connect to server. if successful, begin sending photos
                Nutz sqrlNutz = new Nutz(context);
                SqrlClientSocket socket = new SqrlClientSocket(context, sqrlNutz);
                if (socket.connectedToServer) {
                    System.out.println("Connected to server");
                    socket.sendPhoto();
                    System.out.println("Photos have been sent");
                    setNextAlarm(context, alarmHour, alarmMinute);
                }
                else {
                    System.out.println("No photos were sent");
                    setNextAlarm(context, alarmHour, alarmMinute);
                }
            }
            else {
                // no photos available to send
                System.out.println("No photos available to send");
                setNextAlarm(context, alarmHour, alarmMinute);
            }
        }
        else {
            // not connected to correct wifi
            System.out.println("Not connected to correct ssid");
            setNextAlarm(context, alarmHour, alarmMinute);
        }
    }
/*
    public void setAlarm(Context context, int interval) {
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReadyToTransferAlarm.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0 , intent, 0);
        mgr.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60 * interval, pIntent);// millisec sec min hours
    }
*/
    public void setNextAlarm(Context context, int setHour, int setMinute) {
        // set time of next alarm to 6pm
        alarmHour = setHour;
        alarmMinute = setMinute;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, setHour);
        calendar.set(Calendar.MINUTE, setMinute);
        calendar.set(Calendar.SECOND, 0);

        if (calendar.getTime().compareTo(new Date()) < 0) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            System.out.println("Added 1 day");
        }
        else {
            System.out.println("Stayed on current day");
        }

        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReadyToTransferAlarm.class);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, 0 , intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (mgr != null) {
            mgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pIntent);
            System.out.println("Mgr not null, next alarm has been sent");
        }
        else {
            System.out.println("Mgr null");
        }
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, ReadyToTransferAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(sender);
    }

    // retrieves name of current wifi network device is connected to
    public String getWifiName(Context context) {
        WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        String name = wifiInfo.getSSID();
        String convertedName = removeQuotes(name);
        return convertedName;
    }

    public boolean filesToTransfer(Context context) {
        Nutz sqrlNutz = new Nutz(context);
        if (sqrlNutz.nutz != null) {
            if (sqrlNutz.nutz.size() > 0) {
                return true;
            }
            else {
                // no nutz in gallery
                return false;
            }
        }
        else {
            // nutz = null
            return false;
        }
    }

    //
    public String removeQuotes(String string) {
        String noQuotesString = string.replace("\"", "");
        return noQuotesString;
    }
}
