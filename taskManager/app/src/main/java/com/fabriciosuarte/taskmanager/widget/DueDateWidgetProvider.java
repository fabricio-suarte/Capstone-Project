package com.fabriciosuarte.taskmanager.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;

import com.fabriciosuarte.taskmanager.MainActivity;
import com.fabriciosuarte.taskmanager.R;
import com.fabriciosuarte.taskmanager.TaskDetailActivity;
import com.fabriciosuarte.taskmanager.data.TaskUpdateService;

import java.util.Calendar;

/**
 * An AppWidgetProvider for listing 'today due date' tasks
 */
public class DueDateWidgetProvider extends AppWidgetProvider {

    //region constants

    private static final String LOG_TAG = DueDateWidgetProvider.class.getCanonicalName();
    private static final int ACTION_REFRESH_WIDGET_REQUEST_CODE = 1033;

    /**
     * Broadcast Action that should be sent to the application Widget in order to refresh its content.
     * Usually, it is going to be sent when a time event occurs, like a day shift, for instance. Some
     * tasks might be eligible for the widget after such events.
     */
    public static final String ACTION_REFRESH_WIDGET
            = DueDateWidgetProvider.class.getPackage().getName() + ".ACTION_REFRESH_WIDGET";

    //endregion

    //region AppWidgetProvider overrides

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        if(context == null || appWidgetManager == null || appWidgetIds == null)
            return;

        RemoteViews remoteViews;
        Intent intent;

        for(int widgetId : appWidgetIds) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_due_date);

            // Create an Intent to launch MainActivity
            intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            //Set the intent for the widget layout "touch"
            remoteViews.setOnClickPendingIntent(R.id.widget_due_date, pendingIntent);

            //Set the collection / remote adapter
            remoteViews.setRemoteAdapter(R.id.widget_list_view,
                    new Intent(context, DueDateWidgetRemoteService.class));

            //Create the Pending intent to pe used as template for list items clicks...
            boolean useDetailActivity = context.getResources().getBoolean(R.bool.uses_detail_activity);

            Intent listItemIntent;
            if(useDetailActivity)
                listItemIntent = new Intent(context, TaskDetailActivity.class);
            else
                listItemIntent = new Intent(context, MainActivity.class);

            PendingIntent pendingTemplate
                    = TaskStackBuilder.create(context).addNextIntentWithParentStack(listItemIntent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setPendingIntentTemplate(R.id.widget_list_view, pendingTemplate);

            //Set the empty view
            remoteViews.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view);

            //and finally... tell the manager to update the widget
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        String action = intent.getAction();

        Log.d(LOG_TAG, "Received action: " + action);

        if(action.equalsIgnoreCase(TaskUpdateService.ACTION_DATA_UPDATED) ||
            action.equalsIgnoreCase(ACTION_REFRESH_WIDGET)) {

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, this.getClass()));

            manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list_view);
        }
    }

    //endregion

    //region public methods

    /**
     * Set an alarm for the first hour of the next day. Cancels any previously set alarm.
     * The intent of this alarm is for refreshing any possible application widget
     * @param context Android's context
     */
    public static void setDayShiftAlarm(@NonNull Context context) {

        Intent intent = new Intent(ACTION_REFRESH_WIDGET);
        PendingIntent pIntent  = PendingIntent
                .getBroadcast(context,
                        ACTION_REFRESH_WIDGET_REQUEST_CODE,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //Cancels any possible previously set alarm
        am.cancel(pIntent);

        //Set the new alarm for the first hour of the next day
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long dayInMilli = calendar.getTimeInMillis();
        am.setExact(AlarmManager.RTC, dayInMilli, pIntent);
    }

    //endregion
}
