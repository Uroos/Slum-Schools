package com.example.myapplication;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.myapplication.model.School;

/**
 * Implementation of App Widget functionality.
 */
public class SchoolAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, School school,
                                int appWidgetId) {

        Intent intent;
        PendingIntent pendingIntent;
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.school_app_widget);
        intent = new Intent(context, MapsActivity.class);
        pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // clicking on the widget launches the pending intent
        views.setOnClickPendingIntent(R.id.widget_linear_layout, pendingIntent);
        if (school == null) {
            views.setTextViewText(R.id.widget_name, "");
            views.setTextViewText(R.id.widget_address, "");
            views.setTextViewText(R.id.widget_email, "");
            views.setTextViewText(R.id.widget_start_time, "");
            views.setTextViewText(R.id.widget_end_time, "");
            views.setTextViewText(R.id.widget_phone, "");
        } else {
            views.setTextViewText(R.id.widget_name, school.getName());
            views.setTextViewText(R.id.widget_address, school.getAddress());
            views.setTextViewText(R.id.widget_email, school.getEmail());
            views.setTextViewText(R.id.widget_start_time, school.getStart_time());
            views.setTextViewText(R.id.widget_end_time, school.getEnd_time());
            views.setTextViewText(R.id.widget_phone, school.getPhone_no());
        }
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, null, appWidgetId);
        }
    }

    public static void updateSchoolWidgets(Context context, AppWidgetManager appWidgetManager,
                                           School school, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, school, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

