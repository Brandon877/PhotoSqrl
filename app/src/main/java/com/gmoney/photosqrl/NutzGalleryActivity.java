package com.gmoney.photosqrl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class NutzGalleryActivity extends AppCompatActivity {

    NutzAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutz_gallery);

        ArrayList<String> nutzPathNames = new ArrayList<String>();
        Nutz sqrlNutz = new Nutz(this);
        ArrayList<Nutz> pathToNutz = sqrlNutz.nutz;
        for (Nutz nut : pathToNutz) {
            nutzPathNames.add(nut.getPathName());
        }

        RecyclerView nutRecycler = findViewById(R.id.nut_recycler);
        adapter = new NutzAdapter(nutzPathNames);
        nutRecycler.setAdapter(adapter);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        nutRecycler.setLayoutManager(layoutManager);

    }
}