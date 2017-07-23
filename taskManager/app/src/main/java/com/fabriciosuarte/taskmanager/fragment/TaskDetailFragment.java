package com.fabriciosuarte.taskmanager.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fabriciosuarte.taskmanager.R;
import com.fabriciosuarte.taskmanager.data.DatabaseContract;
import com.fabriciosuarte.taskmanager.data.Task;
import com.fabriciosuarte.taskmanager.data.TaskUpdateService;
import com.fabriciosuarte.taskmanager.reminders.AlarmScheduler;
import com.fabriciosuarte.taskmanager.util.ArgumentHelper;
import com.fabriciosuarte.taskmanager.util.DateHelper;
import com.fabriciosuarte.taskmanager.view.DatePickerFragment;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Task detail fragment
 */

public class TaskDetailFragment extends Fragment implements
        DatePickerDialog.OnDateSetListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    //region constants

    private static final int TASK_LOADER = 1;
    private static final String TASK_URI_ARG = TaskDetailFragment.class.getName() + ".taskUriArg";
    private static final String TWO_PANEL_LAYOUT_ARG = TaskDetailFragment.class.getName() + "twoPanelLayoutArg";

    //endregion

    //region attributes

    private Uri mTaskUri;
    private boolean mTwoPanelLayout;
    private TaskDetailFragment.Callback mFragmentListener;

    @BindView(R.id.detail_description)
    TextView mDescriptionView;

    @BindView(R.id.detail_date)
    TextView mDateView;

    @BindView(R.id.detail_location)
    TextView mLocationView;

    @BindView(R.id.detail_priority)
    ImageView mPriorityView;

    @BindView(R.id.detail_empty)
    View mEmptyView;


    //endregion

    //region Fragment overrides

    @Override
    public void onAttach(Context context) {

        if(context == null)
            return;

        if(context instanceof TaskDetailFragment.Callback) {
            this.mFragmentListener = (TaskDetailFragment.Callback) context;
        }
        else {

            String message
                    = this.getString(R.string.fragment_callback_not_implemented,
                    TaskDetailFragment.Callback.class.getName());

            throw new IllegalStateException(message);
        }

        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Task must be passed to this fragment as a valid provider Uri
        Bundle args = this.getArguments();
        if(args != null) {
            mTaskUri = (Uri) args.get(TASK_URI_ARG);
            mTwoPanelLayout = args.getBoolean(TWO_PANEL_LAYOUT_ARG);
        }

        if(mTaskUri == null && !mTwoPanelLayout) {
            throw new IllegalStateException("Task must be passed to this fragment as a valid provider Uri!");
        }

        if(mTaskUri != null) {
            this.setHasOptionsMenu(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task_detail, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:

                TaskUpdateService.deleteTask(this.getContext(), mTaskUri);

                this.mFragmentListener.onTaskDeleted();

                return true;

            case R.id.action_reminder:

                DatePickerFragment dialog = new DatePickerFragment();
                dialog.setOnDateSetListener(this);

                dialog.show(this.getFragmentManager(), "datePicker");

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_task_detail, container, false);

        ButterKnife.bind(this, root);

        if( this.showEmptyView()) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
        else {
            if(savedInstanceState != null) {
                getLoaderManager().restartLoader(TASK_LOADER, null, this);
            }
            else {
                getLoaderManager().initLoader(TASK_LOADER, null, this);
            }
        }

        return root;
    }

    //endregion

    //region DatePickerDialog.OnDateSetListener implementation

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, 12, 0);

        Date alarmDate = calendar.getTime();
        Date currentDate = Calendar.getInstance().getTime();

        if(alarmDate.before(currentDate)) {
            Toast toast = Toast.makeText(this.getContext(),
                    getText(R.string.past_reminder_scheduled),
                    Toast.LENGTH_LONG);

            toast.show();
        }
        else {
            AlarmScheduler.scheduleAlarm(this.getContext(), alarmDate.getTime(), mTaskUri);
        }
    }

    //endregion

    //region LoaderManager.LoaderCallbacks<Cursor> implementation

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this.getContext(),
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
                mDateView.setText( getText(R.string.detail_date_empty));
            }

            if(task.hasLocation()) {
                mLocationView.setText( task.location);
            }
            else {
                mLocationView.setText( getText(R.string.detail_location_empty));
            }

            if(task.isPriority) {
                mPriorityView.setBackgroundResource(R.drawable.ic_priority);
            }
            else{
                mPriorityView.setBackgroundResource(R.drawable.ic_not_priority);
            }

            this.getActivity().supportPostponeEnterTransition();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //endregion

    //region Callback inner interface
    public interface Callback {
        void onTaskDeleted();
    }

    //endregion

    //region static factory methods

    /**
     * Creates a new instance of this fragment
     * @param taskUri the task uri
     * @param twoPaneLayout when set to "true", it will show an "empty message"
     *        instead of launching an exception if no taskUri is given
     * @return a new TaskDetailFragment instance
     */
    public static TaskDetailFragment create(Uri taskUri, boolean twoPaneLayout) {

        Bundle args = new Bundle();
        args.putParcelable(TASK_URI_ARG, taskUri);
        args.putBoolean(TWO_PANEL_LAYOUT_ARG, twoPaneLayout);

        TaskDetailFragment fragment = new TaskDetailFragment();
        fragment.setArguments(args);

        return fragment;
    }

    //endregion

    //region private aux methods

    private boolean showEmptyView() {
        return this.mTaskUri == null && mTwoPanelLayout;
    }

    //endregion
}
