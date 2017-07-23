package com.fabriciosuarte.taskmanager.data;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.fabriciosuarte.taskmanager.R;
import com.fabriciosuarte.taskmanager.util.ArgumentHelper;
import com.fabriciosuarte.taskmanager.widget.DueDateWidgetProvider;

public class TaskProvider extends ContentProvider {

    //region constants

    private static final String LOG_TAG = TaskProvider.class.getCanonicalName();

    private static final int CLEANUP_JOB_ID = 51;

    private static final int TASKS = 100;
    private static final int TASKS_WITH_ID = 101;

    private static final long CLEANUP_JOB_INTERVAL_DEFAULT = 4 * 3600 * 1000; //4 hours hour
    private static final long CLEANUP_JOB_INTERVAL_LONG = 8 * 3600 * 1000; //8 hours
    private static final long CLEANUP_JOB_INTERVAL_SHORT = 3600 * 1000; //1 hour

    private static final String SINGLE_TASK_SELECTION =
            String.format("%s = ?", DatabaseContract.TaskColumns._ID);

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //endregion

    //region attributes

    private TaskDbHelper mDbHelper;
    private SQLiteQueryBuilder mQueryBuilder;

    //endregion

    //region package static blocks

    static {
        // content://com.google.developer.taskmaker/tasks
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TABLE_TASKS,
                TASKS);

        // content://com.google.developer.taskmaker/tasks/id
        sUriMatcher.addURI(DatabaseContract.CONTENT_AUTHORITY,
                DatabaseContract.TABLE_TASKS + "/#",
                TASKS_WITH_ID);
    }

    //endregion

    //region ContentProvider overrides

    @Override
    public boolean onCreate() {

        Context context = getContext();
        mDbHelper = new TaskDbHelper(context);

        mQueryBuilder = new SQLiteQueryBuilder();
        mQueryBuilder.setTables(DatabaseContract.TABLE_TASKS);

        manageCleanupJob( context);

        //Starts the first alarm for widget refresh
        DueDateWidgetProvider.setDayShiftAlarm(context);

        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null; /* Not used */
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        ArgumentHelper.validateNull(uri, "uri");
        ArgumentHelper.validateNull(projection, "projection");

        int code = sUriMatcher.match(uri);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = null;

        switch (code) {
            case TASKS:
                cursor = mQueryBuilder.query(db,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;

            case TASKS_WITH_ID:
                String taskId = uri.getLastPathSegment();
                cursor = mQueryBuilder.query(db,
                        projection,
                        SINGLE_TASK_SELECTION,
                        new String[] { taskId },
                        null,
                        null,
                        sortOrder);
                break;

            default:
                this.throwUnknownUriException(uri);
        }

        Context context = getContext();
        if(context != null) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        ArgumentHelper.validateNull(uri, "uri");
        ArgumentHelper.validateNull(values, "values");

        Uri newTaskUri = null;
        int code = sUriMatcher.match(uri);

        switch (code) {
            case TASKS:
                SQLiteDatabase db = mDbHelper.getWritableDatabase();

                long id = db.insert(DatabaseContract.TABLE_TASKS,
                            null,
                            values);

                newTaskUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI, id);

                break;

            default:
                this.throwUnknownUriException(uri);
        }

        this.notifyTasksChange();

        return newTaskUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        ArgumentHelper.validateNull(uri, "uri");
        ArgumentHelper.validateNull(values, "values");

        int code = sUriMatcher.match(uri);
        int affectedRows = -1;

        switch (code) {

            case TASKS_WITH_ID:
                SQLiteDatabase db = mDbHelper.getWritableDatabase();
                String taskId = uri.getLastPathSegment();

                affectedRows = db.update(DatabaseContract.TABLE_TASKS,
                                values,
                                SINGLE_TASK_SELECTION,
                                new String[] { taskId} );
                break;

            default:
                throwUnknownUriException(uri);
        }

        this.notifyTasksChange();

        return affectedRows;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        switch (sUriMatcher.match(uri)) {
            case TASKS:
                //Rows aren't counted with null selection
                selection = (selection == null) ? "1" : selection;
                break;
            case TASKS_WITH_ID:
                long id = ContentUris.parseId(uri);
                selection = String.format("%s = ?", DatabaseContract.TaskColumns._ID);
                selectionArgs = new String[]{String.valueOf(id)};
                break;
            default:
                //throw new IllegalArgumentException("Illegal delete URI");
                this.throwUnknownUriException(uri);
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = db.delete(DatabaseContract.TABLE_TASKS, selection, selectionArgs);

        if (count > 0) {
            //Notify observers of the change
            getContext().getContentResolver().notifyChange(uri, null);
        }

        this.notifyTasksChange();

        return count;
    }

    //endregion

    //region public methods

    /**
     * Cancels any previous jobs and schedule a new one according to what is set on preferences
     * @param context application context
     */
    public static void resetCleanupJob(Context context) {

        if(context == null)
            return;

        JobScheduler jobScheduler = (JobScheduler) context
                .getSystemService(Context.JOB_SCHEDULER_SERVICE);

        jobScheduler.cancelAll();
        manageCleanupJob(context);
    }

    //endregion

    //region private aux methods

    /* Initiate a periodic job to clear out completed items */
    private static void manageCleanupJob(Context context) {

        if(context == null)
            return;

        //Let's get the current configuration
        String key = context.getString(R.string.pref_cleanupInterval_key);
        String defaultConfValue = context.getString(R.string.pref_cleanupInterval_default);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String currentConf = sp.getString(key, defaultConfValue);

        long jobInterval;
        if(currentConf.equalsIgnoreCase(defaultConfValue)) {
            jobInterval = CLEANUP_JOB_INTERVAL_DEFAULT;
        }
        else if(currentConf.equalsIgnoreCase( context.getString(R.string.pref_cleanupInterval_short))) {
            jobInterval = CLEANUP_JOB_INTERVAL_SHORT;
        }
        else if(currentConf.equalsIgnoreCase( context.getString(R.string.pref_cleanupInterval_long))) {
            jobInterval = CLEANUP_JOB_INTERVAL_LONG;
        }
        else {
            jobInterval = CLEANUP_JOB_INTERVAL_DEFAULT;
        }

        int hours = (int) (jobInterval / 1000) / 3600;
        Log.d(LOG_TAG, "Scheduling cleanup job for " + String.valueOf(hours) + " hours...");

        JobScheduler jobScheduler = (JobScheduler) context
                .getSystemService(Context.JOB_SCHEDULER_SERVICE);

        ComponentName jobService = new ComponentName(context, CleanupJobService.class);
        JobInfo task = new JobInfo.Builder(CLEANUP_JOB_ID, jobService)
                .setPeriodic(jobInterval)
                .setPersisted(true)
                .build();

        if (jobScheduler.schedule(task) != JobScheduler.RESULT_SUCCESS) {
            Log.w(LOG_TAG, "Unable to schedule cleanup job");
        }
    }

    private void notifyTasksChange() {
        Context context = this.getContext();
        if(context != null)
            context.getContentResolver().notifyChange(DatabaseContract.CONTENT_URI, null);
    }

    private void throwUnknownUriException(Uri uri) {
        throw new UnsupportedOperationException("Unknown uri:" + uri.toString());
    }

    //endregion
}
