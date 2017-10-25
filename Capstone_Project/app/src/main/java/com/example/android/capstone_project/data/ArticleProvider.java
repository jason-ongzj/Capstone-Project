package com.example.android.capstone_project.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ArticleProvider extends ContentProvider {

    private static final int TOP_ARTICLES = 100;
//    private static final int TOP_ARTICLES_WITH_ID = 101;

    private static final int LATEST_ARTICLES = 200;
//    private static final int LATEST_ARTICLES_WITH_ID = 201;

    private ArticleDbHelper mArticleDbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher(){
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ArticleContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ArticleContract.PATH_TOP_ARTICLES, TOP_ARTICLES);
//        matcher.addURI(authority, ArticleContract.PATH_TOP_ARTICLES + "/#", TOP_ARTICLES_WITH_ID);

        matcher.addURI(authority, ArticleContract.PATH_LATEST_ARTICLES, LATEST_ARTICLES);
//        matcher.addURI(authority, ArticleContract.PATH_LATEST_ARTICLES + "/#", LATEST_ARTICLES_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mArticleDbHelper = new ArticleDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mArticleDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch(match){
            case TOP_ARTICLES:
                retCursor = db.query(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case LATEST_ARTICLES:
                retCursor = db.query(ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return retCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final SQLiteDatabase db = mArticleDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match){
            case TOP_ARTICLES:
                long id = db.insert(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE, null, contentValues);
                if(id > 0){
                    returnUri = ContentUris.withAppendedId(ArticleContract.ArticleEntry.TOP_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case LATEST_ARTICLES:
                id = db.insert(ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE, null, contentValues);
                if(id > 0){
                    returnUri = ContentUris.withAppendedId(ArticleContract.ArticleEntry.LATEST_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
