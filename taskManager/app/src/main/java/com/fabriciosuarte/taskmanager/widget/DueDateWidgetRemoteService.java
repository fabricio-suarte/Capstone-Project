package com.fabriciosuarte.taskmanager.widget;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.fabriciosuarte.taskmanager.R;
import com.fabriciosuarte.taskmanager.data.DatabaseContract;
import com.fabriciosuarte.taskmanager.data.Task;

import java.util.Calendar;

/**
 * The remote view service acting as the widget collection adapter
 */

public class DueDateWidgetRemoteService extends RemoteViewsService {

    //region constants

    private static final String LOG_TAG = DueDateWidgetRemoteService.class.getCanonicalName();

    //endregion

    //region private inner classes

    private class DueDateWidgetRemoteViewFactory implements RemoteViewsFactory {

        //region constants

        /* Selection for tasks with due date <= 'today' (parameter should be set properly!)
         * and not completed! */
        private static final String SELECTION
                = DatabaseContract.TaskColumns.DUE_DATE + " <= ? AND " +
                DatabaseContract.TaskColumns.IS_COMPLETE + " = 0";

        //endregion

        //region attributes
        private Cursor mData;

        //endregion

        //region RemoteViewsFactory implementation

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            if (mData != null) {
                mData.close();
            }

            // This method is called by the app hosting the widget (e.g., the launcher)
            // However, our ContentProvider is not exported so it doesn't have access to the
            // data. Therefore we need to clear (and finally restore) the calling identity so
            // that calls use our process and permission. (This one is pretty tricky this... isn't?)
            final long identityToken = Binder.clearCallingIdentity();

            long today = this.getTodayAsMilliSeconds();

            mData = getContentResolver().query(DatabaseContract.CONTENT_URI,
                        new String[] { DatabaseContract.TaskColumns._ID ,
                                       DatabaseContract.TaskColumns.DESCRIPTION,
                                       DatabaseContract.TaskColumns.DUE_DATE}, //projection
                        SELECTION,
                        new String[] { String.valueOf(today)}, //args
                        DatabaseContract.DATE_SORT);

            Log.d(LOG_TAG, "onDataSetChanged executed. Returned tasks: "
                    + (mData == null ? "0" : String.valueOf(mData.getCount())) );

            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            if(mData != null) {
                mData.close();
                mData = null;
            }
        }

        @Override
        public int getCount() {
            if(mData == null)
                return 0;

            return mData.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if(position == RecyclerView.NO_POSITION ||
                    (mData != null && !mData.moveToPosition(position)) ) {
                return null;
            }

            //Let's get the selected columns values
            String taskDescription = DatabaseContract
                    .getColumnString(mData, DatabaseContract.TaskColumns.DESCRIPTION );

            long  taskDueDate = DatabaseContract
                    .getColumnLong(mData, DatabaseContract.TaskColumns.DUE_DATE);

            long taskId = DatabaseContract.getColumnLong(mData, DatabaseContract.TaskColumns._ID);


            //Finally, set the remove view
            RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_list_item);

            remoteViews.setTextViewText( R.id.widget_item_task_description, taskDescription);

            if(taskDueDate < this.getTodayAsMilliSeconds()) {
                remoteViews.setTextViewCompoundDrawables(R.id.widget_item_task_description,
                        R.drawable.ic_task_expired, 0, 0, 0);
            }
            else {
                remoteViews.setTextViewCompoundDrawables(R.id.widget_item_task_description,
                        R.drawable.ic_task_about_to_expire, 0, 0, 0);
            }

            Uri taskUri = ContentUris.withAppendedId(DatabaseContract.CONTENT_URI, taskId);
            Intent taskDetailIntent = new Intent();
            taskDetailIntent.setData(taskUri);

            remoteViews.setOnClickFillInIntent( R.id.widget_list_item,  taskDetailIntent);

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.widget_list_item);
        }

        @Override
        public int getViewTypeCount() {

            //we just have one type of view
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if(mData == null || !mData.moveToPosition(position))
                return -1;

            return DatabaseContract.getColumnLong(mData, DatabaseContract.TaskColumns._ID);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        //endregion

        //region private aux methods

        private long getTodayAsMilliSeconds() {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return Task.getDueDateValue(year, month, day);
        }

        //endregion
    }

    //endregion

    //region RemoveViewsService overrides

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        Log.d(LOG_TAG, "onGetViewFactory...");

        return new DueDateWidgetRemoteViewFactory();
    }

    //endregion
}
