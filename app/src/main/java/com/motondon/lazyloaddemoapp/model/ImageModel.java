package com.motondon.lazyloaddemoapp.model;

import android.widget.ImageView;

/**
 * Class that wraps the url for an image to be downloaded as well as the imageView view which the image will be displayed after the download
 * 
 */
public class ImageModel {

    private String url;
    private ImageView imageView;

    public ImageModel(String url, ImageView imageView) {
        this.url = url;
        this.imageView = imageView;
    }

    public String getUrl() {
        return url;
    }

    public ImageView getImageView() {
        return imageView;
    }
}