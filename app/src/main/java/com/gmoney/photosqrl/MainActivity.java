package com.gmoney.photosqrl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;

import static com.gmoney.photosqrl.NewPhotoMonitorJob.MEDIA_URI;

/*              WHAT TO DO NEXT
* Create button to view PhotoSqrl's gallery.
* In NewPhotoMonitorJob.class, write code to grab copy of each newly added picture and create a
* second instance of the image. Then save that second image inside PhotoSqrl's gallery
*
 */


public class MainActivity extends AppCompatActivity {
    private final String GALLERY_FOLDER_NAME = "SqrlNutz";
    private String full_path_to_gallery;
    static JobScheduler js;

    Button btnStartService;
    Button btnStopService;
    Button btnGallery;
    Button btnStartHotspot;
    Button btnStopHotspot;
    ImageView imageview;

    Context context = this;
    startHotSpot hotspot;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupClickListener();
        createGallery();
        js = (JobScheduler) getSystemService(JobScheduler.class);
        hotspot = new startHotSpot(context);
    }

    public void createGallery() {
        String galleryFolder = getFilesDir().toString();
        File folder = new File(galleryFolder, GALLERY_FOLDER_NAME);
        full_path_to_gallery = galleryFolder + "/" + GALLERY_FOLDER_NAME + "/";
        System.out.println("GALLERY PATH: " + full_path_to_gallery);
        boolean dirCreated = folder.mkdirs();
        if (!dirCreated) {
            System.out.println("Directory not created.  May already exist");
        }
    }

    public void setupClickListener() {
        btnStartService = findViewById(R.id.button_start_service);
        btnStopService = findViewById(R.id.button_stop_service);
        btnGallery = findViewById(R.id.button_gallery);
        btnStartHotspot = findViewById(R.id.button_start_hotspot);
        btnStopHotspot = findViewById(R.id.button_stop_hotspot);

        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMediaPermissions();
                startService();
            }
        });
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
            }
        });
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                printPaths();
                Intent intent = new Intent(MainActivity.this, NutzGalleryActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        btnStartHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestWifiPermissions();
                hotspot.beginSharingWifi();
            }
        });
        btnStopHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestWifiPermissions();
                hotspot.shutDownHotSpot();
            }
        });
    }

    public void startService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");

        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
    }

    public void requestMediaPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    public void requestWifiPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[] {Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    //!!! for testing
    public void setBitmap() {

    }

    public void printPaths() {
        Nutz SqrlNutz = new Nutz(MainActivity.this);
        for (Nutz nut : SqrlNutz.nutz) {
            System.out.println(nut.getPathName());
        }
    }

}


/*
    // Same method that is in NewPhotoMonitorJob.class.  This is used to start the JobService.
    // After this initial call, NewPhotoMonitorJob is self-running
    public void scheduleJob() {
        //JobScheduler js;
        //js = (JobScheduler) getSystemService(JobScheduler.class);
        JobInfo JOB_INFO;
        //System.out.println(getPackageName().toString());
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

 */