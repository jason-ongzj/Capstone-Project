package com.example.android.capstone_project.ui.widget;

import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViewsService;

import com.example.android.capstone_project.R;
import com.example.android.capstone_project.data.ArticleContract;
import com.example.android.capstone_project.data.ArticleDbHelper;
import com.example.android.capstone_project.data.ArticleQuery;
import com.example.android.capstone_project.data.DbUtils;

public class WidgetService extends RemoteViewsService {

    public static final String TAG = "WidgetService";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Cursor cursor;
        DbUtils utils = new DbUtils(new ArticleDbHelper(this));

        String category = intent.getStringExtra(getString(R.string.category)).toLowerCase();
        String sortBy = intent.getStringExtra(getString(R.string.sortBy));

        if(sortBy.equals(getString(R.string.top))){
            cursor = utils.getDb().query(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE
                    , ArticleQuery.PROJECTION, getString(R.string.category_query),
                    new String[] {category}, null, null, null);
            if(cursor.getCount() == 0){
                cursor = utils.getDb().query(ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE
                        , ArticleQuery.PROJECTION, getString(R.string.category_query),
                        new String[] {category}, null, null, null);
            }
        } else {
            cursor = utils.getDb().query(ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE
                    , ArticleQuery.PROJECTION, getString(R.string.category_query),
                    new String[] {category}, null, null, null);
            if(cursor.getCount() == 0){
                cursor = utils.getDb().query(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE
                        , ArticleQuery.PROJECTION, getString(R.string.category_query),
                        new String[] {category}, null, null, null);
            }
        }

        return new WidgetDataProvider(this, cursor);
    }
}
