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
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

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

    Button btnStartStopService;
    Button btnGallery;
    Button btnSetup;
    Button btnStopHotspot;
    Button btnCheckForDevices;

    Context context = this;
    startHotSpot hotspot;

    Nutz sqrlNutz;
    SqrlClientSocket sqrlSocket;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // to be taken out and socket work needs to be added into async task instead-----------
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //----------------------------------------------------------------------------------------
        setupClickListener();
        createGallery();
        js = (JobScheduler) getSystemService(JobScheduler.class);
        //hotspot = new startHotSpot(context);
        sqrlNutz = new Nutz(this);
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
        btnStartStopService = findViewById(R.id.button_start_stop_service);
        btnGallery = findViewById(R.id.button_gallery);
        btnSetup = findViewById(R.id.button_setup);
        btnStopHotspot = findViewById(R.id.button_stop_hotspot);
        btnCheckForDevices = findViewById(R.id.button_check_for_devices);

        btnStartStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startServiceActivity();
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

        btnSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetupActivity();
            }
        });
        /*
        btnStopHotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestWifiPermissions();
                hotspot.shutDownHotSpot();
            }
        });
        btnCheckForDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestMediaPermissions();
                connectToServer();
                sendPhotos();
            }
        });

         */
    }
/*

    public void requestMediaPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_WIFI_STATE};
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }
*/
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
        //Nutz SqrlNutz = new Nutz(MainActivity.this);
        for (Nutz nut : sqrlNutz.nutz) {
            System.out.println(nut.getPathName());
        }
    }

    // attempt to connect to server.  if connection is made, assigns socket to class var sqrlSocket
    // if not connection is made, return null socket
    public void connectToServer() {
        SqrlClientSocket socket = new SqrlClientSocket(this, sqrlNutz);
        if (socket.connectedToServer) {
            sqrlSocket = socket;
        }
        else {
            sqrlSocket = null;
        }
    }

    private void startSetupActivity() {
        Intent intent = new Intent(MainActivity.this, SetupActivity.class);
        MainActivity.this.startActivity(intent);
    }

    private void startServiceActivity() {
        Intent intent = new Intent(MainActivity.this, StartStopServiceActivity.class);
        MainActivity.this.startActivity(intent);
    }

    public void sendPhotos() {
        sqrlSocket.sendPhoto();
    }
}