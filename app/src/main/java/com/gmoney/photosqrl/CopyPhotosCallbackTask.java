package com.gmoney.photosqrl;

public interface CopyPhotosCallbackTask {
    void onCopyPreExecute();
    void onCopyProgressUpdate(int progress);
    void onCopyPostExecute(String path, boolean wasSuccessful, String reason);
}
