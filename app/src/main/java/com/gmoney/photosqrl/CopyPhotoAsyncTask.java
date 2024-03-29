package com.gmoney.photosqrl;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

public class CopyPhotoAsyncTask extends AsyncTask<Uri, Integer, String> {
    private Uri uri;
    private CopyPhotosCallbackTask callback;
    private WeakReference<Context> mContext;
    private String pathPlusName;
    private File folder;
    private Cursor returnCursor;
    private InputStream is = null;
    private String errorReason = "";

     public CopyPhotoAsyncTask(Uri uri, Context context, CopyPhotosCallbackTask callback) {
         this.uri = uri;
         mContext = new WeakReference<>(context);
         this.callback = callback;
     }

    @Override
    protected void onPreExecute() {
        callback.onCopyPreExecute();
        Context context = mContext.get();
        if (context != null) {
            folder = context.getExternalFilesDir("SqrlNutz");
            returnCursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                is = context.getContentResolver().openInputStream(uri);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int post = values[0];
        callback.onCopyProgressUpdate(post);
    }

    @Override
    protected String doInBackground(Uri... params) {
        File file = null;
        int size = -1;

        try {
            try {
                if (returnCursor != null && returnCursor.moveToFirst()) {
                    if (uri.getScheme() != null) {
                        if (uri.getScheme().equals("contet")) {
                            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                            size = (int) returnCursor.getLong(sizeIndex);
                        }
                        else if (uri.getScheme().equals("file")) {
                            File ff = new File(uri.getPath());
                            size = (int) ff.length();
                        }
                    }
                }
            }
            finally {
                if (returnCursor != null) {
                    returnCursor.close();
                }
            }

            pathPlusName = folder + "/" + getFileName(uri, mContext.get());
            file = new File(folder + "/" + getFileName(uri, mContext.get()));

            BufferedInputStream bis = new BufferedInputStream(is);
            FileOutputStream fos = new FileOutputStream(file);

            byte[] data = new byte[1024];
            long total = 0;
            int count;
            while ((count = bis.read(data)) != -1) {
                if (!isCancelled()) {
                    total += count;
                    if (size != -1) {
                        publishProgress((int) ((total * 100) / size));
                    }
                    fos.write(data, 0, count);
                }
            }
            fos.flush();
            fos.close();
        }
        catch (IOException e) {
            errorReason = e.getMessage();
        }
        return file.getAbsolutePath();
    }

    private String getFileName(Uri uri, Context context) {
         String result = null;
         if (uri.getScheme() != null) {
             if (uri.getScheme().equals("content")) {
                 Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                 if (cursor != null && cursor.moveToFirst()) {
                     result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                 }
                 if (cursor != null) {
                     cursor.close();
                 }
             }
         }

         if (result == null) {
             result = uri.getPath();
             assert result != null;
             int cut = result.lastIndexOf('/');
             if (cut != -1) {
                 result = result.substring(cut + 1);
             }
         }
         return result;
    }

    protected void onPostExecute(String result) {
         if (result == null) {
             callback.onCopyPostExecute(pathPlusName, false, errorReason);
         }
         else {
             callback.onCopyPostExecute(pathPlusName, true, "");
         }
    }

}
