package com.motondon.lazyloaddemoapp.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.motondon.lazyloaddemoapp.R;
import com.motondon.lazyloaddemoapp.model.ImageModel;

import java.util.ArrayList;
import java.util.Random;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{

    private MainFragment mainFragment;

    private ArrayList<String> images = new ArrayList<>();
    private Context context;

    private Random r = new Random();

    public ImageAdapter(Context context, MainFragment mainFragment) {
        this.context = context;
        this.mainFragment = mainFragment;

    }

    public void setData(ArrayList<String> newImages) {
        images = newImages;
        notifyDataSetChanged();
    }

    public void clearData() {
        images.clear();
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    /**
     * This method supplies the missing Fresco feature to cancel the image downloads related to views that is detached from 
     * the window. So we have to close all Fresco DataSource which will then cancel the downloads.
     *
     * @param holder
     */
    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        mainFragment.onViewDetachedFromWindow(holder.url);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String imageUrl = images.get(position);

        mainFragment.loadImage(new ImageModel(imageUrl, holder.cover));

        holder.url = imageUrl;
        holder.title.setText("Image Title");
        holder.voteAverage.setText("Vote Average: " + r.nextInt(100 - 0) + 0);
    }


    @Override
    public int getItemCount() {
        return images.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements RecyclerView.OnClickListener{

        private ImageView cover;
        private TextView title;
        private TextView voteAverage;
        private String url;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            voteAverage = (TextView) itemView.findViewById(R.id.voteAverage);
            cover = (ImageView) itemView.findViewById(R.id.image);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }
}