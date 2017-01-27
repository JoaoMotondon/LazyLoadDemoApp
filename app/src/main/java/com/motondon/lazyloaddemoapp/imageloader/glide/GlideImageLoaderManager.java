package com.motondon.lazyloaddemoapp.imageloader.glide;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.motondon.lazyloaddemoapp.R;
import com.motondon.lazyloaddemoapp.imageloader.ImageLoaderManager;
import com.motondon.lazyloaddemoapp.model.ImageModel;

public class GlideImageLoaderManager extends ImageLoaderManager {

    private static final String TAG = GlideImageLoaderManager.class.getSimpleName();

    private RequestManager requestManager;

    public GlideImageLoaderManager(Context context, boolean useDiskCache, boolean useMemoryCache) {
        super(context, useDiskCache, useMemoryCache);
        Log.v(TAG, "ctor()");
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

        requestManager = Glide.with(mContext);

        DrawableTypeRequest<String> drawableTypeRequest = requestManager.load(url);

        // Add a listener so that we can listen for errors.
        addCustomListener(drawableTypeRequest);

        // This call forces Glide to return a Bitmap object since Glide can also load Gifs or videos. 
        // See link below for details:
        // https://futurestud.io/blog/glide-callbacks-simpletarget-and-viewtarget-for-custom-view-classes
        drawableTypeRequest.asBitmap();

        // Set a placeholder image to be displayed while image is being loaded
        // drawableTypeRequest.placeholder(R.drawable.ic_placeholder);
        imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_placeholder));

        // Set an error image to be displayed if image cannot be downloaded successfully
        drawableTypeRequest.error(R.drawable.no_cover);

        // Change the crossFate time (300ms is the default value)
        drawableTypeRequest.crossFade(250);

        // Display the image without a crossfade effect
        // drawableTypeRequest.dontAnimate();

        // Resize the image to these dimensions (in pixel). This is a "resize()" Picasso method equivalent.
        // drawableTypeRequest.override(600, 200);

        //  Prioritize the image loading with other simultaneously Glide loading requests. Default value is NORMAL.
        drawableTypeRequest.priority(Priority.HIGH);

        if (!memoryCache) {
            Log.d(TAG, "loadImage() - Detected Memory cache is OFF");
            // Skip memory cache
            drawableTypeRequest = (DrawableTypeRequest<String>) drawableTypeRequest.skipMemoryCache(true);
        } else {
            Log.d(TAG, "loadImage() - Detected Memory cache is ON");
        }

        if (!diskCache) {
            Log.d(TAG, "loadImage() - Detected Disk cache is OFF");
            // Skip disk cache
            drawableTypeRequest = (DrawableTypeRequest<String>) drawableTypeRequest.diskCacheStrategy(DiskCacheStrategy.NONE);
        } else {
            Log.d(TAG, "loadImage() - Detected Disk cache is ON");
        }

        // Create a Target (i.e. a callback) in order to be able to take any action before display the image.
        SimpleTarget target = new SimpleTarget<GlideBitmapDrawable>() {
            @Override
            public void onResourceReady(GlideBitmapDrawable drawable, GlideAnimation glideAnimation) {
            	Log.d(TAG, "loadImage::onResourceReady() - Download for image: " + url + " finished successfully");
            	imageView.setImageBitmap( drawable.getBitmap() );
            }
        };
        
        // And finally request Glide to download the image
        drawableTypeRequest.into(target);
    }

    /**
     * This method adds a custom listener so that we have a chance to listen for errors. Here we just print a log message.
     *
     * Also we can also take some actions when the image is ready (by adding some code to the onResourceReady() method). This is called
     * prior the SimpleTarget::onResourceReady() method.
     *
     * @param drawableTypeRequest
     */
    private void addCustomListener(DrawableRequestBuilder<String> drawableTypeRequest) {
        RequestListener<String, GlideDrawable> requestListener = new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                Log.e(TAG, "addCustomListener::onException() - Error while loading image: " + e.getMessage());

                // Do not forget to to return false so the error placeholder can be placed!
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                return false;
            }
        };

        drawableTypeRequest.listener(requestListener);
    }

    @Override
    public void clearCache() {
        Log.d(TAG, "clearCache() - Clearing both disk and memory caches");

        // Glide::clearMemory() method must be called in the Main UI Thread
        Glide.get(mContext).clearMemory();

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Call Glide::clearDiskCache() in a worker thread since it can take a while to be done.
                Glide.get(mContext).clearDiskCache();
            }
        }).start();
    }

    @Override
    public void onViewDetachedFromWindow(String url) {
        // Not used by Glide
    }
}