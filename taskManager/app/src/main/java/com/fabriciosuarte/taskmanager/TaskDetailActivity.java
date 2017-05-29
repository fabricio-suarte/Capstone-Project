package com.fabriciosuarte.taskmanager;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fabriciosuarte.taskmanager.data.DatabaseContract;
import com.fabriciosuarte.taskmanager.data.Task;
import com.fabriciosuarte.taskmanager.data.TaskUpdateService;
import com.fabriciosuarte.taskmanager.reminders.AlarmScheduler;
import com.fabriciosuarte.taskmanager.util.DateHelper;
import com.fabriciosuarte.taskmanager.view.DatePickerFragment;

import java.util.Calendar;
import java.util.Date;

public class TaskDetailActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener,
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final int TASK_LOADER = 1;
    private Uri mTaskUri;

    private TextView mDescriptionView;
    private TextView mDateView;
    private ImageView mPriorityView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        //Task must be passed to this activity as a valid provider Uri
        mTaskUri = getIntent().getData();

        mDescriptionView = (TextView) this.findViewById(R.id.detail_description);
        mDateView = (TextView) this.findViewById(R.id.detail_date);
        mPriorityView = (ImageView) this.findViewById(R.id.detail_priority);

        if(savedInstanceState == null)
            getSupportLoaderManager().initLoader(TASK_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:

                TaskUpdateService.deleteTask(this, mTaskUri);

                this.finish();

                return true;

            case R.id.action_reminder:

                DialogFragment dialog = new DatePickerFragment();
                dialog.show(this.getSupportFragmentManager(), "datePicker");

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 12, 0);

        Date alarmDate = calendar.getTime();
        Date currentDate = Calendar.getInstance().getTime();

        if(alarmDate.before(currentDate)) {
            Toast toast = Toast.makeText(this,
                    getText(R.string.past_reminder_scheduled),
                            Toast.LENGTH_LONG);

            toast.show();
        }
        else {
            AlarmScheduler.scheduleAlarm(this, alarmDate.getTime(), mTaskUri);
        }
    }

    //region LoaderManager.LoaderCallbacks<Cursor> implementation

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this,
                mTaskUri,
                DatabaseContract.TaskColumns.getAll(),
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null && data.moveToFirst()) {

            Task task = new Task(data);

            mDescriptionView.setText(task.description);

            if(task.hasDueDate()) {
                Date date = new Date(task.dueDateMillis);
                mDateView.setText(DateHelper.format(date));
            }
            else {
                mDateView.setText( getText(R.string.date_empty));
            }

            if(task.isPriority) {
                mPriorityView.setBackgroundResource(R.drawable.ic_priority);
            }
            else{
                mPriorityView.setBackgroundResource(R.drawable.ic_not_priority);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //endregion
}
