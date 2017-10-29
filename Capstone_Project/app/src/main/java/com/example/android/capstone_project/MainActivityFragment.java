package com.example.android.capstone_project;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import com.example.android.capstone_project.data.ArticleContract;
import com.example.android.capstone_project.data.ArticleDbHelper;
import com.example.android.capstone_project.data.ArticleQuery;
import com.example.android.capstone_project.http.apimodel.Article;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivityFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>{

    public final int TOP_ARTICLES = 0;
    public final int LATEST_ARTICLES = 1;
    private final String TAG = "MainActivityFragment";

    private static final int ID_TOP_ARTICLES_LOADER = 156;
    private static final int ID_LATEST_ARTICLES_LOADER = 249;

    private int category_id;
    private MainActivityAdapter mAdapter;

    boolean top_articles_loaded = false;
    boolean latest_articles_loaded = false;

    private ArticleDbHelper helper;
    private SQLiteDatabase db;
    private Spinner spinner;
    private String spinnerSelection = "all";

    private String[] spinnerItems = new String[] {"All", "Business", "Entertainment", "Gaming", "General",
            "Music", "Politics", "Science-And-Nature", "Sport", "Technology"};

    @Nullable
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    public MainActivityFragment() {
    }

    public static MainActivityFragment newInstance(int id) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle args = new Bundle();
        args.putInt("Category_Id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        helper = new ArticleDbHelper(getActivity());
        db = helper.getWritableDatabase();

        if (getArguments() != null) {
            category_id = getArguments().getInt("Category_Id");
        }

        if (savedInstanceState == null) {

            Log.d(TAG, "onCreate: " + Thread.activeCount());

            IntentFilter topArticlesIntentFilter = new IntentFilter(getString(R.string.get_top_articles));
            MyResponseReceiver responseReceiver = new MyResponseReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(responseReceiver, topArticlesIntentFilter);

            IntentFilter latestArticlesIntentFilter = new IntentFilter(getString(R.string.get_latest_articles));
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(responseReceiver, latestArticlesIntentFilter);

            switch (category_id){
                case TOP_ARTICLES:
                    GetArticlesListService.getTopArticles(getActivity());
                    break;
                case LATEST_ARTICLES:
                    GetArticlesListService.getLatestArticles(getActivity());
                    break;
            }

        } else {
            switch(category_id){
                case TOP_ARTICLES:
                    getLoaderManager().initLoader(ID_TOP_ARTICLES_LOADER, null, this);
                    break;
                case LATEST_ARTICLES:
                    getLoaderManager().initLoader(ID_LATEST_ARTICLES_LOADER, null, this);
                    break;
            }
        }
    }

    public void restartLoader(int loaderID){
        getLoaderManager().restartLoader(loaderID, null, this);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_main_activity, container, false);
        ButterKnife.bind(this, mRootView);

        if(getActivity() instanceof MainActivity){
            spinner = ((MainActivity) getActivity()).getSpinner();
            spinnerSelection = ((MainActivity) getActivity()).getSpinnerSelection();
//            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getActivity(),
//                    android.R.layout.simple_spinner_item, spinnerItems);
//            spinner.setAdapter(spinnerAdapter);
//            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
//                    Toast.makeText(getActivity(), "The planet is " +
//                            adapterView.getItemAtPosition(pos).toString(), Toast.LENGTH_LONG).show();
//                    spinnerSelection = adapterView.getItemAtPosition(pos).toString().toLowerCase();
//                    switch (category_id) {
//                        case TOP_ARTICLES:
//                            if(mAdapter != null) {
//                                restartLoader(ID_TOP_ARTICLES_LOADER);
//                                mAdapter.notifyDataSetChanged();
//                            }
//                            break;
//                        case LATEST_ARTICLES:
//                            if(mAdapter != null) {
//                                restartLoader(ID_LATEST_ARTICLES_LOADER);
//                                mAdapter.notifyDataSetChanged();
//                            }
//                            break;
//                    }
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> adapterView) {
//                }
//            });
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(10);
        mRecyclerView.setDrawingCacheEnabled(true);
        return mRootView;
    }

    public void startLoader(int loaderID){
        getLoaderManager().initLoader(loaderID, null, this);
        if (loaderID == ID_TOP_ARTICLES_LOADER)
            Log.d(TAG, "startLoaderTOP:");
        else Log.d(TAG, "startLoaderLATEST: ");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] selectionArgs = new String[] {spinnerSelection};
        switch(category_id){
            case TOP_ARTICLES:
                Uri top_articles_uri = ArticleContract.ArticleEntry.TOP_URI;
                Log.d(TAG, "onCreateLoader: " + top_articles_uri.toString());
                if(spinnerSelection.equals("all")){
                    return new CursorLoader(getActivity(),
                            top_articles_uri,
                            ArticleQuery.PROJECTION,
                            null,
                            null,
                            null);
                } else
                    return new CursorLoader(getActivity(),
                        top_articles_uri,
                        ArticleQuery.PROJECTION,
                        "Category=?",
                        selectionArgs,
                        null);
            case LATEST_ARTICLES:
                Uri latest_articles_uri = ArticleContract.ArticleEntry.LATEST_URI;
                Log.d(TAG, "onCreateLoader: " + latest_articles_uri.toString());
                if(spinnerSelection.equals("all")){
                    return new CursorLoader(getActivity(),
                            latest_articles_uri,
                            ArticleQuery.PROJECTION,
                            null,
                            null,
                            null);
                } else return new CursorLoader(getActivity(),
                        latest_articles_uri,
                        ArticleQuery.PROJECTION,
                        "Category=?",
                        selectionArgs,
                        null);
            default:
                throw new RuntimeException("Loader not implemented");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished: Category " + category_id + ": " + cursor.getCount());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        switch (category_id){
            case TOP_ARTICLES:
                mAdapter = new MainActivityAdapter();
                mAdapter.setContext(getActivity());
                mAdapter.setCursor(cursor);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(linearLayoutManager);
                break;
            case LATEST_ARTICLES:
                mAdapter = new MainActivityAdapter();
                mAdapter.setContext(getActivity());
                mAdapter.setCursor(cursor);
                mRecyclerView.setAdapter(mAdapter);
                mRecyclerView.setLayoutManager(linearLayoutManager);
                break;
        }
    }

    private void insertIntoDb(ArrayList<Article> array, String sortBy){
        String table = "";
        switch(sortBy){
            case "top":
                table = ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE;
                helper.deleteRecordsFromTopTable(db);
                break;
            case "latest":
                table = ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE;
                helper.deleteRecordsFromLatestTable(db);
                break;
        }
        db.beginTransaction();
        for(int i = 0; i < array.size(); i++){
            ContentValues cv = new ContentValues();
            Article article = array.get(i);
            if(article != null) {
                cv.put(ArticleContract.ArticleEntry._ID, i);
                cv.put(ArticleContract.ArticleEntry.COLUMN_AUTHOR, article.getAuthor());
                cv.put(ArticleContract.ArticleEntry.COLUMN_AUTHOR, "");
                cv.put(ArticleContract.ArticleEntry.COLUMN_TITLE, article.getTitle());
                cv.put(ArticleContract.ArticleEntry.COLUMN_DESCRIPTION, article.getDescription());
                cv.put(ArticleContract.ArticleEntry.COLUMN_URL_TO_IMAGE, article.getUrlToImage());
                cv.put(ArticleContract.ArticleEntry.COLUMN_URL, article.getUrl());
                cv.put(ArticleContract.ArticleEntry.COLUMN_PUBLISHED_AT, article.getPublishedAt());
                cv.put(ArticleContract.ArticleEntry.COLUMN_CATEGORY, article.getCategory());
                db.insert(table, null, cv);
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        Log.d(TAG, "insertIntoDb: completed");
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mAdapter.setCursor(null);
        mAdapter.setCursor(null);
    }

    private class MyResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(category_id){
                case TOP_ARTICLES:
                    ArrayList<Article> s = intent.getParcelableArrayListExtra("GET_TOP_ARTICLES");
                    if (s!=null) {
                        Log.d(TAG, "onReceiveTop: " + s.size());
                        Log.d(TAG, "onReceiveTop: " + s.get(1).getTitle());
                        insertIntoDb(s, "top");
                    }
                    if(!top_articles_loaded) {
                        startLoader(ID_TOP_ARTICLES_LOADER);
                        top_articles_loaded = true;
                    }
                    break;
                case LATEST_ARTICLES:
                    s = intent.getParcelableArrayListExtra("GET_LATEST_ARTICLES");
                    if (s!=null) {
                        Log.d(TAG, "onReceiveLatest: " + s.size());
                        Log.d(TAG, "onReceiveLatest: " + s.get(1).getTitle());
                        insertIntoDb(s, "latest");
                    }
                    if(!latest_articles_loaded) {
                        startLoader(ID_LATEST_ARTICLES_LOADER);
                        latest_articles_loaded = true;
                    }
                    break;
            }
        }
    }
}
