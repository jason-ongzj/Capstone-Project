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

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class GetArticlesListService extends IntentService {

    public static final String TAG = "GetArticlesListService";

    private static final String GET_TOP_ARTICLES = "GetTopArticles";
    private static final String GET_LATEST_ARTICLES = "GetLatestArticles";

    private NewsAPI newsAPI;
    private static String api_Key = "4bea82e302ea46d188f106ffd0121590";
    private static Context mContext;

    public GetArticlesListService(){
        super("GetIngredientsListService");
    }

    public static void getTopArticles(Context context){
        Intent intent = new Intent(context, GetArticlesListService.class);
        mContext = context;
        intent.setAction(GET_TOP_ARTICLES);
        context.startService(intent);
    }

    public static void getLatestArticles(Context context){
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
        if(intent != null) {
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

    private void insertArticles(Article article, String category){
        ContentValues cv = new ContentValues();
        cv.put(ArticleContract.ArticleEntry.COLUMN_AUTHOR, article.getAuthor());
        cv.put(ArticleContract.ArticleEntry.COLUMN_TITLE, article.getTitle());
        cv.put(ArticleContract.ArticleEntry.COLUMN_DESCRIPTION, article.getDescription());
        cv.put(ArticleContract.ArticleEntry.COLUMN_URL_TO_IMAGE, article.getUrlToImage());
        cv.put(ArticleContract.ArticleEntry.COLUMN_URL, article.getUrl());
        cv.put(ArticleContract.ArticleEntry.COLUMN_PUBLISHED_AT, article.getPublishedAt());

        if(category.equals("top"))
            mContext.getContentResolver().insert(ArticleContract.ArticleEntry.TOP_URI, cv);
        else mContext.getContentResolver().insert(ArticleContract.ArticleEntry.LATEST_URI, cv);
    }

    private void getArticles(final String input){
        newsAPI.getSourcesObservable("en")
                .flatMap(new Func1<NewsAPISources, Observable<Source>>() {
                    @Override
                    public Observable<Source> call(NewsAPISources newsAPISources) {
                        return Observable.from(newsAPISources.getSources());
                    }
                }).flatMap(new Func1<Source, Observable<String>>() {
            @Override
            public Observable<String> call(Source source) {
                return Observable.just(source.getId());
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onCompleted() {
                switch(input) {
                    case "top":
                        Log.d(TAG, "Data loading completed: Top articles fetched");
                        Intent localIntent = new Intent(getString(R.string.get_top_articles));
                        localIntent.putExtra("GET_TOP_ARTICLES", "Top articles fetched");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(localIntent);
                        break;
                    case "latest":
                        Log.d(TAG, "Data loading completed: Latest articles fetched");
                        localIntent = new Intent(getString(R.string.get_latest_articles));
                        localIntent.putExtra("GET_LATEST_ARTICLES", "Latest articles fetched");
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(localIntent);
                        break;
                }
            }

            @Override
            public void onError(Throwable e) {
//                e.printStackTrace();
            }

            @Override
            public void onNext(final String s) {
                final String mInput = input;
                // Go for top or latest, popular does not return any results
                newsAPI.getArticlesObservable(s, mInput, api_Key)
                        .flatMap(new Func1<NewsAPIArticles, Observable<Article>>() {
                            @Override
                            public Observable<Article> call(NewsAPIArticles newsAPIArticles) {
                                return Observable.from(newsAPIArticles.getArticles());
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Observer<Article>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onNext(Article article) {
                        insertArticles(article, mInput);
                    }
                });
            }
        });
    }
}
