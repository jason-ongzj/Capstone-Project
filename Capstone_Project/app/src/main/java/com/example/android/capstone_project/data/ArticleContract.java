package com.example.android.capstone_project.data;

import android.net.Uri;
import android.provider.BaseColumns;

public class ArticleContract{

    public static final String CONTENT_AUTHORITY = "com.example.android.capstone_project";

    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_TOP_ARTICLES = "top_articles";
    public static final String PATH_LATEST_ARTICLES = "latest_articles";

    public static final class ArticleEntry implements BaseColumns {

        public static final Uri TOP_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_TOP_ARTICLES)
                .build();

        public static final Uri LATEST_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_LATEST_ARTICLES)
                .build();

        public static final String TOP_ARTICLE_TABLE = "top_articles";
        public static final String LATEST_ARTICLE_TABLE = "latest_articles";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_TITLE = "Title";
        public static final String COLUMN_DESCRIPTION = "Description";
        public static final String COLUMN_URL = "Url";
        public static final String COLUMN_URL_TO_IMAGE = "UrlToImage";
        public static final String COLUMN_PUBLISHED_AT = "PublishedAt";
        public static final String COLUMN_CATEGORY = "Category";
        public static final String COLUMN_SOURCE = "Source";
    }
}
