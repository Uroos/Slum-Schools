package com.example.myapplication;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.myapplication.model.School;

public class SchoolUpdateService extends IntentService {

    private static final String ACTION_SCHOOL_UPDATE = "com.example.mainapplication.action.school_update";

    public SchoolUpdateService() {
        super("SchoolUpdateService");
    }

    public static void startSchoolUpdate(Context context, School school) {
        Intent intent = new Intent(context, SchoolUpdateService.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("school", school);
        intent.setAction(ACTION_SCHOOL_UPDATE);
        intent.putExtras(bundle);
        context.startService(intent);
    }
    @Override
    protected void onHandleIntent( Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_SCHOOL_UPDATE.equals(action)) {
                School school = intent.getParcelableExtra("school");
                //Timber.v("ingredients sent to service are: " + ingredients);
                handleActionSchoolUpdate(school);
            }
        }
    }
    private void handleActionSchoolUpdate(School school) {
        // here we will run data base query to get the favorite recipe and display in widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, SchoolAppWidget.class));
        //appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.recipe_widget_text);
        // Now update the widgets
        SchoolAppWidget.updateSchoolWidgets(this, appWidgetManager, school, appWidgetIds);
    }
}
