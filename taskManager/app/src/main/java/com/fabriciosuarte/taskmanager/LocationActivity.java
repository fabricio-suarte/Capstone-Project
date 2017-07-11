package com.fabriciosuarte.taskmanager;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fabriciosuarte.taskmanager.fragment.LocationFragment;
import com.google.android.gms.maps.model.LatLng;

/**
 * Location pick activity
 */
public class LocationActivity extends AppCompatActivity implements LocationFragment.Callback {

    //region constants

    private static final String LOG_TAG = LocationActivity.class.getCanonicalName();
    private static final int ACCESS_COARSE_LOCATION_REQUEST_CODE = 1000;

    /**
     * Key used to receive the result (picked Address) from a location search
     */
    public static final String SEARCH_PICKED_LOCATION = LocationActivity.class.getCanonicalName() + ".pickedLocation";

    /**
     * Key used to return the final location string in the result intent
     */
    public static final String LOCATION_SET = LocationActivity.class.getCanonicalName() + ".locationSet";

    //endregion

    //region attributes

    private boolean mLocationPermissionRequested;
    private LatLng mCurrentLocation;

    //endregion

    //region AppCompatActivity overrides

    @Override
    protected void onResume() {
        super.onResume();

        if(mLocationPermissionRequested)
            return;

       if( ContextCompat
               .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

           //Let's ask for the location permission
           String[] permissions = new String[] { Manifest.permission.ACCESS_COARSE_LOCATION};
           ActivityCompat.requestPermissions(this, permissions, ACCESS_COARSE_LOCATION_REQUEST_CODE);

           this.mLocationPermissionRequested = true;
       }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == ACCESS_COARSE_LOCATION_REQUEST_CODE) {

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                LocationFragment frag = (LocationFragment) this.getSupportFragmentManager()
                        .findFragmentByTag(  this.getString(R.string.fragment_location_tag));

                frag.setDefaultLocation();
            }
            else{
                this.finish();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_location);

        if(savedInstanceState != null)
            return;

        Fragment frag = LocationFragment.create();

        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_location_container,
                        frag,
                        this.getString(R.string.fragment_location_tag))
                .commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.setIntent(intent);
        Address pickedLocation = intent.getParcelableExtra(SEARCH_PICKED_LOCATION);

        Log.d(LOG_TAG, pickedLocation.toString());

        String locationFragmentTAG = this.getString(R.string.fragment_location_tag);
        LocationFragment frag = (LocationFragment) this.getSupportFragmentManager()
                .findFragmentByTag(locationFragmentTAG);

        frag.setPickedLocation(pickedLocation);
    }

    @Override
    public boolean onSearchRequested() {

        //I must override this method, so I can pass the current location for the searchable activity!
        Bundle searchData = new Bundle();
        searchData.putParcelable(SearchableActivity.CURRENT_LOCATION, mCurrentLocation);

        this.startSearch(null, false, searchData, false);

        return super.onSearchRequested();
    }

    //endregion

    //region LocationFragment.Callback implementation

    @Override
    public void onLocationSet(String location) {
        Intent data = new Intent();
        data.putExtra(LOCATION_SET, location);

        this.setResult(Activity.RESULT_OK, data);
        this.finish();
    }

    @Override
    public void onSearchRequested(LatLng currentLocation) {

        mCurrentLocation = currentLocation;

        //Calls the activity onSearchRequest() method!
        this.onSearchRequested();
    }

    //endregion
}
