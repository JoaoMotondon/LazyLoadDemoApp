package com.motondon.lazyloaddemoapp.imageloader.uil;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.motondon.lazyloaddemoapp.R;
import com.motondon.lazyloaddemoapp.imageloader.ImageLoaderManager;
import com.motondon.lazyloaddemoapp.model.ImageModel;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

public class UilImageLoaderManager extends ImageLoaderManager {

    private static final String TAG = UilImageLoaderManager.class.getSimpleName();

    ImageLoader imageLoader;

    public UilImageLoaderManager(Context context, boolean useDiskCache, boolean useMemoryCache) {
        super(context, useDiskCache, useMemoryCache);

        Log.v(TAG, "ctor()");
        
        try {

           // Adjust some parameters. See link below for details:
           // https://github.com/nostra13/Android-Universal-Image-Loader/blob/master/sample/src/main/java/com/nostra13/universalimageloader/sample/UILApplication.java
           ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(mContext);
           config.threadPriority(Thread.NORM_PRIORITY - 2);

           config.denyCacheImageMultipleSizesInMemory();
           config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
           config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
           config.tasksProcessingOrder(QueueProcessingType.LIFO);
           config.writeDebugLogs(); // Remove for release app

           imageLoader = ImageLoader.getInstance();
           imageLoader.init(config.build());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This is the entry point to start an image download
     *
     * @param imageModel
     */
    @Override
    public void loadImage(ImageModel imageModel) {
        Log.d(TAG, "loadImage() - Loading image from URL: " + imageModel.getUrl());

        final String url = imageModel.getUrl();
        final ImageView imageView = imageModel.getImageView();
        
        DisplayImageOptions.Builder options = new DisplayImageOptions.Builder();
        if (memoryCache) {
            Log.d(TAG, "loadImage() - Detected Memory cache is ON");
            options = options.cacheInMemory(true);
        } else {
            // Skip memory cache
            Log.d(TAG, "loadImage() - Detected Memory cache is OFF");
            options = options.cacheInMemory(false);
        }

        if (diskCache) {
            Log.d(TAG, "loadImage() - Detected Disk cache is ON");
            options = options.cacheOnDisk(true);
        } else {
            // Skip disk cache
            Log.d(TAG, "loadImage() - Detected Disk cache is OFF");
            options = options.cacheOnDisk(false);
        }

        //  Image will be scaled exactly to the target size
        options.imageScaleType(ImageScaleType.EXACTLY);

        // Set a placeholder image to be displayed while image is being loaded
        options.showImageOnLoading(R.drawable.ic_placeholder);

        // Set an error image to be displayed if image cannot be downloaded successfully
        options.showImageOnFail(R.drawable.no_cover);

        // Show a fade for 300 ms when displaying image
        options.displayer(new FadeInBitmapDisplayer(300));

        DisplayImageOptions displayOptions = options.build();

        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(mContext);
        config.defaultDisplayImageOptions(displayOptions);

        // This will start the download. When it finishes (either in case of success or failure), a callback will  be
        // called
        imageLoader.displayImage(url, imageView, displayOptions, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                String message = null;
                switch (failReason.getType()) {
                    case IO_ERROR:
                        message = "Input/Output error";
                        break;
                    case DECODING_ERROR:
                        message = "Image can't be decoded";
                        break;
                    case NETWORK_DENIED:
                        message = "Downloads are denied";
                        break;
                    case OUT_OF_MEMORY:
                        message = "Out Of Memory error";
                        break;
                    case UNKNOWN:
                        message = "Unknown error";
                        break;
                }

                Log.e(TAG, "loadImage::onLoadingFailed() - Error while loading image: " + url + ". Message: " + message);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            	Log.d(TAG, "loadImage::onLoadingComplete() - Download for image: " + imageUri + " finished successfully");
                imageView.setImageBitmap(loadedImage);
            }
        });
    }

    @Override
    public void clearCache() {
        Log.d(TAG, "clearCache() - Clearing both disk and memory caches");
        
        imageLoader.clearDiskCache();
        imageLoader.clearMemoryCache();
    }

    @Override
    public void onViewDetachedFromWindow(String url) {
        // Not used by UIL
   }
}