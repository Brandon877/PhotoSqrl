package com.gmoney.photosqrl;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/****************** Socket codes **********************************************************
 * -999 : Sent by server to inform client photo size has been received
 * -555 : Sent by server to inform client that all bytes for a single photo have been sent
 * -111 : Sent by client to inform serve that ALL photos have been sent
 ****************************************************************************************8*/

public class SqrlClientSocket extends Activity {
    int port;
    Socket server;
    DataOutputStream out;
    DataInputStream in;
    boolean connectedToServer = false;
    //static ArrayList<byte[]> imageByteArrays;
    //static ArrayList<String> imagePaths;
    Context mContext;
    Nutz sqrlNutz;

    // when initializing class, attempt is made to connect to connect to server.
    // context used for accessing file dir
    public SqrlClientSocket(Context context, Nutz nutz) {
        mContext = context;
        if (connectToServer()) {
            System.out.println("Connected to server, ready to send photo to PC");
            prepareAllPhotosToSend();
            sqrlNutz = nutz;
            connectedToServer = true;
        }
        else {
            System.out.println("Unable to connect to PC");
        }
    }

    // connects to server and initializes streams
    // 192.168.1.139
    public boolean connectToServer() {
        String serverIp = getIp();
        port = Integer.parseInt(getPort());
        try {
            server = new Socket(serverIp, port);
            out = new DataOutputStream(server.getOutputStream());
            in = new DataInputStream(server.getInputStream());
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Unable to connect to server");
        return false;
    }

    // iterates through each Nutz object's byte array.  For each byte array, size of photo
    // is first sent to server, server then responds when size is received.  Next the byte array
    // of the photo is sent to the server.  Finally wait for wait for confirmation from server
    // that photo was received before iterating to next photo byte array.  After all photos sent,
    // sends code to server informing it no more photos will be sent before closing socket.
    public void sendPhoto() {
        try {
            for (Nutz nut : sqrlNutz.nutz) {
                byte[] photoBytes = nut.getByteArray();
                int arrLength = photoBytes.length;
                System.out.println("Array length: " + arrLength);

                // send size of photo
                out.writeInt(arrLength);

                // wait for confirmation size was received
                boolean clientReady = false;
                System.out.println("Waiting for server to confirm receipt of image size...");
                while (!clientReady) {
                    int serverResponse = in.readInt();
                    if (serverResponse == -999) {
                        clientReady = true;
                        System.out.println("Received code -999.  Server received image size");
                    }
                }

                // send image
                out.write(photoBytes);
                System.out.println("Photo has been sent");

                // wait for confirmation photo was received
                clientReady = false;
                System.out.println("Waiting for server to confirm receipt of image...");
                while (!clientReady) {
                    int serverResponse = in.readInt();
                    if (serverResponse == -555) {
                        clientReady = true;
                        File file = new File(nut.getPathName());
                        if (file.delete()) {
                            System.out.println("File deleted");
                        }
                        else {
                            System.out.println("File not deleted");
                    }
                        System.out.println("Received code -555.  Server received image");
                    }
                }
            }
            out.writeInt(-111);
            server.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // returns a byte array of file passed in
    public byte[] convertImageToBytes(File file) throws IOException {
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        byte[] tempBuff = new byte[size];
        FileInputStream fis = new FileInputStream(file);
        try {
            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tempBuff, 0, remain);
                    System.arraycopy(tempBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Image not converted to  bytes");
            return null;
        }
        finally {
            fis.close();
        }
        return bytes;
    }

    // iterates through each Nutz object and retrieves path to photo.  From the path it then creates
    // a byte array for the photo and saves byte array to to the Nutz object for later retrieval.
    public void prepareAllPhotosToSend() {
    //public ArrayList<byte[]> prepareAllPhotosToSend() {
        //sqrlNutz = new Nutz(mContext);
        for (Nutz nut : sqrlNutz.nutz) {
            String pathName = nut.getPathName();
            System.out.println("PATH NAME IS: " + pathName);
            File photo = new File(pathName);

            try {
                byte[] photoBytes = convertImageToBytes(photo);
                nut.setByteArray(photoBytes);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getIp() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultIp = getResources().getString(R.string.saved_ip_address_default_key);
        String ip = sharedPref.getString(getString(R.string.saved_ip_address_key), defaultIp);
        return ip;
    }

    private String getPort() {
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String defaultPort = getResources().getString(R.string.saved_port_number_default_key);
        String port = sharedPref.getString(getString(R.string.saved_port_number_key), defaultPort);
        return port;
    }
}
