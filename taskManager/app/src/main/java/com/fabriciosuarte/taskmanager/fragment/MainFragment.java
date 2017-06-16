package com.fabriciosuarte.taskmanager.fragment;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fabriciosuarte.taskmanager.AddTaskActivity;
import com.fabriciosuarte.taskmanager.R;
import com.fabriciosuarte.taskmanager.SettingsActivity;
import com.fabriciosuarte.taskmanager.TaskDetailActivity;
import com.fabriciosuarte.taskmanager.data.DatabaseContract;
import com.fabriciosuarte.taskmanager.data.TaskAdapter;
import com.fabriciosuarte.taskmanager.data.TaskUpdateService;

/**
 * Application's main fragment
 */
public class MainFragment extends Fragment implements
        TaskAdapter.OnItemClickListener,
        View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    //region constants

    private static final int TASKS_LOADER = 1;

    //endregion

    //region attributes

    private TaskAdapter mAdapter;

    //endregion

    //region overrides on Fragment class

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_main, container, false);

        mAdapter = new TaskAdapter(null);
        mAdapter.setOnItemClickListener(this);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));

        root.findViewById(R.id.fab).setOnClickListener(this);

        //Registers this fragment as a listener for shared preferences changes
        PreferenceManager.getDefaultSharedPreferences(this.getActivity())
                .registerOnSharedPreferenceChangeListener(this);

        //Attention! Set the toolbar to the AppCompatActivity! otherwise, it is not possible
        //to inflate menu items (onCreateOptionMenu) method is not called.
        ((AppCompatActivity) this.getActivity()).setSupportActionBar( (Toolbar) root.findViewById(R.id.main_toolbar));

        this.getLoaderManager().initLoader(TASKS_LOADER, null, this);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(this.getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region View.OnClickListener implementation

    /* Click events in Floating Action Button */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this.getActivity(), AddTaskActivity.class);
        startActivity(intent);
    }

    //endregion

    //region TaskAdapter.OnItemClickListener implementation

    /* Click events in RecyclerView items */
    @Override
    public void onItemClick(View v, int position) {

        long taskId = mAdapter.getItem(position).id;
        Uri taskUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI, taskId);

        Intent intent = new Intent(this.getActivity(), TaskDetailActivity.class);
        intent.setData(taskUri);

        startActivity(intent);
    }


    /* Click events on RecyclerView item checkboxes */
    @Override
    public void onItemToggled(boolean active, int position) {

        long taskId = mAdapter.getItem(position).id;
        final Uri uri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI, taskId);
        final ContentValues values = new ContentValues();
        values.put(DatabaseContract.TaskColumns.IS_COMPLETE, active);

        TaskUpdateService.updateTask(this.getActivity(), uri, values);
    }

    //endregion

    //region LoaderManager.LoaderCallbacks<Cursor> implementation

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortKey = this.getString(R.string.pref_sortBy_key);
        String defaultSortSetting = this.getString(R.string.pref_sortBy_default);
        String dueDateSortSetting = this.getString(R.string.pref_sortBy_due);

        String currentSortSetting = PreferenceManager
                .getDefaultSharedPreferences(this.getActivity())
                .getString(sortKey, defaultSortSetting);

        String sorting;
        if(currentSortSetting.equals(dueDateSortSetting)) {
            sorting = DatabaseContract.DATE_SORT;
        }
        else {
            sorting = DatabaseContract.DEFAULT_SORT;
        }

        CursorLoader loader = new CursorLoader(this.getActivity(),
                DatabaseContract.CONTENT_URI,
                DatabaseContract.TaskColumns.getAll(),
                null,
                null,
                sorting
        );

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    //endregion

    //region OnSharedPreferenceChangeListener implementation

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        Context context = this.getContext();
        if(context == null)
            return;

        String sortKey = this.getContext().getString(R.string.pref_sortBy_key);

        if(key != null && key.equals(sortKey)) {
            getLoaderManager().restartLoader(TASKS_LOADER, null, this);
        }
    }

    //endregion

}
