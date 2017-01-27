package com.motondon.lazyloaddemoapp.imageloader.picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.motondon.lazyloaddemoapp.R;
import com.motondon.lazyloaddemoapp.imageloader.ImageLoaderManager;
import com.motondon.lazyloaddemoapp.model.ImageModel;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import java.io.File;
import java.util.HashMap;

public class PicassoImageLoaderManager extends ImageLoaderManager {

    private static final String TAG = PicassoImageLoaderManager.class.getSimpleName();

    private Picasso mPicasso;
    private Object mTag;

    // Hold a reference to every image being downloaded. This way we will be able to cancel a download when needed.
    private HashMap<String, Target> targetList = new HashMap<>();
    private Object sync = new Object();

    public PicassoImageLoaderManager(Context context, boolean useDiskCache, boolean useMemoryCache) {
        super(context, useDiskCache, useMemoryCache);

        Log.v(TAG, "ctor()");

        try {
            mPicasso = Picasso.with(mContext);

            // By calling .setIndicatorsEnabled(true), all imageView will have a small indicator on the top left corner:
            //   - green (memory, best performance)
            //   - blue (disk, good performance)
            //   - red (network, worst performance).
            ////////////////////////////////////////////////////////////////////
            // THIS IS NOT GOING TO WORK WHEN USING TARGET (CALLBACK) OBJECT!!!
            ////////////////////////////////////////////////////////////////////
            mPicasso.setIndicatorsEnabled(true);

            // All Picasso requests will have logs printed to the Android logcat
            // mPicasso.setLoggingEnabled(true);

            mTag = new Object();

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

        // Create the Target object which will receive the bitmap image or the error message after Picasso finishes its task.
        Target target = new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            	Log.d(TAG, "loadImage::onBitmapLoaded() - Download for image: " + url + " finished successfully");
            	
                imageView.setImageBitmap(bitmap);

                synchronized (sync) {
                    targetList.remove(url);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            	Log.e(TAG, "loadImage::onBitmapFailed() - Error while loading image: " + url);

                // Set an error image
                imageView.setImageBitmap(((BitmapDrawable) errorDrawable).getBitmap());

                synchronized (sync) {
                    targetList.remove(url);
                }
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                // Set an placeholder image
                imageView.setImageBitmap(((BitmapDrawable) placeHolderDrawable).getBitmap());
            }
        };

        // Now create a RequestCreator object which will allow us to configure some properties.
        RequestCreator requestCreator = mPicasso.load(url);

        // Set a tag so that will be possible to cancel all requests with a specific tag.
        requestCreator.tag(mTag);

        if (!memoryCache) {
            Log.d(TAG, "preparePicasso() - Detected Memory cache is OFF");
            requestCreator = requestCreator
                    .memoryPolicy(MemoryPolicy.NO_CACHE) // Skip memory cache
                    .memoryPolicy(MemoryPolicy.NO_STORE); // Prevent Picasso to store downloaded image into the memory cache
        } else {
            Log.d(TAG, "preparePicasso() - Detected Memory cache is ON");
        }

        if (!diskCache) {
            Log.d(TAG, "preparePicasso() - Detected Disk cache is OFF");

            requestCreator = requestCreator
                    .networkPolicy(NetworkPolicy.NO_CACHE) // Skip disk cache
                    .networkPolicy(NetworkPolicy.NO_STORE); // Prevent Picasso to store downloaded image into the disk cache
        } else {
            Log.d(TAG, "preparePicasso() - Detected Disk cache is ON");
        }

        // Set a placeholder image to be displayed while image is being loaded
        requestCreator.placeholder(R.drawable.ic_placeholder);

        // Set an error image to be displayed if image cannot be downloaded successfully
        requestCreator.error(R.drawable.no_cover);

        // Reduce the image size to the dimensions of the ImageView. This can delay the image request since Picasso will need to wait until the
        // size of the ImageView can be measured, but since the image is at the lowest possible resolution, without affecting its quality.
        // This means less data to be hold in the cache, which can significantly reduce the impact of images in the memory footprint.
        // This is the Glide's "override" method equivalent.
        // Unfortunately Picasso does not allow call "fit" when using a Target object. So we commented out this line. Let it here for future references
        //requestCreator.fit();

        //  Prioritize the image loading with other simultaneously Picasso loading requests. Default value is NORMAL.
        requestCreator.priority(Picasso.Priority.HIGH);

        // Request Picasso to download the image. When it is finished (either in case of success or failure) a Target callback will
        // be called. Remember that when using target, source indicator does not work.
        requestCreator.into(target);

        // If we need to use that little triangle on the left top corner indicating the source, we need to use this version, 
        // since indicator will not work when using targets
        //requestCreator.into(imageView);

        // Now, add the target to the target map in order to hold a reference to it, otherwise it can be collected by the garbage collector. This
        // reference will be removed from the map on these cases:
        //   - after a successful download
        //   - after an error during the download
        //   - when the view is detached from the window. On this case our adapter will call onViewDetachedFromWindow() method which will remove the
        //     related target from the list
        synchronized (sync) {
            targetList.put(url, target);
        }
    }

    @Override
    public void clearCache() {
        Log.d(TAG, "clearCache() - Clearing both disk and memory caches");

        clearMemoryCache();
        clearDiskCache();
    }

    private void clearMemoryCache() {
        Log.d(TAG, "clearMemoryCache()");

        // This is a hack to access a Picasso private package method. See PicassoTools class for details.
        PicassoTools.clearCache(mPicasso);
    }

    private boolean clearDiskCache() {
    	Log.d(TAG, "clearDiskCache()");
    	
        // Since Picasso does not offer any way to clean up its disk cache, we have do do it manually by removing entire "picasso-cache" folder
    	File cache = new File(mContext.getApplicationContext().getCacheDir(), "picasso-cache");
        if (cache.exists() && cache.isDirectory()) {
            return deleteDir(cache);
        }
        return false;
    }

	private boolean deleteDir(File dir) {
    	Log.d(TAG, "deleteDir() - Dir: " + dir);
    	
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     * When using a target (i.e. a callback) to load the images, Picasso will not cancel a download when the view it is attaches gets our
     * of visibility, so we have do do it manually.
     *
     * This method will be called when our adapter detects a view is detached from the window. Then we can cancel the pending request
     * related to the image that is out of visibility
     *
     * @param url
     */
    @Override
    public void onViewDetachedFromWindow(String url) {
        synchronized (sync) {
            Target target = targetList.get(url);
            if (target != null) {
                mPicasso.cancelRequest(target);
                targetList.remove(url);
            }
        }
    }
}