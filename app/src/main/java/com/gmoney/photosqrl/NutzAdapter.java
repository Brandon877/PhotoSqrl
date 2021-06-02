package com.gmoney.photosqrl;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

public class NutzAdapter extends RecyclerView.Adapter<NutzAdapter.ViewHolder> {
    private ArrayList<String> pathToNutz = new ArrayList<String>();


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;

        public ViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public NutzAdapter(ArrayList<String> paths) {
        this.pathToNutz = paths;
    }

    @Override
    public int getItemCount() {
        return pathToNutz.size();
    }

    @Override
    public NutzAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
        CardView cv = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_card_view, parent, false);
        return new ViewHolder(cv);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        ImageView imageView = (ImageView)cardView.findViewById(R.id.photo_image);
        Picasso.get().load(new File(pathToNutz.get(position))). into(imageView);
    }



}
