package com.fabriciosuarte.taskmanager.data;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class DatabaseContract {
    //Database schema information
    static final String TABLE_TASKS = "tasks";

    public static final class TaskColumns implements BaseColumns {
        //Task description
        public static final String DESCRIPTION = "description";

        //Completed marker
        public static final String IS_COMPLETE = "is_complete";

        //Priority marker
        public static final String IS_PRIORITY = "is_priority";

        //Completion date (can be null)
        public static final String DUE_DATE = "due_date";

        //Task location (can be null)
        public static final String LOCATION = "location";

        public static String[] getAll() {
            return new String[] {
                    _ID,
                    DESCRIPTION,
                    IS_COMPLETE,
                    IS_PRIORITY,
                    DUE_DATE,
                    LOCATION
            };
        }
    }

    //Unique authority string for the content provider
    static final String CONTENT_AUTHORITY = "com.fabriciosuarte.taskmanager";

    /* Sort order constants */
    //Priority first, Completed last, the rest by date
    public static final String DEFAULT_SORT = String.format("%s ASC, %s DESC, %s ASC",
            TaskColumns.IS_COMPLETE, TaskColumns.IS_PRIORITY, TaskColumns.DUE_DATE);

    //Completed last, then by date, followed by priority
    public static final String DATE_SORT = String.format("%s ASC, %s ASC, %s DESC",
            TaskColumns.IS_COMPLETE, TaskColumns.DUE_DATE, TaskColumns.IS_PRIORITY);

    //Base content Uri for accessing the provider
    public static final Uri CONTENT_URI = new Uri.Builder().scheme("content")
            .authority(CONTENT_AUTHORITY)
            .appendPath(TABLE_TASKS)
            .build();


    /* Helpers to retrieve column values */
    public static String getColumnString(Cursor cursor, String columnName) {
        return cursor.getString( cursor.getColumnIndex(columnName) );
    }
    static int getColumnInt(Cursor cursor, String columnName) {
        return cursor.getInt( cursor.getColumnIndex(columnName) );
    }
    public static long getColumnLong(Cursor cursor, String columnName) {
        return cursor.getLong( cursor.getColumnIndex(columnName) );
    }
}
