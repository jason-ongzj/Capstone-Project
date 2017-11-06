package com.example.android.capstone_project.ui.widget;

import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViewsService;

import com.example.android.capstone_project.data.ArticleDbHelper;
import com.example.android.capstone_project.data.DbUtils;

public class WidgetService extends RemoteViewsService{

    public static final String TAG = "WidgetService";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        DbUtils utils = new DbUtils(new ArticleDbHelper(this));
        Cursor cursor = utils.queryTopArticles();
        Log.d(TAG, "onGetViewFactory: " + cursor.getCount());
        return new WidgetDataProvider(this, cursor);
    }
}
