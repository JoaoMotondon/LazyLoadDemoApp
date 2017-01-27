package com.motondon.lazyloaddemoapp.imageloader.manual.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import com.motondon.lazyloaddemoapp.R;
import com.motondon.lazyloaddemoapp.imageloader.ImageLoaderManager;
import com.motondon.lazyloaddemoapp.imageloader.manual.cache.FileCache;
import com.motondon.lazyloaddemoapp.imageloader.manual.cache.MemoryCache;
import com.motondon.lazyloaddemoapp.imageloader.manual.task.PhotosLoader;
import com.motondon.lazyloaddemoapp.model.ImageModel;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Code downloaded from: http://androidexample.com/Download_Images_From_Web_And_Lazy_Load_In_ListView_-_Android_Example/index.php?view=article_discription&aid=112&aaid=134
 *
 * This class is responsible to download images from the Internet. It also manages cache on both memory and disk (with MemoryCache and FileCache classes respectively)
 * 
 */
public class ManualImageLoaderManager extends ImageLoaderManager {

    private static final String TAG = ManualImageLoaderManager.class.getSimpleName();

    // Initialize MemoryCache
    private MemoryCache mMemoryCacheObject;

    // Use to store images on the disk
    private FileCache fileCache;

    // Hold a reference to every image being downloaded. This way we will be able to cancel a download when needed.
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    private ExecutorService executorService;

    // Define a placeholder image
    final int stub_id = R.drawable.ic_placeholder;

    // Handler to display images in UI thread
    private Handler handler = new Handler();

    public ManualImageLoaderManager(Context context, boolean useDiskCache, boolean useMemoryCache) {
        super(context, useDiskCache, useMemoryCache);

        mMemoryCacheObject = new MemoryCache();
        fileCache = new FileCache(context);

        // Creates a thread pool that reuses a fixed number of threads operating off a shared unbounded queue.
        executorService = Executors.newFixedThreadPool(5);
    }

    /**
     * This is the entry point to start an image download. It will first look in the cache will only 
     * download the image if it is not available there
     * 
     * @param imageModel
     */
    @Override
    public void loadImage(ImageModel imageModel) {
        Log.d(TAG, "loadImage() - Loading image from URL: " + imageModel.getUrl());

        String url = imageModel.getUrl();
        final ImageView imageView = imageModel.getImageView();

        // Store the image imageView and url in our Map, so that we can avoid issues when an imageView is recycled by the system (i.e.: when
        // a download for an image finishes after an user scrolled the list and system reused that same imageView. So in this
        // case we cannot update the image just downloaded). See link below for a better explanation:
        // http://negativeprobability.blogspot.com.br/2011/08/lazy-loading-of-images-in-listview.html
        imageViews.put(imageView, url);

        if (memoryCache) {
            Log.d(TAG, "loadImage() - Detected Memory cache is ON");
            //Check image is stored in MemoryCache Map or not. If so, just return it.
            Bitmap bitmap = mMemoryCacheObject.getImageFromCache(url);

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                return;
            }
        } else {
            Log.d(TAG, "loadImage() - Detected Memory cache is OFF");
        }

        if (diskCache) {
            Log.d(TAG, "loadImage() - Detected Disk cache is ON");
        } else {
            Log.d(TAG, "loadImage() - Detected Disk cache is OFF");
        }

        // Queue the request
        queuePhoto(new ImageModel(url, imageView));

        //Before downloading image show the placeholder image
        imageView.setImageResource(stub_id);
    }

    /**
     * Pass ImageModel object to a new PhotosLoader instance and submit it to the executor. This will start the download
     *
     * @param imageModel
     */
    private void queuePhoto(ImageModel imageModel) {
        executorService.submit(new PhotosLoader(this, imageModel, diskCache, memoryCache));
    }

    /**
     * Used prior to display the image to an imageView
     * 
     * @param imageModel
     * @return
     */
    public boolean imageViewReused(ImageModel imageModel) {

        String tag = imageViews.get(imageModel.getImageView());
        //Check url is already exist in imageViews MAP
        if (tag == null || !tag.equals(imageModel.getUrl())) {
            return true;
        }

        return false;
    }

    /**
     * Display the image in the thread that handler is attached (in this case the UI thread)
     *
     * @param bmp
     * @param imageModel
     */
    public void displayImage(final Bitmap bmp, final ImageModel imageModel) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (imageViewReused(imageModel))
                    return;

                // Show bitmap on UI
                if (bmp != null)
                    imageModel.getImageView().setImageBitmap(bmp);
                else
                    imageModel.getImageView().setImageResource(stub_id);
            }
        });
    }

    public void addMemoryCache(String url, Bitmap bmp) {
        if (memoryCache) {
            mMemoryCacheObject.addImageToCache(url, bmp);
        }
    }

    public File getFileCache(String url) {
        if (diskCache) {
            return fileCache.getFile(url);
        } else {
            return null;
        }
    }

    public void clearMemoryCache() {
        if (memoryCache) {
            mMemoryCacheObject.clear();
        }
    }

    @Override
    public void clearCache() {
        Log.d(TAG, "clearCache() - Clearing both disk and memory caches");

        if (memoryCache) {
            mMemoryCacheObject.clear();
        }

        if (diskCache) {
            fileCache.clear();
        }
    }

    @Override
    public void onViewDetachedFromWindow(String url) {
        // Currently used by the Fresco and Picasso implementations (this last one is necessary when working with targets)
    }
}