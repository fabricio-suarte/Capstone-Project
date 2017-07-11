package com.fabriciosuarte.taskmanager.data;

import android.database.Cursor;
import android.text.TextUtils;

import static com.fabriciosuarte.taskmanager.data.DatabaseContract.*;

/**
 * Helpful data model for holding attributes related to a task.
 */
public class Task {

    //Unique identifier in database
    public long id;

    //Task description
    public final String description;

    //Marked if task is done
    public final boolean isComplete;

    //Marked if task is priority
    public final boolean isPriority;

    //Optional due date for the task
    public final long dueDateMillis;

    //Optional location for the task
    public final String location;

    /**
     * Create a new task from a database Cursor
     */
    public Task(Cursor cursor) {
        this.id = getColumnLong(cursor, TaskColumns._ID);
        this.description = getColumnString(cursor, TaskColumns.DESCRIPTION);
        this.isComplete = getColumnInt(cursor, TaskColumns.IS_COMPLETE) == 1;
        this.isPriority = getColumnInt(cursor, TaskColumns.IS_PRIORITY) == 1;
        this.dueDateMillis = getColumnLong(cursor, TaskColumns.DUE_DATE);
        this.location = getColumnString(cursor, TaskColumns.LOCATION);
    }

    /**
     * Return true if a due date was set to this task.
     */
    public boolean hasDueDate() {
        return this.dueDateMillis != Long.MAX_VALUE;
    }

    /**
     * Returns true if a location was set to this task.
     */
    public boolean hasLocation() {
        return !TextUtils.isEmpty(this.location);
    }

}
