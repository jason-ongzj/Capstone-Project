package com.example.android.capstone_project;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
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

import com.example.android.capstone_project.data.ArticleContract;
import com.example.android.capstone_project.data.ArticleDbHelper;

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
        if (getArguments() != null) {
            category_id = getArguments().getInt("Category_Id");
        }
        if (savedInstanceState == null) {
            ArticleDbHelper dbHelper = new ArticleDbHelper(getActivity());
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dbHelper.deleteRecords(db);
            db.close();

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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_main_activity, container, false);
        ButterKnife.bind(this, mRootView);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        return mRootView;
    }

    private void delayLoader(int loaderID){
        try
        {
            Thread.sleep(5000);
            startLoader(loaderID);
//            Thread.sleep(1000);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    private void startLoader(int loaderID){
        getLoaderManager().initLoader(loaderID, null, this);
        if (loaderID == ID_TOP_ARTICLES_LOADER)
            Log.d(TAG, "startLoaderTOP:");
        else Log.d(TAG, "startLoaderLATEST: ");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(category_id){
            case TOP_ARTICLES:
                Uri top_articles_uri = ArticleContract.ArticleEntry.TOP_URI;
                Log.d(TAG, "onCreateLoader: " + top_articles_uri.toString());
                return new CursorLoader(getActivity(),
                        top_articles_uri,
                        null,
                        null,
                        null,
                        null);
            case LATEST_ARTICLES:
                Uri latest_articles_uri = ArticleContract.ArticleEntry.LATEST_URI;
                Log.d(TAG, "onCreateLoader: " + latest_articles_uri.toString());
                return new CursorLoader(getActivity(),
                        latest_articles_uri,
                        null,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader not implemented");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished: Category " + category_id + ": " + cursor.getCount());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new MainActivityAdapter();
        mAdapter.setContext(getActivity());
        mAdapter.setCursor(cursor);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);
//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                if(!mRecyclerView.canScrollVertically(1)){
//                    mAdapter.setItemCount();
//                    mAdapter.notifyDataSetChanged();
//                }
//            }
//        });
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {
        mAdapter.setCursor(null);
    }

    private class MyResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(category_id){
                case TOP_ARTICLES:
                    String s = intent.getStringExtra("GET_TOP_ARTICLES");
                    Log.d(TAG, "onReceiveTop: " + s);
                    if(!top_articles_loaded) {
                        delayLoader(ID_TOP_ARTICLES_LOADER);
                        top_articles_loaded = true;
                    }
                    break;
                case LATEST_ARTICLES:
                    s = intent.getStringExtra("GET_LATEST_ARTICLES");
                    Log.d(TAG, "onReceiveLatest: " + s);
                    if(!latest_articles_loaded) {
                        delayLoader(ID_LATEST_ARTICLES_LOADER);
                        latest_articles_loaded = true;
                    }
                    break;
            }

        }
    }

    public void updateAdapter(){
        mAdapter.notifyDataSetChanged();
    }

}
