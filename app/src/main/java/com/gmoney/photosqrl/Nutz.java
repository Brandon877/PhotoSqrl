package com.gmoney.photosqrl;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;

// Creates a ArrayList to hold the path to each photo that will be transferred
public class Nutz {
    public static ArrayList<Nutz> nutz;
    static Context context;
    String pathName;

    // used for creating ArrayList of nutz
    public Nutz(Context context) {
        this.context = context;
        this.nutz = createNutz();
    }

    // used for creating individual nutz
    public Nutz(String path) {
        this.pathName = path;
    }

    // returns path for given nut
    public String getPathName() {
        return pathName;
    }

    // gets path for each image in folder and save it as a Nutz object
    public ArrayList<Nutz> createNutz() {
        ArrayList<Nutz> tempNutz = new ArrayList<Nutz>();
        String dir = context.getExternalFilesDir("SqrlNutz").toString();
        File file = new File(dir);
        for (File child : file.listFiles()) {
            String path = child.getAbsolutePath();
            Nutz nut = new Nutz(path);
            tempNutz.add(nut);
        }
        return tempNutz;
    }
}
