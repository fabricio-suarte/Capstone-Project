package com.fabriciosuarte.taskmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.fabriciosuarte.taskmanager.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
