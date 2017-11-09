package com.example.android.capstone_project.ui.widget;

import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.widget.RemoteViewsService;

import com.example.android.capstone_project.data.ArticleContract;
import com.example.android.capstone_project.data.ArticleDbHelper;
import com.example.android.capstone_project.data.ArticleQuery;
import com.example.android.capstone_project.data.DbUtils;

public class WidgetService extends RemoteViewsService {

    public static final String TAG = "WidgetService";
    private static Cursor cursor;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        DbUtils utils = new DbUtils(new ArticleDbHelper(this));

        int appWidgetId = Integer.valueOf(intent.getData().getSchemeSpecificPart());

        String category = intent.getStringExtra("Category").toLowerCase();
        String sortBy = intent.getStringExtra("SortBy");

        if(sortBy.equals("Top")){
            cursor = utils.getDb().query(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE
                    , ArticleQuery.PROJECTION, "Category = ?",
                    new String[] {category}, null, null, null);
            if(cursor.getCount() == 0){
                cursor = utils.getDb().query(ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE
                        , ArticleQuery.PROJECTION, "Category = ?",
                        new String[] {category}, null, null, null);
            }
        } else {
            cursor = utils.getDb().query(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE
                    , ArticleQuery.PROJECTION, "Category = ?",
                    new String[] {category}, null, null, null);
            if(cursor.getCount() == 0){
                cursor = utils.getDb().query(ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE
                        , ArticleQuery.PROJECTION, "Category = ?",
                        new String[] {category}, null, null, null);
            }
        }

        Log.d(TAG, "onGetViewFactory: " + cursor.getCount());

        return new WidgetDataProvider(this, cursor, appWidgetId);
    }
}
