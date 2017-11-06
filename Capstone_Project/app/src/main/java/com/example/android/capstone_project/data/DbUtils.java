package com.example.android.capstone_project.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android.capstone_project.http.apimodel.Article;

import java.util.ArrayList;

public class DbUtils {

    private ArticleDbHelper helper;
    private SQLiteDatabase db;

    public DbUtils(ArticleDbHelper helper){
        this.helper = helper;
        this.db = helper.getReadableDatabase();
    }

    public SQLiteDatabase getDb(){
        return db;
    }

    public ArticleDbHelper getHelper() { return helper; }

    public void insertIntoDb(ArrayList<Article> array, String sortBy){
        String table = "";
        switch(sortBy){
            case "top":
                table = ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE;
                helper.deleteRecordsFromTopTable(db);
                break;
            case "latest":
                table = ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE;
                helper.deleteRecordsFromLatestTable(db);
                break;
        }
        db.beginTransaction();
        for(int i = 0; i < array.size(); i++){
            ContentValues cv = new ContentValues();
            Article article = array.get(i);
            if(article != null) {
                cv.put(ArticleContract.ArticleEntry._ID, i);
                cv.put(ArticleContract.ArticleEntry.COLUMN_AUTHOR, article.getAuthor());
                cv.put(ArticleContract.ArticleEntry.COLUMN_TITLE, article.getTitle());
                cv.put(ArticleContract.ArticleEntry.COLUMN_DESCRIPTION, article.getDescription());
                cv.put(ArticleContract.ArticleEntry.COLUMN_URL_TO_IMAGE, article.getUrlToImage());
                cv.put(ArticleContract.ArticleEntry.COLUMN_URL, article.getUrl());
                cv.put(ArticleContract.ArticleEntry.COLUMN_PUBLISHED_AT, article.getPublishedAt());
                cv.put(ArticleContract.ArticleEntry.COLUMN_CATEGORY, article.getCategory());
                cv.put(ArticleContract.ArticleEntry.COLUMN_SOURCE, article.getSource());
                db.insert(table, null, cv);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public Cursor querySources(){
        // Get unique entries of source names using UNION statement
        String sql = "SELECT " + ArticleContract.ArticleEntry.COLUMN_SOURCE + ", "
                + ArticleContract.ArticleEntry.COLUMN_CATEGORY + " FROM "
                + ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE + " GROUP BY "
                + ArticleContract.ArticleEntry.COLUMN_SOURCE + " UNION "
                + "SELECT " + ArticleContract.ArticleEntry.COLUMN_SOURCE + ", "
                + ArticleContract.ArticleEntry.COLUMN_CATEGORY + " FROM "
                + ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE + " GROUP BY "
                + ArticleContract.ArticleEntry.COLUMN_SOURCE;
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }

    public Cursor queryCombinedArticleLists(String query){
        // Get combined entries from top and latest articles tables
        String output = ArticleContract.ArticleEntry.COLUMN_TITLE + ", "
                + ArticleContract.ArticleEntry.COLUMN_DESCRIPTION + ", "
                + ArticleContract.ArticleEntry.COLUMN_URL_TO_IMAGE + ", "
                + ArticleContract.ArticleEntry.COLUMN_URL;

        String whereClause = " WHERE " + ArticleContract.ArticleEntry.COLUMN_DESCRIPTION + " LIKE \"%"
                + query + "%\" OR " + ArticleContract.ArticleEntry.COLUMN_TITLE + " LIKE \"%"
                + query + "%\"";

        String sql = "SELECT " + output + " FROM " + ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE
                + whereClause + " UNION "
                + "SELECT " + output + " FROM " + ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE
                + whereClause;
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }

    public Cursor queryTopArticles(){
        return db.query(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE, ArticleQuery.PROJECTION,
                null, null, null, null, null);
    }
}
