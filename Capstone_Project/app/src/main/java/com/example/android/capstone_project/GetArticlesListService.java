package com.example.android.capstone_project;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.capstone_project.data.ArticleContract;
import com.example.android.capstone_project.http.NewsAPI;
import com.example.android.capstone_project.http.apimodel.Article;
import com.example.android.capstone_project.http.apimodel.NewsAPIArticles;
import com.example.android.capstone_project.http.apimodel.NewsAPISources;
import com.example.android.capstone_project.http.apimodel.Source;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class GetArticlesListService extends IntentService {

    public static final String TAG = "GetArticlesListService";

    private static final String GET_TOP_ARTICLES = "GetTopArticles";
    private static final String GET_LATEST_ARTICLES = "GetLatestArticles";

    private AtomicInteger topSourceIndex = new AtomicInteger(0);
    private AtomicInteger latestSourceIndex = new AtomicInteger(0);

    private ArrayList<String> topArticlesInsertArray = new ArrayList<>();
    private ArrayList<String> latestArticlesInsertArray = new ArrayList<>();

    private ArrayList<Article> topArticlesArray = new ArrayList<Article>();
    private ArrayList<Article> latestArticlesArray = new ArrayList<Article>();

    boolean topArticlesFetched = false;
    boolean latestArticlesFetched = false;

    private NewsAPI newsAPI;
    private static String api_Key = "4bea82e302ea46d188f106ffd0121590";
    private static Context mContext;

    public GetArticlesListService() {
        super("GetIngredientsListService");
    }

    public static void getTopArticles(Context context) {
        Intent intent = new Intent(context, GetArticlesListService.class);
        mContext = context;
        intent.setAction(GET_TOP_ARTICLES);
        context.startService(intent);
    }

    public static void getLatestArticles(Context context) {
        Intent intent = new Intent(context, GetArticlesListService.class);
        mContext = context;
        intent.setAction(GET_LATEST_ARTICLES);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(NewsAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        newsAPI = retrofit.create(NewsAPI.class);
        if (intent != null) {
            switch (intent.getAction()) {
                case GET_TOP_ARTICLES:
                    getArticles("top");
                    break;
                case GET_LATEST_ARTICLES:
                    getArticles("latest");
                    break;
            }
        }
    }

    private void insertArticles(Article article, String category) {
        ContentValues cv = new ContentValues();
        cv.put(ArticleContract.ArticleEntry.COLUMN_AUTHOR, article.getAuthor());
        cv.put(ArticleContract.ArticleEntry.COLUMN_TITLE, article.getTitle());
        cv.put(ArticleContract.ArticleEntry.COLUMN_DESCRIPTION, article.getDescription());
        cv.put(ArticleContract.ArticleEntry.COLUMN_URL_TO_IMAGE, article.getUrlToImage());
        cv.put(ArticleContract.ArticleEntry.COLUMN_URL, article.getUrl());
        cv.put(ArticleContract.ArticleEntry.COLUMN_PUBLISHED_AT, article.getPublishedAt());
    }

    private void getArticles(final String input) {
        newsAPI.getSourcesObservable("en")
                .flatMap(new Func1<NewsAPISources, Observable<Source>>() {
                    @Override
                    public Observable<Source> call(NewsAPISources newsAPISources) {
                        return Observable.from(newsAPISources.getSources());
                    }
                })
//                .filter(new Func1<Source, Boolean>() {
//                    @Override
//                    public Boolean call(Source source) {
//                        return source.getCategory().equals("general");
//                    }
//                })
                .flatMap(new Func1<Source, Observable<String>>() {
                    @Override
                    public Observable<String> call(Source source) {
                        return Observable.just(source.getId());
                    }
                })
                .subscribeOn(Schedulers.newThread()).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(final String s) {
                final ArrayList<String> articlesList = new ArrayList<>();
                final ArrayList<Article> arrayArticle = new ArrayList<Article>();
                switch (input) {
                    case "top":
                        topSourceIndex.getAndIncrement();
                        Log.d(TAG, "onNext: " + topSourceIndex.get());
                        break;
                    case "latest":
                        latestSourceIndex.getAndIncrement();
                        Log.d(TAG, "onNext: " + latestSourceIndex.get());
                        break;
                }
                // Go for top or latest, popular does not return any results
                final AtomicInteger articleID = new AtomicInteger(0);
                newsAPI.getArticlesObservable(s, input, api_Key)
                        .flatMap(new Func1<NewsAPIArticles, Observable<Article>>() {
                            @Override
                            public Observable<Article> call(NewsAPIArticles newsAPIArticles) {
                                return Observable.from(newsAPIArticles.getArticles());
                            }
                        }).filter(new Func1<Article, Boolean>() {
                    @Override
                    public Boolean call(Article article) {
                        return !article.getDescription().equals("");
                    }
                }).subscribeOn(Schedulers.newThread())
                    .subscribe(new Observer<Article>() {
                        @Override
                        public void onCompleted() {
                            switch (input) {
                                case "top":

                                    // Add all insertions into a single arraylist, to be parsed in a
                                    // single transaction in the database ExecSQL.

                                    topArticlesInsertArray.addAll(articlesList);
                                    topArticlesArray.addAll(arrayArticle);
                                    Log.d(TAG, "onCompleted: topArticlesStringArray" + topArticlesInsertArray.size());
                                    Log.d(TAG, "onCompleted: topArticlesArray " + topArticlesArray.size());
                                    Log.d(TAG, "onReceive: " + Thread.activeCount());

                                    // Use thread count as an estimate the completion of the observable, since
                                    // RxJava 2 does not have defer function. Thread active count is merely an
                                    // estimate of how close the observable is to completion. We need to ensure only
                                    // a single broadcast is released, so that we can prevent double entries in the
                                    // database. This does not guarantee the capture of the major sources of data

                                    if (Thread.activeCount() <= 25 && !topArticlesFetched) {

                                        // Set boolean to be true first so that the next thread(or next observer
                                        // subscribed on another thread will not be able to run the code below.
                                        // Else multiple broadcasts could result.

                                        topArticlesFetched = true;
                                        Intent localIntent = new Intent(getString(R.string.get_top_articles));
                                        localIntent.putParcelableArrayListExtra("GET_TOP_ARTICLES", topArticlesArray);
                                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(localIntent);

                                    }
                                    break;

                                case "latest":

                                    latestArticlesInsertArray.addAll(articlesList);
                                    latestArticlesArray.addAll(arrayArticle);

                                    // Same as case for "top"

                                    Log.d(TAG, "onCompleted: latestArticlesStringArray" + latestArticlesInsertArray.size());
                                    Log.d(TAG, "onCompleted: latestArticlesArray " + latestArticlesArray.size());
                                    Log.d(TAG, "onReceive: " + Thread.activeCount());
                                    if (Thread.activeCount() <= 10 && !latestArticlesFetched) {
                                        latestArticlesFetched = true;
                                        Intent latestIntent = new Intent(getString(R.string.get_latest_articles));
                                        latestIntent.putParcelableArrayListExtra("GET_LATEST_ARTICLES", latestArticlesArray);
                                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(latestIntent);
                                    }
                                    break;
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(Article article) {
                            arrayArticle.add(article);
                            articleID.getAndIncrement();
                            switch (input) {
                                case "top":
                                    String sql = "INSERT INTO " + ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE
                                            + " VALUES"
                                            + "(" + articleID + ", \'" + article.getAuthor() + "\', \'" + article.getTitle() +
                                            "\', \'" + article.getDescription() + "\', \'" + article.getUrlToImage() +
                                            "\', \'" + article.getUrl() + "\', \'" + article.getPublishedAt() + "\');";
                                    articlesList.add(sql);
                                    break;
                                case "latest":
                                    sql = "INSERT INTO " + ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE
                                            + "  VALUES"
                                            + "(" + articleID + ", \'" + article.getAuthor() + "\', \'" + article.getTitle() +
                                            "\', \'" + article.getDescription() + "\', \'" + article.getUrlToImage() +
                                            "\', \'" + article.getUrl() + "\', \'" + article.getPublishedAt() + "\');";
                                    articlesList.add(sql);
                                    break;
                            }
                        }
                    });
            }
        });
    }
}
