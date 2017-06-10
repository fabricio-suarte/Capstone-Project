package com.fabriciosuarte.taskmanager.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.fabriciosuarte.taskmanager.R;
import com.fabriciosuarte.taskmanager.data.TaskProvider;

/**
 * The preferences settings fragment
 */

public class SettingsFragment extends PreferenceFragmentCompat
                            implements SharedPreferences.OnSharedPreferenceChangeListener{

    //region PreferenceFragmentCompat overrides

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        this.addPreferencesFromResource(R.xml.preferences);
        this.loadSummary();
    }

    @Override
    public void onResume() {
        super.onResume();

        //Register the listener according to Google API docs
        this.getPreferenceManager()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        //Unregister the listener (following the Google API docs pattern)
        this.getPreferenceManager()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    //endregion

    //region SharedPreferences.OnSharedPreferenceChangeListener

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        String prefCleanupIntervalKey = this.getString(R.string.pref_cleanupInterval_key);

        if(key.equalsIgnoreCase(prefCleanupIntervalKey)) {

            //Let's update the cleanup job settings...
            TaskProvider.resetCleanupJob(this.getContext());
        }

        this.setSummaryForPreferenceKey(sharedPreferences, key);
    }

    //endregion

    //region private aux methods

    //Loads / sets the summary when the fragment is loaded
    private void loadSummary() {

        SharedPreferences preferences = this.getPreferenceManager()
                .getSharedPreferences();

        String key;

        key = this.getString(R.string.pref_sortBy_key);
        this.setSummaryForPreferenceKey(preferences, key);

        key = this.getString(R.string.pref_cleanupInterval_key);
        this.setSummaryForPreferenceKey(preferences, key);
    }

    private void setSummaryForPreferenceKey(SharedPreferences sharedPreferences, String key) {

        if(sharedPreferences == null || key == null)
            return;

        Preference preference = this.getPreferenceManager()
                .findPreference(key);

        if(preference == null)
            return;

        if(preference instanceof ListPreference) {

            String newValue = sharedPreferences.getString(key, "");

            //Let's update the summary for this "ListPreference" object
            ListPreference listPreference = (ListPreference) preference;

            int valueIndex = listPreference.findIndexOfValue(newValue);

            if(valueIndex >= 0)
                listPreference.setSummary(listPreference.getEntries()[valueIndex]);
        }
        else if(preference instanceof CheckBoxPreference) {
            //do nothing. But the condition must exists... otherwise it would be caught
            //by the else.
        }
        else {

            String newValue = sharedPreferences.getString(key, "");

            //For any other preferences, set the summary to value's simple string
            preference.setSummary(newValue);
        }
    }

    //endregion
}
