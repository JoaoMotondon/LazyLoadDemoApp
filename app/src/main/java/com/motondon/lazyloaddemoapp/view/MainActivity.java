package com.motondon.lazyloaddemoapp.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.motondon.lazyloaddemoapp.R;
import com.motondon.lazyloaddemoapp.model.ImageDownloaderEngine;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;

    // This is the navigation drawer itself.
    private NavigationView mNavigationView;

    // Used to show a animated toggle button in the toolbar.
    private ActionBarDrawerToggle mDrawerToggle;

    private Menu mDrawerMenu;
    private Toolbar mToolbar;

    private MainFragment mFragment;

    private static final int REQUEST_WRITE_STORAGE = 112;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        setupNavigationDrawer();

        mFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(MainFragment.TAG);
        if (mFragment == null) {
            mFragment = new MainFragment();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.flContent, mFragment, MainFragment.TAG).commit();

        // Set "Manual Image Downloader" as default title, since Manual mode is the default option
        setTitle("Manual Image Loader");

        // When running this app on a Marshmallow or higher, we need to ask user for additional permission.
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }
    }

    /**
     * onPostCreate is called after onStart()
     *
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();

        // Only set download engine in the onPostCreate, otherwise MainPresenterImpl object in the MainFragment will not be initialized yet.
        mFragment.setDownloadEngine(ImageDownloaderEngine.MANUAL);

        // Set small image size as default mode
        mFragment.setImageSize(false);

        // Now set the app to use both disk and memory caches by default.
        mFragment.useDiskCache(true);
        mFragment.useMemoryCache(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration changes to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.nvView);

        // By default there is not a hamburger animation icon in the toolbar. So, if we want it, we must set it up manually.
        // This is what the method below does.
        setupDrawerToggle();

        // Now add a listener to the NavigationView view, so that it can handle user actions.
        setupDrawerContent(mNavigationView);
    }

    /**
     * In order for the hamburger icon to animate to indicate the drawer is being opened and closed, we need to use
     * the ActionBarDrawerToggle class.
     *
     */
    private void setupDrawerToggle() {
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open,  R.string.drawer_close) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                // When opening the drawer, hide virtual keyboard (if it is visible)
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);

        // Add drawer toggle to the drawer layout listener.
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    /**
     * Set all the required listeners for the navigation drawer menu items.
     *
     * @param navigationView
     */
    private void setupDrawerContent(NavigationView navigationView) {

        mDrawerMenu = mNavigationView.getMenu();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });
    }

    private void selectDrawerItem(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_type_manual:
                item.setChecked(true);

                // Set action bar title
                setTitle("Manual Image Loader");
                mFragment.setDownloadEngine(ImageDownloaderEngine.MANUAL);
                break;

            case R.id.nav_type_picasso:
                item.setChecked(true);

                // Set action bar title
                setTitle("Picasso Image Loader");
                mFragment.setDownloadEngine(ImageDownloaderEngine.PICASSO);
                break;

            case R.id.nav_type_glide:
                item.setChecked(true);

                // Set action bar title
                setTitle("Glide Image Loader");
                mFragment.setDownloadEngine(ImageDownloaderEngine.GLIDE);
                break;

            case R.id.nav_type_uil:
                item.setChecked(true);

                // Set action bar title
                setTitle("UIL Image Loader");
                mFragment.setDownloadEngine(ImageDownloaderEngine.UIL);
                break;

            case R.id.nav_type_fresco:
                item.setChecked(true);

                // Set action bar title
                setTitle("Fresco Image Loader");
                mFragment.setDownloadEngine(ImageDownloaderEngine.FRESCO);
                break;

            case R.id.nav_clear_cache:
                mFragment.clearCache();
                break;

            case R.id.nav_useSmallImages:
            	mFragment.setImageSize(false);
                
            	if (!item.isChecked()) {
                    Toast.makeText(this, "Caches cleared since changed image size",
                            Toast.LENGTH_SHORT).show();
                }

                item.setChecked(true);
                mDrawerMenu.findItem(R.id.nav_useLargeImages).setChecked(false);

                break;

            case R.id.nav_useLargeImages:
            	mFragment.setImageSize(true);
                
            	if (!item.isChecked()) {
                    Toast.makeText(this, "Caches cleared since changed image size",
                            Toast.LENGTH_SHORT).show();
                }

                item.setChecked(true);
                mDrawerMenu.findItem(R.id.nav_useSmallImages).setChecked(false);

                break;

            case R.id.nav_use_memory_cache:
                item.setChecked(!item.isChecked());

                mFragment.useMemoryCache(item.isChecked());
                break;

            case R.id.nav_use_disk_cache:
                item.setChecked(!item.isChecked());

                mFragment.useDiskCache(item.isChecked());
                break;
        }

        // Close the navigation drawer
        mDrawerLayout.closeDrawers();
    }

    /**
     * In order to remove overflow menu icon (that three little dots in the right side of the toolbar), just inflate
     * an empty menu. See link below for details:
     *
     * http://stackoverflow.com/questions/9206530/how-to-disable-hide-three-dot-indicatoroption-menu-indicator-on-ics-handsets
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * When running this app on a Marshmallow or higher version, we need to request user for additional permissions. this callback
     * method is called after user accept or deny the request.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "Not allowed to write to the storage.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
