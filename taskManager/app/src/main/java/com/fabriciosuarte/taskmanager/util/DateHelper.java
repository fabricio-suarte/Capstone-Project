package com.fabriciosuarte.taskmanager.util;

import android.text.format.DateFormat;
import java.util.Date;

/**
 * Created by suarte on 01/04/17.
 *
 */

public final class DateHelper {

    private static final String FORMAT = "MM/dd/yyyy";

    public static CharSequence format(Date date) {
        CharSequence cs = DateFormat.format(FORMAT, date);
        return cs;
    }

    public static CharSequence format(long dateTime){
        CharSequence cs = DateFormat.format(FORMAT, dateTime);
        return cs;
    }
}
