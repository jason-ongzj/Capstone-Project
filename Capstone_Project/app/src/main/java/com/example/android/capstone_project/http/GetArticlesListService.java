package com.example.android.capstone_project.http;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.android.capstone_project.R;
import com.example.android.capstone_project.http.apimodel.Article;
import com.example.android.capstone_project.http.apimodel.NewsAPISources;
import com.example.android.capstone_project.http.apimodel.Source;

import java.util.ArrayList;
import java.util.List;

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

    private List<Source> sourcesList;

    private ArrayList<Article> topArticlesArray = new ArrayList<>();
    private ArrayList<Article> latestArticlesArray = new ArrayList<>();

    private NewsAPI newsAPI;

    private int count = 0;

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
        newsAPI.getSourcesObservable("", "en")
            .flatMap(new Func1<NewsAPISources, Observable<Source>>() {
                @Override
                public Observable<Source> call(NewsAPISources newsAPISources) {
                    sourcesList = newsAPISources.getSources();
                    return Observable.from(sourcesList);
                }
            }).flatMap(source ->
            Observable.defer(() -> {
                try {
                    Log.d(TAG, "getArticles: " + source.getId());
                    return newsAPI.getArticlesObservable(source.getId(), input,
                            getString(R.string.api_key))
                            .flatMap(newsAPIArticles ->
                                    Observable.from(newsAPIArticles.getArticles())
                            )
                            .filter(article ->
                                !article.getUrl().equals("")
                            )
                            .filter(article ->
                                !article.getDescription().equals("")
                            )
                            .filter(article ->
                                !article.getUrlToImage().equals("")
                            )
                            .map(article -> {
                                try {
                                    article.setCategory(source.getCategory());
                                    article.setSource(source.getName());
                                    addArticlesToArray(input, article);
                                    return article;
                                } catch (Exception e){
                                    return null;
                                }
                            });
                } catch (Exception e){
                    e.printStackTrace();
                    return null;
                }
            }).subscribeOn(Schedulers.newThread()).onErrorResumeNext(Observable.empty()))
            .subscribe(

                new Observer<Article>() {
                    @Override
                    public void onCompleted() {
                        count++;
                        // 2 intent service tasks running, we only intend to broadcast once
                        if(count == 2) {
                            Log.d(TAG, "onCompleted: top" + topArticlesArray.size());
                            Log.d(TAG, "onCompleted: latest" + latestArticlesArray.size());
                            Intent localIntent = new Intent(getString(R.string.get_articles));
                            localIntent.putParcelableArrayListExtra
                                    (getString(R.string.get_top_articles), topArticlesArray);
                            localIntent.putParcelableArrayListExtra
                                    (getString(R.string.get_latest_articles), latestArticlesArray);
                            LocalBroadcastManager.getInstance(getApplicationContext())
                                    .sendBroadcast(localIntent);
                            count = 0;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e);
                    }

                    @Override
                    public void onNext(Article article) {
                    }
                }
            );
    }

    private ArrayList<Article> addArticlesToArray(String input, Article article){
        switch(input){
            case "top":
                topArticlesArray.add(article);
                return topArticlesArray;
            case "latest":
                latestArticlesArray.add(article);
                return latestArticlesArray;
        }
        return null;
    }

}
