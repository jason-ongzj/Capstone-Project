package com.example.android.capstone_project.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ArticleDbHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "articles.db";

    private static final int DATABASE_VERSION = 1;

    public ArticleDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_TOP_ARTICLES_TABLE =

                "CREATE TABLE " + ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE + " (" +
                        ArticleContract.ArticleEntry._ID + " INT NOT NULL, " +
                        ArticleContract.ArticleEntry.COLUMN_AUTHOR + " TEXT, " +
                        ArticleContract.ArticleEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        ArticleContract.ArticleEntry.COLUMN_DESCRIPTION + " TEXT, " +
                        ArticleContract.ArticleEntry.COLUMN_URL_TO_IMAGE + " TEXT, " +
                        ArticleContract.ArticleEntry.COLUMN_URL + " TEXT NOT NULL, " +
                        ArticleContract.ArticleEntry.COLUMN_PUBLISHED_AT + " TEXT, " +
                        ArticleContract.ArticleEntry.COLUMN_CATEGORY + " TEXT, " +
                        ArticleContract.ArticleEntry.COLUMN_SOURCE+ " TEXT);";

        final String SQL_CREATE_LATEST_ARTICLES_TABLE =

                "CREATE TABLE " + ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE + " (" +
                        ArticleContract.ArticleEntry._ID + " INT NOT NULL, " +
                        ArticleContract.ArticleEntry.COLUMN_AUTHOR + " TEXT, " +
                        ArticleContract.ArticleEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                        ArticleContract.ArticleEntry.COLUMN_DESCRIPTION + " TEXT, " +
                        ArticleContract.ArticleEntry.COLUMN_URL_TO_IMAGE + " TEXT, " +
                        ArticleContract.ArticleEntry.COLUMN_URL + " TEXT NOT NULL, " +
                        ArticleContract.ArticleEntry.COLUMN_PUBLISHED_AT + " TEXT, " +
                        ArticleContract.ArticleEntry.COLUMN_CATEGORY + " TEXT, " +
                        ArticleContract.ArticleEntry.COLUMN_SOURCE+ " TEXT);";

        sqLiteDatabase.execSQL(SQL_CREATE_TOP_ARTICLES_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_LATEST_ARTICLES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        onCreate(sqLiteDatabase);
    }

    public void deleteRecordsFromTopTable(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("DELETE FROM " + ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE);
    }

    public void deleteRecordsFromLatestTable(SQLiteDatabase sqLiteDatabase){
        sqLiteDatabase.execSQL("DELETE FROM " + ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE);
    }
}
