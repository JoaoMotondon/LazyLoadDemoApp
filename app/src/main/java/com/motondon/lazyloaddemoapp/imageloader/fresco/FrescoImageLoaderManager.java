package com.motondon.lazyloaddemoapp.imageloader.fresco;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

import com.facebook.common.executors.UiThreadImmediateExecutorService;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.motondon.lazyloaddemoapp.R;
import com.motondon.lazyloaddemoapp.imageloader.ImageLoaderManager;
import com.motondon.lazyloaddemoapp.model.ImageModel;

import java.util.HashMap;

public class FrescoImageLoaderManager extends ImageLoaderManager {

    private static final String TAG = FrescoImageLoaderManager.class.getSimpleName();

    // Hold a reference to every image being downloaded. This way we will be able to cancel a download when needed.
    private HashMap<String, DataSource<CloseableReference<CloseableImage>>> pendingImageLoad = new HashMap<>();
    private Object sync = new Object();

    public FrescoImageLoaderManager(Context context, boolean useDiskCache, boolean useMemoryCache) {
        super(context, useDiskCache, useMemoryCache);

        Log.v(TAG, "ctor()");
        
        Fresco.initialize(mContext);
    }

    /**
     * This is the entry point to start an image download
     *
     * @param imageModel
     */
    @Override
    public void loadImage(ImageModel imageModel) {
        Log.d(TAG, "loadImage() - Loading image from URL: " + imageModel.getUrl());

        String url = imageModel.getUrl();
        final ImageView imageView = imageModel.getImageView();

        imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_placeholder));

        if (memoryCache) {
            Log.d(TAG, "loadImage() - Detected Memory cache is ON");
        } else {
            // Skip memory cache
            Log.d(TAG, "loadImage() - Detected Memory cache is OFF");
            Fresco.getImagePipeline().evictFromMemoryCache(Uri.parse(url));
        }

        if (diskCache) {
            Log.d(TAG, "loadImage() - Detected Disk cache is ON");
        } else {
            // Skip disk cache
            Log.d(TAG, "loadImage() - Detected Disk cache is OFF");
            Fresco.getImagePipeline().evictFromDiskCache(Uri.parse(url));
        }

        setBaseBitmapDataSubscriber(mContext, Uri.parse(url), 0, 0, imageView);
    }

    private void setBaseBitmapDataSubscriber(Context context, final Uri uri, int width, int height, final ImageView imageView) {
    	Log.d(TAG, "setBaseBitmapDataSubscriber()");
    	
        // This is the subscriber recommended in the Fresco documentation when what we need is just a bitmap.
        final BaseBitmapDataSubscriber baseBitmapDataSubscriber = new BaseBitmapDataSubscriber() {

            @Override
            public void onNewResultImpl(@Nullable final Bitmap bitmap) {
                if (bitmap != null && !bitmap.isRecycled()) {
                	Log.d(TAG, "setBaseBitmapDataSubscriber::onNewResultImpl() - Download for image: " + uri.toString() + " finished successfully");
                    // According to the documentation we cannot assign the bitmap to any variable outside the onNewResultImpl method scope.
                    // The reason is, after the subscriber has finished executing, the image pipeline will recycle the bitmap and free its memory.
                    // See link below for details:
                    // http://frescolib.org/docs/datasources-datasubscribers.html
                    // In order to fix it, we create a bitmap, copy and assign it to our view.
                    Bitmap bitmapCloned = bitmap.copy(bitmap.getConfig(), true);
                    imageView.setImageBitmap(bitmapCloned); 

                    // Now remove the datasource from the list since download has finished.
                    synchronized (sync) {
                        pendingImageLoad.remove(uri.toString());
                    }
                }
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
            	Log.e(TAG, "setBaseBitmapDataSubscriber::onFailureImpl() - Error while loading image: " + uri.toString());
            	
                // In case of fail, set the error (no cover) image.
                imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.no_cover));

                // And also remove the datasource from the list.
                synchronized (sync) {
                    pendingImageLoad.remove(uri.toString());
                }
            }
        };

        subscribe(context, uri, width, height, baseBitmapDataSubscriber);
    }

    /**
     * @param context
     * @param uri
     * @param width
     * @param height
     * @param dataSubscriber
     */
    public void subscribe(Context context, Uri uri, int width, int height, DataSubscriber dataSubscriber) {
    	Log.d(TAG, "subscribe()");
    	
        final ImagePipeline imagePipeline = Fresco.getImagePipeline();

        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);

        if (width > 0 && height > 0) {
            builder.setResizeOptions(new ResizeOptions(width, height));
        }

        // Create an ImageRequest object in order to fetch image directly from the pipeline.
        ImageRequest request = builder.build();

        // Now create a datasource. It will be used if we need to cancel the download while it is in progress
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage(request, context);

        // And finally subscribe the dataSubscriber informing which kind of executor to be used (we will use one that allows us to deal
        // with the view in the UI thread. 
        // This will start a download. When it is finished, it will call either BaseBitmapDataSubscriber::onNewResultImpl(),
        // or BaseBitmapDataSubscriber::onFailureImpl(), depends on the result.
        dataSource.subscribe(dataSubscriber, UiThreadImmediateExecutorService.getInstance());

        // Now, add the url and dataSource in the list, so that we can cancel this request if we need
        synchronized (sync) {
            pendingImageLoad.put(uri.toString(), dataSource);
        }
    }

    @Override
    public void clearCache() {
        Log.d(TAG, "clearCache() - Clearing both disk and memory caches");
        Fresco.getImagePipeline().clearCaches();
    }

    
    /**
     * When using a pipeline directly, Fresco will not cancel a download when the view it is attaches gets our of visibility, so we
     * have do do it manually.
     *
     * This method will be called when our adapter detects a view is detached from the window. Then we can close the dataSource related
     * to the image which will trigger the download cancellation. 
     *
     * See this link for details: http://stackoverflow.com/questions/33961167/fresco-cancel-prefetch-requests
     *
     * @param url
     */
    @Override
    public void onViewDetachedFromWindow(String url) {
        synchronized (sync) {
            DataSource<CloseableReference<CloseableImage>> ds = pendingImageLoad.get(url);
            if (ds != null) {
                ds.close();
                pendingImageLoad.remove(url);
                Log.d(TAG, "Datasource closed -> url: " + url);
                return;
            }
        }
    }
}