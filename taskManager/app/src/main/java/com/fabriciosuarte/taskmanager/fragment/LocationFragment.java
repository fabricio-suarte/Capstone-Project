package com.fabriciosuarte.taskmanager.fragment;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.fabriciosuarte.taskmanager.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Location fragment
 */
public class LocationFragment extends Fragment
        implements OnMapReadyCallback, OnSuccessListener<Location> {

    //region attributes

    @BindView(R.id.search_location)
    View mSearchLocation;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private boolean mDefaultLocationSet;

    //endregion

    //region Fragment overrides

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_location, container, false);
        ButterKnife.bind(this, root);

        this.loadMapFragment();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!this.mDefaultLocationSet) {
            this.setDefaultLocationOnMap();

            this.mDefaultLocationSet = true;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_pick_location, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_pick_location) {

            //TODO
            //get current location mark information
            //persist it
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region OnMapReadyCallback implementation

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    //endregion

    //region OnSuccessListener<Location> implementation

    @Override
    public void onSuccess(Location location) {
        if(location == null)
            return;

        String markerTitle = this.getString(R.string.task_location_map_marker);

        LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
        this.mMap.addMarker(new MarkerOptions().position(coordinates).title(markerTitle));
    }

    //endregion

    //region Callback inner interface

    public interface Callback {
        void onLocationPicked(Location location);
    }

    //endregion

    //region private aux methods

    private void loadMapFragment() {
        SupportMapFragment mapFragment = new SupportMapFragment();
        String mapFragmentTAG = this.getString(R.string.fragment_map_tag);

        this.getFragmentManager()
                .beginTransaction()
                .replace(R.id.map_container, mapFragment, mapFragmentTAG)
                .commit();
    }

    private void setDefaultLocationOnMap() {

        if(ContextCompat
                .checkSelfPermission(
                        this.getContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            this.mFusedLocationClient
                    .getLastLocation()
                    .addOnSuccessListener(this);

        }
        else {
            Toast toast = Toast.makeText(this.getContext(),
                    R.string.location_permission_not_granted,
                    Toast.LENGTH_LONG);

            toast.show();
        }
    }

    //endregion

    //region static factory methods

    public static LocationFragment create() {

        LocationFragment fragment = new LocationFragment();

        return fragment;
    }

    //endregion
}
