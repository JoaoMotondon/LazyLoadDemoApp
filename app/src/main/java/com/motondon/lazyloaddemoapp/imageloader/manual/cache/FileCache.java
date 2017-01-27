package com.motondon.lazyloaddemoapp.imageloader.manual.cache;

import android.content.Context;

import java.io.File;

/**
 * Code downloaded from https://github.com/thest1/LazyList
 *
 */
public class FileCache {

    private File cacheDir;

    public FileCache(Context context){

        //Find the dir at SDCARD to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "LazyList");
        } else {
            // if checking on simulator the create cache dir in your application context
            cacheDir = context.getCacheDir();
        }

        if (!cacheDir.exists()) {
            // create cache dir in your application context
            // NOTE: If you run this app on Marshmallow or higher and you have an external storage (i.e.: an SD card), you must have
            // first granted access to write on the external drive. Otherwise this is not going to work
            cacheDir.mkdirs();
        }
    }

    public File getFile(String url){
        //Identify images by hashcode or encode by URLEncoder.encode.
        String filename=String.valueOf(url.hashCode());

        File f = new File(cacheDir, filename);
        return f;
    }

    /**
     * Delete all files in the cache directory
     */
    public void clear(){
    	File[] files=cacheDir.listFiles();
        if(files==null)
            return;
        for(File f:files)
            f.delete();
    }
}