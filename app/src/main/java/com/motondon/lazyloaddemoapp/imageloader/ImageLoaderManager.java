package com.motondon.lazyloaddemoapp.imageloader;

import com.motondon.lazyloaddemoapp.model.ImageModel;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by Joca on 4/13/2016.
 */
public abstract class ImageLoaderManager {

    private static final String TAG = ImageLoaderManager.class.getSimpleName();

    protected Context mContext;

    protected Boolean diskCache;
    protected Boolean memoryCache;

    public abstract void loadImage(ImageModel imageModel);

    public abstract void clearCache();

    // Currently this method is only used in the Fresco implementation. See Fresco implementation for details.
    public abstract void onViewDetachedFromWindow(String url);

    public ImageLoaderManager(Context context, boolean useDiskCache, boolean useMemoryCache) {
    	Log.v(TAG, "ctor()");
        this.mContext = context;
        this.diskCache = useDiskCache;
        this.memoryCache = useMemoryCache;
    }

    public void setMemoryCache(Boolean memoryCache) {
        Log.v(TAG, "setMemoryCache: " + memoryCache);
        this.memoryCache = memoryCache;
    }

    public void setDiskCache(Boolean diskCache) {
        Log.v(TAG, "setDiskCache: " + diskCache);
        this.diskCache = diskCache;
    }
}
