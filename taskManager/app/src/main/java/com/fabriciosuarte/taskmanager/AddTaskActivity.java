package com.fabriciosuarte.taskmanager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.fabriciosuarte.taskmanager.fragment.AddTaskFragment;

public class AddTaskActivity extends AppCompatActivity implements AddTaskFragment.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        if(savedInstanceState != null)
            return;

        Fragment frag = AddTaskFragment.create();

        this.getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_add_task_container,
                        frag,
                        this.getString(R.string.fragment_add_task_tag))
                .commit();
    }

    //endregion

    //region AddTaskFragment.Callback implementation

    @Override
    public void onTaskSaved() {
        this.finish();
    }

    //endregion
}
