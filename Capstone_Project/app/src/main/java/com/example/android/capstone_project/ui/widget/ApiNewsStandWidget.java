package com.example.android.capstone_project.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.example.android.capstone_project.R;
import com.example.android.capstone_project.ui.WebViewActivity;

public class ApiNewsStandWidget extends AppWidgetProvider {

    public static final String TAG = "Widget";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.api_news_stand_widget);

        Intent intent = new Intent(context, WidgetService.class);

        String categoryValue = ApiNewsStandWidgetConfigureActivity.loadCategoryPref(context, appWidgetId);
        String sortByValue = ApiNewsStandWidgetConfigureActivity.loadSortByPref(context, appWidgetId);

        intent.putExtra("Category", categoryValue);
        intent.putExtra("SortBy", sortByValue);

        intent.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null));

        views.setRemoteAdapter(R.id.widget_listView, intent);
        views.setTextViewText(R.id.Category, categoryValue);
        views.setTextViewText(R.id.SortBy, sortByValue);

//        Intent activityIntent = new Intent(context, ApiNewsStandWidgetConfigureActivity.class);
//        activityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
//        activityIntent.setData(Uri.fromParts("content", String.valueOf(appWidgetId), null));
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
//                activityIntent, PendingIntent.FLAG_CANCEL_CURRENT);
//
//        views.setOnClickPendingIntent(R.id.configure_options, pendingIntent);

        Intent appIntent = new Intent(context, WebViewActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0,
                appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setPendingIntentTemplate(R.id.widget_listView, appPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            ApiNewsStandWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
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

