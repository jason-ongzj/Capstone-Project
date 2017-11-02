package com.example.android.capstone_project.data;

public interface ArticleQuery {

    String[] PROJECTION = {
            ArticleContract.ArticleEntry._ID,
            ArticleContract.ArticleEntry.COLUMN_AUTHOR,
            ArticleContract.ArticleEntry.COLUMN_TITLE,
            ArticleContract.ArticleEntry.COLUMN_DESCRIPTION,
            ArticleContract.ArticleEntry.COLUMN_URL_TO_IMAGE,
            ArticleContract.ArticleEntry.COLUMN_URL,
            ArticleContract.ArticleEntry.COLUMN_PUBLISHED_AT,
            ArticleContract.ArticleEntry.COLUMN_CATEGORY,
            ArticleContract.ArticleEntry.COLUMN_SOURCE
    };

    String[] SEARCH_PROJECTION = {
            ArticleContract.ArticleEntry.COLUMN_TITLE,
            ArticleContract.ArticleEntry.COLUMN_DESCRIPTION,
            ArticleContract.ArticleEntry.COLUMN_URL_TO_IMAGE,
            ArticleContract.ArticleEntry.COLUMN_URL
    };

    int ID = 0;
    int AUTHOR = 1;
    int TITLE = 2;
    int DESCRIPTION = 3;
    int URL_TO_IMAGE = 4;
    int URL = 5;
    int PUBLISHED_AT = 6;
    int CATEGORY = 7;
    int SOURCE = 8;

    int SEARCH_TITLE = 0;
    int SEARCH_DESCRIPTION = 1;
    int SEARCH_URL_TO_IMAGE = 2;
    int SEARCH_URL = 3;
}
