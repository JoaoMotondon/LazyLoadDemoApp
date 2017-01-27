package com.motondon.lazyloaddemoapp.imageloader.manual.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.motondon.lazyloaddemoapp.imageloader.manual.manager.ManualImageLoaderManager;
import com.motondon.lazyloaddemoapp.imageloader.manual.utils.Utils;
import com.motondon.lazyloaddemoapp.model.ImageModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Code downloaded from https://github.com/thest1/LazyList
 * 
 */
public class PhotosLoader implements Runnable {
    private static final String TAG = PhotosLoader.class.getSimpleName();

    private ImageModel imageModel;
    private ManualImageLoaderManager manualImageLoaderManager;

    private Boolean useDiskCache;
    private Boolean useMemoryCache;

    public PhotosLoader(ManualImageLoaderManager manualImageLoaderManager, ImageModel imageModel, boolean useDiskCache, boolean useMemoryCache) {
        this.manualImageLoaderManager = manualImageLoaderManager;
        this.imageModel = imageModel;

        this.useDiskCache = useDiskCache;
        this.useMemoryCache = useMemoryCache;
    }

    @Override
    public void run() {
        try {
            // Prior to download an image, check if image was already downloaded. If so, just return.
            if (manualImageLoaderManager. imageViewReused(imageModel))
                return;

            // This will start the download
            final Bitmap bmp = getBitmap(imageModel.getUrl());

            if (bmp != null) {
            	Log.d(TAG, "PhotosLoader::run() - Download for image: " + imageModel.getUrl().toString() + " finished successfully");
            } else {
            	
            }
            
            if (useMemoryCache) {
                // set image data in Memory Cache
                manualImageLoaderManager.addMemoryCache(imageModel.getUrl(), bmp);
            }

            // After download an image, check whether its view is already used by another URL (i.e.: the the view was detached from the
            // view). If so, do not update it.
            if (manualImageLoaderManager.imageViewReused(imageModel))
                return;

            // Finally add a task in order to display just downloaded image.
            manualImageLoaderManager.displayImage(bmp, imageModel);

        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private Bitmap getBitmap(String url) {
        File f = manualImageLoaderManager.getFileCache(url);

        if (useDiskCache) {
            //from cache
            Bitmap b = decodeFile(f);
            if (b != null)
                return b;
        }

        // Download image file from web
        try {

            Bitmap bitmap;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();

            // When using disk cache, save just downloaded image to the disk
            if (useDiskCache) {
                OutputStream os = new FileOutputStream(f);

                // Read each pixel from input stream and write them to output stream (i.e.: a file)
                Utils.CopyStream(is, os);

                os.close();
                conn.disconnect();

                // Decodes image and scales it to reduce memory consumption
                bitmap = decodeFile(f);
            } else {

            	// when disk cache is disabled, just convert the just donwloaded image (i.e. an inputStream) into a byte array.
                byte[] imageInBytes = readFully(is);
                
                // Now, convert that byte array into a bitmap 
                bitmap = BitmapFactory.decodeByteArray(imageInBytes, 0, imageInBytes.length);
            }

            return bitmap;

        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                manualImageLoaderManager.clearMemoryCache();
            return null;
        }
    }

    /**
     * Convert an InputStream in a byte array. This method is useful when no disk cache is enabled
     * Code downloaded from: http://stackoverflow.com/questions/2163644/in-java-how-can-i-convert-an-inputstream-into-a-byte-array-byte
     *
     * @param input
     * @return
     * @throws IOException
     */
    private static byte[] readFully(InputStream input) throws IOException {

        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }

    /**
     * Decodes image and scales it to reduce memory consumption
     *  
     * @param f
     * @return
     */
    private Bitmap decodeFile(File f) {

        try {

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 85;

            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //decode with current scale values
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;

        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}