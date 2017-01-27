package com.motondon.lazyloaddemoapp.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.motondon.lazyloaddemoapp.R;
import com.motondon.lazyloaddemoapp.model.ImageDownloaderEngine;
import com.motondon.lazyloaddemoapp.model.ImageModel;
import com.motondon.lazyloaddemoapp.presenter.MainPresenter;
import com.motondon.lazyloaddemoapp.presenter.MainPresenterImpl;
import com.motondon.lazyloaddemoapp.provider.Images;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Joca on 4/12/2016.
 */
public class MainFragment extends Fragment {

    public static final String TAG = MainFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private ImageAdapter adapter;

    public MainPresenter mainPresenterImpl;

    // Used to decide which download engine will be used.
    private ImageDownloaderEngine downloadEngine;

    // Used to clear the image cache when changing from small to large image size
    private boolean useLargeImages = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	Log.d(TAG, "onCreateView");
    	
        View root = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = (RecyclerView) root.findViewById(R.id.recycler);

        // Instantiate the presenter layer
        mainPresenterImpl = new MainPresenterImpl(getContext(), this);

        onResetAdapter();
        setImageSize(false);

        return root;
    }

    public void setImageSize(boolean largeImage) {
    	Log.d(TAG, "setImageSize() - largeImage: " + largeImage);
    	
    	ArrayList<String> listImages = new ArrayList<>(Arrays.asList(largeImage ? Images.imageUrls : Images.imageThumbUrls));
        adapter.setData(listImages);
        this.useLargeImages = largeImage;
    }

    public void clearCache() {
    	Log.d(TAG, "clearCache()");
    	
        mainPresenterImpl.clearCache();
        Toast.makeText(getContext(), "Caches have been cleared", Toast.LENGTH_SHORT).show();
    }

    public void useMemoryCache(boolean memoryCache) {
    	Log.d(TAG, "useMemoryCache() - memoryCache: " + memoryCache);
    	mainPresenterImpl.setMemoryCache(memoryCache);
    }

    public void useDiskCache(boolean diskCache) {
    	Log.d(TAG, "useDiskCache() - diskCache: " + diskCache);
        mainPresenterImpl.setDiskCache(diskCache);
    }

    public void setDownloadEngine(ImageDownloaderEngine downloadEngine) {
    	Log.d(TAG, "setDownloadEngine() - downloadEngine: " + downloadEngine);
        this.downloadEngine = downloadEngine;

        mainPresenterImpl.setDownloadEngine(downloadEngine);
    }

    public void onResetAdapter() {
    	Log.d(TAG, "onResetAdapter()");
    	
        adapter = new ImageAdapter(getContext(), this);
        setImageSize(useLargeImages);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(adapter);
    }

    public void onViewDetachedFromWindow(String url) {
        mainPresenterImpl.onViewDetachedFromWindow(url);
    }

    /**
     * This method calls the presenter layer which will then call the business logic layer.
     *
     * @param imageModel
     */
    public void loadImage(ImageModel imageModel) {
    	Log.d(TAG, "loadImage() - imageModel: " + imageModel);
    	
        switch (downloadEngine) {
            case GLIDE:
                mainPresenterImpl.loadImage(imageModel);
                break;

            case FRESCO:
                mainPresenterImpl.loadImage(imageModel);
                break;

            case MANUAL:
                mainPresenterImpl.loadImage(imageModel);
                break;

            case PICASSO:
                mainPresenterImpl.loadImage(imageModel);
                break;

            case UIL:
                mainPresenterImpl.loadImage(imageModel);
                break;
        }
    }
}
