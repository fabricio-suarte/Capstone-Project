package com.fabriciosuarte.taskmanager.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import com.fabriciosuarte.taskmanager.util.SystemHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;


import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Location fragment
 */
public class LocationFragment extends Fragment
        implements OnMapReadyCallback, OnSuccessListener<Location>,
        DialogInterface.OnCancelListener, View.OnClickListener {

    //region constants

    private static final int DEFAULT_ZOOM = 15;
    private static final String ADDRESS_STATE_KEY = "addressKey";

    private static final String LOCATION_TO_RETURN_FORMAT = "%s - %s";

    //endregion

    //region attributes

    private LocationFragment.Callback mCallbackListener;

    @BindView(R.id.fab_last_know_location)
    View mFabLastKnownLocation;

    private GoogleMap mMap;
    private Marker mLocationMarker;
    private Address mAddress;

    private FusedLocationProviderClient mFusedLocationClient;

    private boolean mMapFragmentLoaded;

    //endregion

    //region Fragment overrides

    @Override
    public void onAttach(Context context) {
        if(context == null)
            return;

        if(context instanceof LocationFragment.Callback) {
            this.mCallbackListener = (LocationFragment.Callback) context;
        }
        else {
            String message
                    = this.getString(R.string.fragment_callback_not_implemented,
                    LocationFragment.Callback.class.getName());

            throw new IllegalStateException(message);
        }

        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);

        if(savedInstanceState != null) {
            mAddress = savedInstanceState.getParcelable(ADDRESS_STATE_KEY);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_location, container, false);
        ButterKnife.bind(this, root);

        this.mFabLastKnownLocation.setOnClickListener(this);

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(ADDRESS_STATE_KEY, mAddress);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!this.mMapFragmentLoaded) {

            int servicesResult =  GoogleApiAvailability
                                    .getInstance()
                                    .isGooglePlayServicesAvailable(this.getContext());

            if(servicesResult == ConnectionResult.SUCCESS) {
                this.loadMapFragment();

                this.mMapFragmentLoaded = true;
            }
            else {

                //here I'm going to try to get the error dialog.
                //The first "int" parameter is the service result...
                //and the second would be the request code (if start for result...)
                Dialog d = GoogleApiAvailability
                            .getInstance()
                            .getErrorDialog(this.getActivity(), servicesResult, -1, this);

                d.show();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_pick_location, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_set_location) {

            this.processSetLocation();
            return true;
        }
        else if(id == R.id.action_search) {

            if(this.mLocationMarker != null) {
                mCallbackListener.onSearchRequested(mLocationMarker.getPosition());
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region View.onClick implementation

    /* Handles clicks on the Fab... */
    @Override
    public void onClick(View v) {

        this.setDefaultLocationOnMap();
    }

    //endregion

    //region OnMapReadyCallback implementation

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(mAddress != null) {
            LatLng coordinates = new LatLng(mAddress.getLatitude(), mAddress.getLongitude());
            this.updateLocationMark(coordinates);
        }
        else {
            this.setDefaultLocationOnMap();
        }
    }

    //endregion

    //region OnSuccessListener<Location> implementation

    @Override
    public void onSuccess(Location location) {
        if(location == null)
            return;

        LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
        this.updateLocationMark(coordinates);
    }

    //endregion

    //region DialogInterface.OnCancelListener implementation

    //Google Services API Error dialog
    @Override
    public void onCancel(DialogInterface dialog) {

    }

    //endregion

    //region Callback inner interface

    public interface Callback {
        void onLocationSet(String location);
        void onSearchRequested(LatLng currentPosition);
    }

    //endregion

    //region public methods

    public static LocationFragment create() {

        return new LocationFragment();
    }

    /**
     * Sets the given address as the picked location and updates the map marker
     * @param location Address instance
     */
    public void setPickedLocation(Address location) {
        if(location == null)
            return;

        mAddress = location;

        LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
        this.updateLocationMark(coordinates);

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

        mapFragment.getMapAsync(this);
    }

    private void setDefaultLocationOnMap() {

        if(ContextCompat
                .checkSelfPermission(
                        this.getActivity(),
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

    private void updateLocationMark(LatLng coordinates) {

        if(mLocationMarker != null)
            mLocationMarker.remove();

        String markerTitle = this.getString(R.string.task_location_map_marker);

        mLocationMarker = mMap.addMarker(new MarkerOptions()
                .position(coordinates)
                .title(markerTitle)
                .draggable(true)
        );

        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(DEFAULT_ZOOM), 1000, null);
    }

    private void processSetLocation() {

        Address locationAddress = null;

        if(mAddress != null) {
            locationAddress = mAddress;
        }
        else {

            //We don't have an address set... the user didn't search and picked one.
            if (SystemHelper.isConnected(this.getActivity())){

                Geocoder geocoder = new Geocoder(this.getActivity());
                LatLng markerPosition = mLocationMarker.getPosition();

                try {
                    List<Address> addresses = geocoder
                            .getFromLocation(markerPosition.latitude, markerPosition.longitude, 1);

                    locationAddress = addresses.get(0);
                }
                catch (IOException ex) {
                    //TODO: handle this properly!
                }
            }
            else {
                //TODO: handle no connection scenario
            }
        }

        if(locationAddress != null) {
            String locationToReturn
                    = String.format(LOCATION_TO_RETURN_FORMAT, locationAddress.getAddressLine(0),
                    locationAddress.getLocality());

            mCallbackListener.onLocationSet(locationToReturn);
        }
    }

    //endregion
}
