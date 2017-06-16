package com.fabriciosuarte.taskmanager.fragment;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.fabriciosuarte.taskmanager.R;
import com.fabriciosuarte.taskmanager.data.DatabaseContract;
import com.fabriciosuarte.taskmanager.data.TaskUpdateService;
import com.fabriciosuarte.taskmanager.util.DateHelper;
import com.fabriciosuarte.taskmanager.view.DatePickerFragment;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * The Ad task fragment
 */
public class AdTaskFragment extends Fragment implements
        DatePickerDialog.OnDateSetListener,
        View.OnClickListener {

    //region constants

    private static final String DUE_DATE_KEY = "dueDateKey";

    //endregion

    //region attributes

    //Selected due date, stored as a timestamp
    private long mDueDate = Long.MAX_VALUE;

    @BindView(R.id.text_input_description)
    TextInputEditText mDescriptionView;

    @BindView(R.id.switch_priority)
    SwitchCompat mPrioritySelect;

    @BindView(R.id.text_date)
    TextView mDueDateView;

    private AdTaskFragment.Callback mFragmentListener;

    //endregion

    //region Fragment overrides

    @Override
    public void onAttach(Context context) {
        if(context == null)
            return;

        if(context instanceof AdTaskFragment.Callback) {
            this.mFragmentListener = (AdTaskFragment.Callback) context;
        }
        else {
            String message
                    = this.getString(R.string.fragment_callback_not_implemented,
                    AdTaskFragment.Callback.class.getName());

            throw new IllegalStateException(message);
        }

        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_add_task, container, false);

        ButterKnife.bind(this, root);

        if(savedInstanceState != null) {
            mDueDate = savedInstanceState.getLong(DUE_DATE_KEY);
        }

        mDueDateView.setOnClickListener(this);
        updateDateDisplay();

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putLong(DUE_DATE_KEY, mDueDate);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_task, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_save) {
            saveItem();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //endregion

    //region View.OnClickListener implementation

    /* Click events on Due Date */
    @Override
    public void onClick(View v) {
        DatePickerFragment dialogFragment = new DatePickerFragment();
        dialogFragment.setOnDateSetListener(this);

        dialogFragment.show(getFragmentManager(), "datePicker");
    }

    //endregion

    //region DatePickerDialog.OnDateSetListener implementation

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        //Set to noon on the selected day
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        this.setDateSelection(c.getTimeInMillis());
    }

    //endregion

    //region Callback inner interface

    public interface Callback {
        void onTaskSaved();
    }

    //endregion

    //region private aux methods

    private void setDateSelection(long selectedTimestamp) {
        mDueDate = selectedTimestamp;
        this.updateDateDisplay();
    }

    public long getDateSelection() {
        return mDueDate;
    }

    private void updateDateDisplay() {
        if (getDateSelection() == Long.MAX_VALUE) {
            mDueDateView.setText(R.string.date_empty);
        } else {
            CharSequence formatted = DateHelper.format(mDueDate);
            mDueDateView.setText(formatted);
        }
    }

    private void saveItem() {
        //Insert a new item
        ContentValues values = new ContentValues(4);
        values.put(DatabaseContract.TaskColumns.DESCRIPTION, mDescriptionView.getText().toString());
        values.put(DatabaseContract.TaskColumns.IS_PRIORITY, mPrioritySelect.isChecked() ? 1 : 0);
        values.put(DatabaseContract.TaskColumns.IS_COMPLETE, 0);
        values.put(DatabaseContract.TaskColumns.DUE_DATE, getDateSelection());

        TaskUpdateService.insertNewTask(this.getContext(), values);

        this.mFragmentListener.onTaskSaved();
    }

    //endregion

    //region static factory methods

    public static AdTaskFragment create() {

        return new AdTaskFragment();
    }

    //endregion
}
