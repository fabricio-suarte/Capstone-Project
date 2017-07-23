package com.fabriciosuarte.taskmanager.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Responsible for receiving some ACTIONS like ACTION_TIME_CHANGED,
 * ACTION_TIMEZONE_CHANGED, LOCALE_CHANGED and ACTION_BOOT_COMPLETED in order to
 * properly trigger applications's broadcast REFRESH_WIDGET
 */

public class WidgetRefreshTrigger extends BroadcastReceiver {

    //region constants

    private static final String LOG_TAG = WidgetRefreshTrigger.class.getCanonicalName();

    //endregion

    //region BroadcastReceiver overrides

    @Override
    public void onReceive(Context context, Intent intent) {

        if(context == null || intent == null)
            return;

        String action = intent.getAction();
        Log.d(LOG_TAG, "Received action: " + action);

        if(action.equalsIgnoreCase(Intent.ACTION_TIME_CHANGED) ||
                action.equalsIgnoreCase(Intent.ACTION_TIMEZONE_CHANGED) ||
                action.equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED) ||
                action.equalsIgnoreCase(Intent.ACTION_LOCALE_CHANGED)) {

            //Well... due to one of this events, we should reset our widget refresh alarm
            DueDateWidgetProvider.setDayShiftAlarm(context);

            //... and triggers a refresh right now
            Intent actionIntent = new Intent(DueDateWidgetProvider.ACTION_REFRESH_WIDGET);
            context.sendBroadcast(actionIntent);
        }
    }

    //endregion


}
