package com.gmoney.photosqrl;

import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;



import androidx.documentfile.provider.DocumentFile;
import androidx.loader.content.CursorLoader;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewPhotoMonitorJob extends JobService implements CopyPhotosCallbackTask {
    // The root URI of the media provider, to monitor for generic changes to it's content
    static final Uri MEDIA_URI = Uri.parse("content://" + MediaStore.AUTHORITY + "/");

    // path segments for image-specific UIRs in the provider
    static final List<String> EXTERNAL_PATH_SEGMENTS =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPathSegments();

    // the columns we want to retrieve about a particular image
    static final String[] PROJECTION = new String[] {
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATA};

    static final int PROJECTION_ID = 0;
    static final int PROJECTION_DATA = 1;

    // this is the external storage directory where cameras place pictures
    static final String DCIM_DIR = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DCIM).getPath();

    // job scheduler for service
    static JobScheduler scheduler;

    // a pre-built JobInfo to use for scheduling job
    static final JobInfo JOB_INFO;

    static {
        JobInfo.Builder builder = new JobInfo.Builder(JobIds.PHOTOS_CONTENT_JOB,
                new ComponentName("com.gmoney.photosqrl", NewPhotoMonitorJob.class.getName()));
        // look for specific changes to images in the provider
        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
        // also look for general reports of changes in the overall provider
        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(MEDIA_URI, 0));
        JOB_INFO = builder.build();
    }

    // fake job work. Real implementation, to be added later, will need to do work in separate thread here
    final Handler mHandler = new Handler();
    final Runnable mWorker = new Runnable() {
        @Override
        // The system handles calling run() when conditions contained in JOB_INFO are fulfilled
        public void run() {
            System.out.println("JOB HAS BEEN STARTED AND IS RUNNING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.out.println("Job info: ---- " + JOB_INFO);
            scheduleJob(NewPhotoMonitorJob.this);
            jobFinished(mRunningParams, false);
        }
    };

    JobParameters mRunningParams;


    public void start() {
        mHandler.post(mWorker);
    }

    // schedule this job, replace any existing one
    // This method is also in MainActivity.class in order to launch this JobService.
    // The only meaningful difference is that this method does not define JOB_INFO inside method
    public static void scheduleJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        js.schedule(JOB_INFO);
        Log.i("NewPhotoMonitorJob", "JOB SCHEDULED!");
    }

    // check whether this job is currently scheduled
    public static boolean isScheduled(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        List<JobInfo> jobs = js.getAllPendingJobs();
        if (jobs == null) {
            return false;
        }
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getId() == JobIds.PHOTOS_CONTENT_JOB) {
                return true;
            }
        }
        return false;
    }

    // cancel this job if currently scheduled
    public static void cancelJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        js.cancel(JobIds.PHOTOS_CONTENT_JOB);
    }

    // The actions that this job takes are contained here.
    // At the end, mHandler reposts this job so it runs endlessly
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i("NewPhotosMonitorJob", "JOB STARTED");
        mRunningParams = params;

        // instead pf real work, we are going to build a string to show to the user
        StringBuilder sb = new StringBuilder();

        // was it triggered due to content change?
        if (params.getTriggeredContentAuthorities() != null) {
            boolean rescanNeeded = false;

            if (params.getTriggeredContentUris() != null) {
                // if we have details about whichu uris changed, then iterate through them and
                // collect either the ids that were impacted or note that a generic change occurred
                ArrayList<String> ids = new ArrayList<>();
                for (Uri uri : params.getTriggeredContentUris()) {

                    CopyPhotoAsyncTask task = new CopyPhotoAsyncTask(uri, NewPhotoMonitorJob.this, NewPhotoMonitorJob.this);
                    task.execute();

                    //Bitmap bit = convertUriToBitmap(uri);
                    //saveBitmapToFile(bit);

                    List<String> path = uri.getPathSegments();
                    if (path != null && path.size() == EXTERNAL_PATH_SEGMENTS.size() + 1) {
                        // this is a specific file
                        ids.add(path.get(path.size() - 1));


                        //String ext = getFilePathFromUri(uri);

                        //System.out.println("FILE NAME :   --------- " + "." + ext);
                    }
                    else {
                        // there has been a general change
                        rescanNeeded = true;
                    }
                }
                if (ids.size() > 0) {
                    // if we found some ids that changed, we need to determine what they are
                    // content provider is queried to ask about them
                    StringBuilder selection = new StringBuilder();
                    for (int i = 0; i < ids.size(); i++) {
                        if (selection.length() > 0) {
                            selection.append(" OR ");
                        }
                        selection.append(MediaStore.Images.ImageColumns._ID);
                        selection.append("='");
                        selection.append(ids.get(i));
                        selection.append("'");
                    }
                    // iterate through the query while looking at the filenames of the items
                    // to determine if they are the ones needed
                    Cursor cursor = null;
                    boolean haveFiles = false;
                    try {
                        cursor = getContentResolver().query(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                PROJECTION, selection.toString(), null, null);
                        while (cursor.moveToNext()) {
                            // only look in DCIM directory
                            String dir = cursor.getString(PROJECTION_DATA);
                            if (dir.startsWith(DCIM_DIR)) {
                                if (!haveFiles) {
                                    haveFiles = true;
                                    sb.append("New photos:\n");
                                }
                                sb.append(cursor.getInt(PROJECTION_ID));
                                sb.append(": ");
                                sb.append(dir);
                                sb.append("\n");
                            }
                        }
                    }
                    catch (SecurityException e) {
                        sb.append("Error: no access to media");
                    }
                    finally {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
            }
            else {
                // too many URIs changed at once so we don't have any details, rescan is needed
                rescanNeeded = true;
            }
            if (rescanNeeded) {
                sb.append("Photos rescan needed");
            }
        }
        else {
            sb.append("(no photos content)");
        }
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + sb);

        // we will emulate taking some time to do this work, so we can see batching happen
        mHandler.postDelayed(mWorker, 10*1000);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mHandler.removeCallbacks(mWorker);
        return false;
    }

    public String getFilePathFromUri(Uri uri) {
        ContentResolver resolver = this.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String name = mime.getExtensionFromMimeType(resolver.getType(uri)); // returns just extension
        //String name = resolver.getType(uri); // returns in format image/jpg
        return name;
    }

    public Bitmap convertUriToBitmap(Uri uri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);

            parcelFileDescriptor.close();
            return image;
        }
        catch (IOException e) {

        }
        return null;
    }

    public void saveBitmapToFile(Bitmap bitmap) {
        try {
            FileOutputStream out = new FileOutputStream("/data/user/0/com.gmoney.photosqrl/files/SqrlNutz/");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CopyPhotosCallbackTask methods below
    ProgressBar progressBar;
    TextView percentText;
    private AlertDialog dialog;

    @Override
    public void onCopyPreExecute() {
        //final AlertDialog.Builder mPro = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.myDialog));
    }

    @Override
    public void onCopyProgressUpdate(int progress) {

    }

    @Override
    public void onCopyPostExecute(String path, boolean wasSuccessful, String reason) {

    }









}


/*                  What I've learned so far.
* To monitor content URI changes, JobService needs to be started by another activity. The JOB_INFO
* should be the same regardless of where it is called from. In order to continuously monitor for
* changes to the URIs, the JobService needs to start itself again before it ends. This is because
* TriggerContentUri only runs once before stopping. So by having JobService start itself before
* the job is finished, we can avoid having the user need to manually restart JobService after each
* time new photos are saved to PhotoSqrl gallery.
 */