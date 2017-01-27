package com.motondon.lazyloaddemoapp.presenter;

import android.content.Context;
import android.widget.ImageView;

import com.motondon.lazyloaddemoapp.imageloader.ImageLoaderManager;
import com.motondon.lazyloaddemoapp.imageloader.fresco.FrescoImageLoaderManager;
import com.motondon.lazyloaddemoapp.imageloader.glide.GlideImageLoaderManager;
import com.motondon.lazyloaddemoapp.imageloader.manual.manager.ManualImageLoaderManager;
import com.motondon.lazyloaddemoapp.imageloader.picasso.PicassoImageLoaderManager;
import com.motondon.lazyloaddemoapp.imageloader.uil.UilImageLoaderManager;
import com.motondon.lazyloaddemoapp.model.ImageDownloaderEngine;
import com.motondon.lazyloaddemoapp.model.ImageModel;
import com.motondon.lazyloaddemoapp.view.MainFragment;

/**
 * Created by Joca on 4/11/2016.
 */
public class MainPresenterImpl implements MainPresenter {

    private Context mContext;
    private MainFragment mView;
    private ImageLoaderManager imageLoaderManager;

    private Boolean useDiskCache = true;
    private Boolean useMemoryCache = true;


    public MainPresenterImpl(Context context, MainFragment mainFragment) {
        this.mView = mainFragment;
        this.mContext = context;
    }

    @Override
    public void loadImage(ImageModel imageModel) {
        imageLoaderManager.loadImage(imageModel);
    }

    @Override
    public void clearCache() {
        imageLoaderManager.clearCache();
    }

    @Override
    public void setMemoryCache(Boolean useMemoryCache) {
        this.useMemoryCache = useMemoryCache;
        imageLoaderManager.setMemoryCache(useMemoryCache);
    }

    @Override
    public void setDiskCache(Boolean useDiskCache) {
        this.useDiskCache = useDiskCache;
        imageLoaderManager.setDiskCache(useDiskCache);
    }

    @Override
    public void setDownloadEngine(ImageDownloaderEngine downloadEngine) {

        switch (downloadEngine) {
            case MANUAL:
                this.imageLoaderManager = new ManualImageLoaderManager(mContext, useDiskCache, useMemoryCache);
                break;

            case PICASSO:
                this.imageLoaderManager = new PicassoImageLoaderManager(mContext, useDiskCache, useMemoryCache);
                break;

            case GLIDE:
                this.imageLoaderManager = new GlideImageLoaderManager(mContext, useDiskCache, useMemoryCache);
                break;

            case UIL:
                this.imageLoaderManager = new UilImageLoaderManager(mContext, useDiskCache, useMemoryCache);
                break;

            case FRESCO:
                this.imageLoaderManager = new FrescoImageLoaderManager(mContext, useDiskCache, useMemoryCache);
                break;
        }
        
        clearCache();

        setDiskCache(useDiskCache);
        setMemoryCache(useMemoryCache);

        mView.onResetAdapter();
    }

    @Override
    public void onViewDetachedFromWindow(String url) {
        this.imageLoaderManager.onViewDetachedFromWindow(url);
    }
}
