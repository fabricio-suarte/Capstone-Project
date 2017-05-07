package com.fabriciosuarte.taskmanager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.fabriciosuarte.taskmanager.data.DatabaseContract;
import com.fabriciosuarte.taskmanager.data.TaskAdapter;
import com.fabriciosuarte.taskmanager.data.TaskUpdateService;

public class MainActivity extends AppCompatActivity implements
        TaskAdapter.OnItemClickListener,
        View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private TaskAdapter mAdapter;
    private int TASKS_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAdapter = new TaskAdapter(null);
        mAdapter.setOnItemClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.fab).setOnClickListener(this);

        //Registers this activity as a listener for shared preferences changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        this.getSupportLoaderManager().initLoader(TASKS_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Click events in Floating Action Button */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        startActivity(intent);
    }

    /* Click events in RecyclerView items */
    @Override
    public void onItemClick(View v, int position) {
        //TODO: Handle list item click event

        long taskId = mAdapter.getItem(position).id;
        Uri taskUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI, taskId);

        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.setData(taskUri);

        startActivity(intent);
    }

    /* Click events on RecyclerView item checkboxes */
    @Override
    public void onItemToggled(boolean active, int position) {
        //TODO: Handle task item checkbox event

        long taskId = mAdapter.getItem(position).id;
        final Uri uri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI, taskId);
        final ContentValues values = new ContentValues();
        values.put(DatabaseContract.TaskColumns.IS_COMPLETE, active);

        TaskUpdateService.updateTask(this, uri, values);
    }

    //region LoaderCallbacks
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortKey = this.getString(R.string.pref_sortBy_key);
        String defaultSortSetting = this.getString(R.string.pref_sortBy_default);
        String dueDateSortSetting = this.getString(R.string.pref_sortBy_due);

        String currentSortSetting = PreferenceManager
                                .getDefaultSharedPreferences(this)
                                .getString(sortKey, defaultSortSetting);

        String sorting;
        if(currentSortSetting.equals(dueDateSortSetting)) {
            sorting = DatabaseContract.DATE_SORT;
        }
        else {
            sorting = DatabaseContract.DEFAULT_SORT;
        }

        CursorLoader loader = new CursorLoader(this,
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

        String sortKey = this.getString(R.string.pref_sortBy_key);

        if(key != null && key.equals(sortKey)) {
            getSupportLoaderManager().restartLoader(TASKS_LOADER, null, this);
        }
    }

    //endregion
}
