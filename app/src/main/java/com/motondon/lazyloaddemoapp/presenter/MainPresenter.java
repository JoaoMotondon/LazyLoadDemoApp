package com.motondon.lazyloaddemoapp.presenter;

import android.widget.ImageView;

import com.motondon.lazyloaddemoapp.model.ImageDownloaderEngine;
import com.motondon.lazyloaddemoapp.model.ImageModel;;

/**
 * Created by Joca on 4/19/2016.
 */
public interface MainPresenter {

    void loadImage(ImageModel imageModel);

    void clearCache();

    void setMemoryCache(Boolean useMemoryCache);

    void setDiskCache(Boolean useDiskCache);

    void setDownloadEngine(ImageDownloaderEngine downloadEngine);

    void onViewDetachedFromWindow(String url);
}