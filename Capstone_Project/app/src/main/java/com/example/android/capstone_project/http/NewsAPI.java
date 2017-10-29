package com.example.android.capstone_project.http;

import com.example.android.capstone_project.http.apimodel.NewsAPIArticles;
import com.example.android.capstone_project.http.apimodel.NewsAPISources;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;


public interface NewsAPI {

    String BASE_URL = "https://newsapi.org/v1/";

    @GET("articles")
    Call<NewsAPIArticles> getArticles(@Query("source") String source, @Query("apiKey") String apiKey);

    @GET("articles")
    Observable<NewsAPIArticles> getArticlesObservable(@Query("source") String source,
                                                      @Query("sortBy") String sortBy,
                                                      @Query("apiKey") String apiKey);

    @GET("sources")
    Call<NewsAPISources> getSources(@Query("language") String language);

    @GET("sources")
    Observable<NewsAPISources> getSourcesObservable(@Query("category") String category,
                                                    @Query("language") String language);

}
