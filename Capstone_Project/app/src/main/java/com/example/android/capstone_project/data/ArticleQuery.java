package com.example.android.capstone_project.data;

public interface ArticleQuery {

    String[] PROJECTION = {
//            ArticleContract.ArticleEntry._ID,
            ArticleContract.ArticleEntry.COLUMN_AUTHOR,
            ArticleContract.ArticleEntry.COLUMN_TITLE,
            ArticleContract.ArticleEntry.COLUMN_DESCRIPTION,
            ArticleContract.ArticleEntry.COLUMN_URL_TO_IMAGE,
            ArticleContract.ArticleEntry.COLUMN_URL,
            ArticleContract.ArticleEntry.COLUMN_PUBLISHED_AT
    };

    int AUTHOR = 0;
    int TITLE = 1;
    int DESCRIPTION = 2;
    int URL_TO_IMAGE = 3;
    int URL = 4;
    int PUBLISHED_AT = 5;
}
