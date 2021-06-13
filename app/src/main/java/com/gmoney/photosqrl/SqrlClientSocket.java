package com.gmoney.photosqrl;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;

public class SqrlClientSocket extends Activity {
    int port = 8770;
    Socket server;
    DataOutputStream out;
    DataInputStream in;
    boolean connectedToServer = false;
    static ArrayList<byte[]> imageByteArrays;
    Context mContext;

    public SqrlClientSocket(Context context) {
        mContext = context;
        if (connectToServer()) {
            System.out.println("Connected to server, ready to send photo to PC");
            imageByteArrays = prepareAllPhotosToSend();
            connectedToServer = true;
        }
        else {
            System.out.println("Unable to connect to PC");
        }
    }

    public boolean connectToServer() {
        String serverIp = "192.168.0.14";
        //String serverIp = "remote1";
        try {
            server = new Socket(serverIp, port);
            out = new DataOutputStream(server.getOutputStream());
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Unable to connect to server");
        return false;
    }

    public void sendPhoto() {
        try {
            //ArrayList<byte[]> photosToSend = prepareAllPhotosToSend();
            out.write(imageByteArrays.get(0));
            //out.write(-1);

            System.out.println("Photo has been sent");
            server.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        //imageByteArrays.add(bytes);
        return bytes;
    }

    // iterates through each photo in folder and converts to byte array, saving array inside
    // class var "imageByteArray"
    public ArrayList<byte[]> prepareAllPhotosToSend() {
        ArrayList<byte[]> photosToSend = new ArrayList<>();
        String dir = mContext.getExternalFilesDir("SqrlNutz").toString();
        File galleryDir = new File(dir);
        System.out.println("Folder image gallery images are pulled from: " + galleryDir.getPath());
        for (File photo : galleryDir.listFiles()) {
            try {
                byte[] photoBytes = convertImageToBytes(photo);
                photosToSend.add(photoBytes);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return photosToSend;
    }
}
