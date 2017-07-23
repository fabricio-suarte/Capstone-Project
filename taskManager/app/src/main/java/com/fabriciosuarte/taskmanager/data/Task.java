package com.fabriciosuarte.taskmanager.data;

import android.database.Cursor;
import android.text.TextUtils;

import java.util.Calendar;

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

    /**
     * Returns a due date value in milliseconds, for the given parameters and hour set for non.
     * @param year Due date year
     * @param month Due date monty
     * @param day due date day
     * @return long, representing the due date in milliseconds
     */
    public static long getDueDateValue(int year, int month, int day) {
        //Set to noon on the selected day
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c.getTimeInMillis();
    }
}
