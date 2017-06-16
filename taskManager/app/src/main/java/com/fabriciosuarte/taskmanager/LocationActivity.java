package com.fabriciosuarte.taskmanager;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.fabriciosuarte.taskmanager.fragment.LocationFragment;

/**
 * Location pick activity
 */
public class LocationActivity extends AppCompatActivity implements LocationFragment.Callback {

    //region AppCompatActivity overrides

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

    //endregion

    //region LocationFragment.Callback implementation

    @Override
    public void onLocationPicked(Location location) {

    }

    //endregion
}
