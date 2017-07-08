package com.fabriciosuarte.taskmanager.data;

import android.location.Address;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fabriciosuarte.taskmanager.R;

import org.w3c.dom.Text;

import java.util.List;

/**
 * The Adapter for Location Search result list
 */

public class LocationSearchResultAdapter extends RecyclerView.Adapter<LocationSearchResultAdapter.ResultHolder> {

    //region attributes

    private List<Address> mData;
    private TextView mEmptyView;
    private LocationSearchResultAdapter.OnItemClickListener mOnItemClickListener;

    //endregion

    //region inner interfaces and classes definitions

    /* Callback for list item click events */
    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    class ResultHolder extends  RecyclerView.ViewHolder implements View.OnClickListener {

        TextView mLocation;

        ResultHolder(View itemView) {
            super(itemView);

            mLocation = (TextView) itemView.findViewById(R.id.search_result_location);
            mLocation.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            postItemClick(this);
        }
    }

    //endregion

    //region overrides on RecyclerView.Adapter<LocationSearchResultAdapter.ResultHolder>

    @Override
    public ResultHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_location_search_result, parent, false);

        return new ResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ResultHolder holder, int position) {

        if(mData != null && mData.size() > position) {

            Address address = mData.get(position);
            String locationText = this.getLocationText(address);

            holder.mLocation.setText(locationText);
        }
    }

    @Override
    public int getItemCount() {
        if(mData == null)
            return 0;

        return mData.size();
    }

    //endregion

    //region public methods

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void swapData(List<Address> data) {
        mData = data;

        if(mData != null && mData.size() > 0) {
            mEmptyView.setVisibility(View.GONE);
        }
        else {
            mEmptyView.setVisibility(View.VISIBLE);
        }

        notifyDataSetChanged();
    }

    /**
     * Retrieve an "Address" object for the given position.
     *
     * @param position Adapter item position.
     *
     * @return A {@link Address} representing a location result from the location search
     */
    public Address getItem(int position) {
        if (mData == null || mData.size() <= position) {
            throw new IllegalStateException("Invalid item position requested");
        }

        return mData.get(position);
    }

    public void setEmptyView(TextView emptyView) {
        mEmptyView = emptyView;
    }

//    /***
//     * Retrieves the formatted location text, as it is shown in the list
//     * @param position Adapter item position.
//     * @return A String for the location
//     */
//    public String getFormatedLocation(int position) {
//        Address address =  this.getItem(position);
//
//        return this.getLocationText(address);
//    }

    //endregion

    //region private aux methods

    private void postItemClick(ResultHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
        }
    }

    private String getLocationText(Address address) {
        String location = address.getAddressLine(0) + " - " + address.getLocality();

        return location;
    }

    //endregion
}
