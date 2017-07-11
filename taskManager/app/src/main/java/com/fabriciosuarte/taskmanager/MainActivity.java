package com.fabriciosuarte.taskmanager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.fabriciosuarte.taskmanager.fragment.MainFragment;
import com.fabriciosuarte.taskmanager.fragment.TaskDetailFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
        implements MainFragment.Callback, TaskDetailFragment.Callback{

    //region constants

    private static final String SELECTED_TASK_STATE_KEY = "selectedTaskStateKey";

    //endregion

    //region attributes

    @BindView(R.id.detail_layout_holder)
    @Nullable
    ViewGroup mDetailLayoutHolder;

    //It is going to be used only for large layouts
    private Uri mSelectedTask;

    //endregion

    //region AppCompatActivity overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if(this.isLargeLayoutSelected()) {

            //The large layout is available! In this case, we must configure the toolbar
            Toolbar toolbar = (Toolbar) this.findViewById(R.id.main_toolbar);
            this.setSupportActionBar(toolbar);

            if(savedInstanceState != null) {
                mSelectedTask = savedInstanceState.getParcelable(SELECTED_TASK_STATE_KEY);

                if(mSelectedTask != null) {
                    this.replaceDetailFragment();
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if(this.isLargeLayoutSelected()) {
            outState.putParcelable(SELECTED_TASK_STATE_KEY, mSelectedTask);
        }

        super.onSaveInstanceState(outState);
    }

    //endregion

    //region MainFragment.Callback implementation

    @Override
    public void onTaskSelected(Uri taskUri) {

        if(this.isLargeLayoutSelected()) {

            mSelectedTask = taskUri;

            this.replaceDetailFragment();
        }
        else {
            Intent intent = new Intent(this, TaskDetailActivity.class);
            intent.setData(taskUri);

            startActivity(intent);
        }
    }

    //endregion

    //region TaskDetailFragment.Callback implementation

    @Override
    public void onTaskDeleted() {

    }

    //endregion


    //region private aux methods

    private boolean isLargeLayoutSelected() {
        return mDetailLayoutHolder != null;
    }

    private void replaceDetailFragment() {

        TaskDetailFragment fragment = TaskDetailFragment.create(mSelectedTask);

        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.detail_layout_holder,
                        fragment,
                        this.getString(R.string.fragment_task_detail_tag))
                .commit();
    }

    //endregion
}
