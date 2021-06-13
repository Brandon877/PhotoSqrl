package com.gmoney.photosqrl;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.ContextThemeWrapper;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

import static com.gmoney.photosqrl.NewPhotoMonitorJob.MEDIA_URI;

public class ForegroundService extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static NewPhotoMonitorJob job;
    static JobScheduler js;
    startHotSpot hotspot;

    @Override
    public void onCreate() {
        super.onCreate();
        js = (JobScheduler) getSystemService(JobScheduler.class);
        hotspot = new startHotSpot(ForegroundService.this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Foreground Service")
                //.setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        // ForegroundService.class starts the NewPhotoMonitorJob.class JobService
        scheduleJob();

        //do heavy work on a background thread

        //stopSelf()

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    // Same method that is in NewPhotoMonitorJob.class.  This is used to start the JobService.
    // After this initial call, NewPhotoMonitorJob is self-running
    public void scheduleJob() {
        JobInfo JOB_INFO;
        JobInfo.Builder builder = new JobInfo.Builder(JobIds.PHOTOS_CONTENT_JOB,
                new ComponentName("com.gmoney.photosqrl", NewPhotoMonitorJob.class.getName()));
        // look for specific changes to images in the provider
        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
        // also look for general reports of changes in the overall provider
        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(MEDIA_URI, 0));
        JOB_INFO = builder.build();
        js.schedule(JOB_INFO);
    }

    // checks if phone is connected to specified wifi network
    public boolean isConnectedTo(String ssid, Context context) {
        boolean retVal = false;
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifi.getConnectionInfo();
        if (wifiInfo != null) {
            String currentConnectedSSID = wifiInfo.getSSID();
            if (currentConnectedSSID != null && ssid.equals(currentConnectedSSID)) {
                retVal = true;
            }
        }
        return retVal;
    }

    // handles all of the work monitoring for conditions, then transferring files
    public void monitorTransferConditions() {
        // start new thread to handle monitoring for wifi and then sending photos

        // connected to correct wifi
        if (isConnectedTo("ATT957x71F", ForegroundService.this)) {
            hotspot.beginSharingWifi();
            
        }










        // NOT connected to correct wifi
        else {
            try {
                TimeUnit.MINUTES.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // returns true and starts local wifi sharing
    public boolean checkWifiNetwork() {
        if (isConnectedTo("ATT957x71F", ForegroundService.this)) {
            hotspot.beginSharingWifi();
            return true;
        } else {
            return false;
        }
    }




































}
