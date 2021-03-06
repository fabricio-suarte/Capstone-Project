package com.fabriciosuarte.taskmanager.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;

/* Wrapper to show a managed date picker */
public class DatePickerFragment extends DialogFragment {

    DatePickerDialog.OnDateSetListener mOnDateSetListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), mOnDateSetListener, year, month, day);
    }

    public void setOnDateSetListener(DatePickerDialog.OnDateSetListener listener) {
        if(listener != null)
            this.mOnDateSetListener = listener;
    }
}
