package com.fabriciosuarte.taskmanager;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.fabriciosuarte.taskmanager.fragment.TaskDetailFragment;

public class TaskDetailActivity extends AppCompatActivity
        implements TaskDetailFragment.Callback{

    //region Fragment overrides

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_task_detail);

        Uri taskUri = this.getIntent().getData();
        Fragment frag = TaskDetailFragment.create(taskUri, false);

        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_detail_container,
                        frag,
                        this.getString(R.string.fragment_task_detail_tag))
                .commit();
    }

    //endregion

    //region TaskDetailFragment.Callback implementation

    @Override
    public void onTaskDeleted() {
        this.finish();
    }

    //endregion
}
