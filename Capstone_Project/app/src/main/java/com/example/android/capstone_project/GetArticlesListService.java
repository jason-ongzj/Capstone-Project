package com.example.android.capstone_project;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.capstone_project.http.NewsAPI;
import com.example.android.capstone_project.http.apimodel.Article;
import com.example.android.capstone_project.http.apimodel.NewsAPIArticles;
import com.example.android.capstone_project.http.apimodel.NewsAPISources;
import com.example.android.capstone_project.http.apimodel.Source;

import java.util.ArrayList;
import java.util.List;
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

    private List<Source> sourcesList;

    private ArrayList<Article> topArticlesArray = new ArrayList<>();
    private ArrayList<Article> latestArticlesArray = new ArrayList<>();

    boolean topArticlesFetched = false;
    boolean latestArticlesFetched = false;

    private NewsAPI newsAPI;
    private static String api_Key = "4bea82e302ea46d188f106ffd0121590";

    public GetArticlesListService() {
        super("GetIngredientsListService");
    }

    public static void getTopArticles(Context context) {
        Intent intent = new Intent(context, GetArticlesListService.class);
        intent.setAction(GET_TOP_ARTICLES);
        context.startService(intent);
    }

    public static void getLatestArticles(Context context) {
        Intent intent = new Intent(context, GetArticlesListService.class);
        intent.setAction(GET_LATEST_ARTICLES);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.d(TAG, "onHandleIntent: " + Thread.activeCount());
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

    private void getArticles(final String input) {
        final ArrayList<String> articleSourcesList = new ArrayList<String>();
        newsAPI.getSourcesObservable("", "en")
                .flatMap(new Func1<NewsAPISources, Observable<Source>>() {
                    @Override
                    public Observable<Source> call(NewsAPISources newsAPISources) {
                        sourcesList = newsAPISources.getSources();
                        return Observable.from(sourcesList);
                    }
                })
                .subscribeOn(Schedulers.newThread()).subscribe(new Observer<Source>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onNext(final Source source) {
                // Go for top or latest, popular does not return any results
                articleSourcesList.add(source.getName());
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

                newsAPI.getArticlesObservable(source.getId(), input, api_Key)
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
                                    // single transaction in the database through execSQL.

                                    topArticlesArray.addAll(arrayArticle);

                                    // Use thread count as an estimate the completion of the observable, since
                                    // RxJava 2 does not have defer function. Thread active count is merely an
                                    // estimate of how close the observable is to completion. We need to ensure only
                                    // a single broadcast is released, so that we can prevent double entries in the
                                    // database. This does not guarantee the capture of the major sources of data

                                    if (Thread.activeCount() <= 25 && !latestArticlesFetched) {
                                        // Set boolean to be true first so that the next thread(or next observer
                                        // subscribed on another thread will not be able to run the code below.
                                        // Else multiple broadcasts could result.

                                        topArticlesFetched = true;
                                        Intent localIntent = new Intent(getString(R.string.get_top_articles));
                                        localIntent.putParcelableArrayListExtra("GET_TOP_ARTICLES", topArticlesArray);
                                        localIntent.putStringArrayListExtra("GET_TOP_ARTICLES_SOURCES", articleSourcesList);
                                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(localIntent);
                                    }
                                    break;

                                case "latest":

                                    latestArticlesArray.addAll(arrayArticle);

                                    // Same as case for "top"
                                    if (Thread.activeCount() <= 25 && !latestArticlesFetched) {
                                        latestArticlesFetched = true;
                                        Intent latestIntent = new Intent(getString(R.string.get_latest_articles));
                                        latestIntent.putParcelableArrayListExtra("GET_LATEST_ARTICLES", latestArticlesArray);
                                        latestIntent.putStringArrayListExtra("GET_LATEST_ARTICLES_SOURCES", articleSourcesList);
                                        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(latestIntent);
                                    }
                                    break;
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                        }

                        @Override
                        public void onNext(Article article) {
                            article.setCategory(source.getCategory());
                            article.setSource(source.getName());
                            arrayArticle.add(article);
                        }
                    });
            }
        });
    }
}
