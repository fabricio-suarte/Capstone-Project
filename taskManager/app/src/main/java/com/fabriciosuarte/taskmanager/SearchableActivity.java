package com.fabriciosuarte.taskmanager;

import android.app.SearchManager;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.fabriciosuarte.taskmanager.data.LocationSearchResultAdapter;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;

/**
 * The searchable activity...
 */

public class SearchableActivity extends AppCompatActivity
        implements LocationSearchResultAdapter.OnItemClickListener,
        LoaderManager.LoaderCallbacks<List<Address>> {

    //region constants

    public static final String CURRENT_LOCATION = SearchableActivity.class.getCanonicalName() + ".currentLocation";

    private static final String LOG_TAG = SearchableActivity.class.getCanonicalName();
    private static final int MAX_RESULTS = 10;
    private static final int LOCATIONS_LOADER = 1;

    //endregion

    //region attributes

    private LocationSearchResultAdapter mAdapter;

    //endregion

    //region AppCompatActivity overrides

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);

        TextView emptyView = (TextView) this.findViewById(R.id.search_result_empty_view);

        mAdapter = new LocationSearchResultAdapter();
        mAdapter.setOnItemClickListener(this);
        mAdapter.setEmptyView(emptyView);

        RecyclerView recyclerView = (RecyclerView) this.findViewById(R.id.recycler_view_search_result);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        this.handleIntent(getIntent());
    }

    //This will be called because this activity is set to "singleTop"! Take a look at the manifest.
    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        this.handleIntent(intent);
    }

    //endregion

    //region LocationSearchResultAdapter.OnItemClickListener implementation

    @Override
    public void onItemClick(View v, int position) {
        Address pickedLocation = mAdapter.getItem(position);

        Intent intent = new Intent(this, LocationActivity.class);
        intent.putExtra(LocationActivity.SEARCH_PICKED_LOCATION, pickedLocation);

        this.startActivity(intent);
        this.finish();
    }

    //endregion

    //region LoaderManager.LoaderCallbacks<String[]> implementation

    @Override
    public Loader<List<Address>> onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader<List<Address>>(this) {

            @Override
            public List<Address> loadInBackground() {

                Intent activityIntent = getIntent();

                String query = activityIntent.getStringExtra(SearchManager.QUERY);
                Bundle extra = activityIntent.getBundleExtra(SearchManager.APP_DATA);

                LatLng currentLocation = null;
                if(extra != null) {
                    currentLocation = extra.getParcelable(SearchableActivity.CURRENT_LOCATION);
                }

                Geocoder geocoder = new Geocoder(this.getContext());
                List<Address> addresses;
                try {

                    if(currentLocation != null) {
                        addresses = geocoder
                                .getFromLocationName(query,
                                        MAX_RESULTS,
                                        getLowerLeftLatitude(currentLocation),
                                        getLowerLeftLongitude(currentLocation),
                                        getUpperRightLatitude(currentLocation),
                                        getUpperRightLongitude(currentLocation)
                                );
                    }
                    else {
                        addresses = geocoder.getFromLocationName(query, MAX_RESULTS);
                    }
                }
                catch (IOException ex) {
                    //TODO: handle this exception properly! set an empty view to the recycler view use it properly to communicate the error

                    return null;
                }

                return addresses;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Address>> loader, List<Address> data) {
        mAdapter.swapData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Address>> loader) {
        mAdapter.swapData(null);
    }

    //endregion

    //region private aux methods

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            Log.d(LOG_TAG, "Received query: " + query);

            //Let's init the loader here!
            this.getSupportLoaderManager().initLoader(LOCATIONS_LOADER, null, this)
                    .forceLoad();
        }
    }
    //Remember: 1 latitude degree is approximately 111 kms...
    private double getLowerLeftLatitude(LatLng location) {

        double lowerLeft = location.latitude - 2;
        if(lowerLeft < -90)
            lowerLeft = -90;

        return lowerLeft;
    }

    private double getUpperRightLatitude(LatLng location) {

        double upperRight = location.latitude + 2;
        if(upperRight > 90)
            upperRight = 90;

        return upperRight;
    }

    private double getLowerLeftLongitude(LatLng location) {

        double lowerLeft = location.longitude - 2;
        if(lowerLeft < -180)
            lowerLeft = -180;

        return lowerLeft;
    }

    private double getUpperRightLongitude(LatLng location) {

        double upperRight = location.longitude + 2;
        if(upperRight > 180)
            upperRight = 180;

        return upperRight;
    }

    //endregion
}
