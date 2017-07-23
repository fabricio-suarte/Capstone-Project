package com.fabriciosuarte.taskmanager.widget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import com.fabriciosuarte.taskmanager.MainActivity;
import com.fabriciosuarte.taskmanager.R;
import com.fabriciosuarte.taskmanager.TaskDetailActivity;
import com.fabriciosuarte.taskmanager.data.TaskUpdateService;

/**
 * An AppWidgetProvider for listing 'today due date' tasks
 */
public class DueDateWidgetProvider extends AppWidgetProvider {

    //region constants

    private static final String LOG_TAG = DueDateWidgetProvider.class.getCanonicalName();

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

        if(action.equalsIgnoreCase(TaskUpdateService.ACTION_DATA_UPDATED)) {

            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            int[] ids = manager.getAppWidgetIds(new ComponentName(context, this.getClass()));

            manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_list_view);
            Log.d(LOG_TAG, "notified ids: " + ids.toString());
        }
    }

    //endregion
}
