package com.example.android.capstone_project.ui;

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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.android.capstone_project.R;
import com.example.android.capstone_project.data.ArticleContract;
import com.example.android.capstone_project.data.ArticleDbHelper;
import com.example.android.capstone_project.data.ArticleQuery;
import com.example.android.capstone_project.data.DbUtils;
import com.example.android.capstone_project.http.GetArticlesListService;
import com.example.android.capstone_project.http.apimodel.Article;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivityFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>,
    NavigationAdapter.OnClickHandler{

    public final int TOP_ARTICLES = 0;
    public final int LATEST_ARTICLES = 1;
    private int MAIN_ACTIVITY = 0;
    private final String TAG = "MainActivityFragment";

    private static final int ID_TOP_ARTICLES_LOADER = 156;
    private static final int ID_LATEST_ARTICLES_LOADER = 249;

    private DataInterface mCallback;

    private static String source_item = "";
    private static String current_source;

    private Cursor sourceCursor;

    private int category_id;
    private SearchArticlesAdapter mAdapter;

    private MyResponseReceiver responseReceiver;

    private ArticleDbHelper helper;
    private String spinnerSelection = "all";
    private Spinner spinner;
    private DbUtils utils;

    private String[] spinnerItems = new String[] {"all", "business", "entertainment", "gaming", "general",
            "music", "politics", "science-and-nature", "sport", "technology"};

    @Nullable
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    @Nullable
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    public MainActivityFragment() {
    }

    public static MainActivityFragment newInstance(int id) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle args = new Bundle();
        fragment.setRetainInstance(true);
        args.putInt("Category_Id", id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSourceItemClicked(String source, String category) {
        source_item = source;
        mCallback.onSourceItemClicked(source, category);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            mCallback = (DataInterface) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                + " must implement DataInterface");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        helper = new ArticleDbHelper(getActivity());
        utils = new DbUtils(helper);

        if (getArguments() != null) {
            category_id = getArguments().getInt("Category_Id");
        }

        if (savedInstanceState == null) {
            getArticlesList(getActivity());
        }
    }

    public void getArticlesList(Context context){
        IntentFilter topArticlesIntentFilter = new IntentFilter(getString(R.string.get_top_articles));
        responseReceiver = new MyResponseReceiver();
        LocalBroadcastManager.getInstance(context).registerReceiver(responseReceiver, topArticlesIntentFilter);

        IntentFilter latestArticlesIntentFilter = new IntentFilter(getString(R.string.get_latest_articles));
        LocalBroadcastManager.getInstance(context).registerReceiver(responseReceiver, latestArticlesIntentFilter);

        if(mCallback.getRefreshListButton()!= null)
            mCallback.getRefreshListButton().setEnabled(false);

        switch (category_id) {
            case TOP_ARTICLES:
                GetArticlesListService.getTopArticles(context);
                break;
            case LATEST_ARTICLES:
                GetArticlesListService.getLatestArticles(context);
                break;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!= null) {
            category_id = savedInstanceState.getInt("CategoryId");
            source_item = savedInstanceState.getString("SourceItem");
            current_source = savedInstanceState.getString("CurrentSource");

            Log.d(TAG, "onActivityCreated: " + source_item);

            switch (category_id) {
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
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("CategoryId", category_id);
        outState.putString("CurrentSource", current_source);
        outState.putString("SourceItem", source_item);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_main_activity, container, false);
        ButterKnife.bind(this, mRootView);

        if(getActivity() instanceof MainActivity){
            spinnerSelection = ((MainActivity) getActivity()).getSpinnerSelection();
        }

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(10);
        mRecyclerView.setDrawingCacheEnabled(true);
        return mRootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mProgressBar.setVisibility(View.VISIBLE);
        if(getActivity() instanceof MainActivity)
            ((MainActivity) getActivity()).setItemSelectedTrue();

        SQLiteDatabase readDb = helper.getReadableDatabase();
        spinnerSelection = ((MainActivity) getActivity()).getSpinnerSelection();

        String[] sourceSelection = new String[] {source_item};
        String[] selectionArgs = new String[] {spinnerSelection};

        Uri top_articles_uri = ArticleContract.ArticleEntry.TOP_URI;
        Uri latest_articles_uri = ArticleContract.ArticleEntry.LATEST_URI;
        switch(category_id){
            case TOP_ARTICLES:

                if(spinnerSelection.equals("all")){
                    source_item = "";
                    return new CursorLoader(getActivity(), top_articles_uri, ArticleQuery.PROJECTION,
                            null, null, null);

                } else {

                    if (source_item.equals("")) {

                        // Account for categories available only in Top but not in Latest. If Latest
                        // tab is missing in info, duplicate info from Top tab.
                        Cursor cursor = readDb.query(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE,
                                ArticleQuery.PROJECTION, "Category=?", selectionArgs, null, null, null);
                        int count = cursor.getCount();
                        cursor.close();
                        if(count != 0) {
                            return new CursorLoader(getActivity(), top_articles_uri,
                                    ArticleQuery.PROJECTION, "Category=?", selectionArgs, null);
                        } else return new CursorLoader(getActivity(), latest_articles_uri,
                                ArticleQuery.PROJECTION, "Category=?", selectionArgs, null);

                    } else {

                        // If source selected from navigation drawer, set spinner selection to category,
                        // then query info based on source name provided.
                        Cursor cursor = readDb.query(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE,
                                ArticleQuery.PROJECTION, "Source=?", sourceSelection, null, null, null);
                        int count = cursor.getCount();
                        cursor.close();
                        if(count != 0) {
                            return new CursorLoader(getActivity(), top_articles_uri,
                                    ArticleQuery.PROJECTION, "Source=?", sourceSelection, null);
                        } else return new CursorLoader(getActivity(), latest_articles_uri,
                                ArticleQuery.PROJECTION, "Source=?", sourceSelection, null);
                    }

                }

            case LATEST_ARTICLES:

                if(spinnerSelection.equals("all")){
                    source_item = "";
                    return new CursorLoader(getActivity(), latest_articles_uri,
                            ArticleQuery.PROJECTION, null, null, null);

                } else {

                    if (source_item.equals("")) {

                        // Account for categories available only in Latest but not in Top. If Top
                        // tab is missing in info, duplicate info from Latest tab.
                        Cursor cursor = readDb.query(ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE,
                                ArticleQuery.PROJECTION, "Category=?", selectionArgs, null, null, null);
                        int count = cursor.getCount();
                        cursor.close();
                        if(count != 0) {
                            return new CursorLoader(getActivity(), latest_articles_uri,
                                    ArticleQuery.PROJECTION, "Category=?", selectionArgs, null);
                        } else return new CursorLoader(getActivity(), top_articles_uri,
                                ArticleQuery.PROJECTION, "Category=?", selectionArgs, null);
                    } else {

                        // If source selected from navigation drawer, set spinner selection to category,
                        // then query info based on source name provided.
                        Cursor cursor = readDb.query(ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE,
                                ArticleQuery.PROJECTION, "Source=?", sourceSelection, null, null, null);
                        int count = cursor.getCount();
                        cursor.close();
                        if(count != 0) {
                            return new CursorLoader(getActivity(), latest_articles_uri,
                                    ArticleQuery.PROJECTION, "Source=?", sourceSelection, null);
                        } else return new CursorLoader(getActivity(), top_articles_uri,
                                ArticleQuery.PROJECTION, "Source=?", sourceSelection, null);
                    }
                }
            default:
                throw new RuntimeException("Loader not implemented");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
        updateNavAdapter();

        mCallback.setSyncFinished();
        mCallback.setItemSelectedTrue();
        if(mCallback.getToggle() != null) {
            mCallback.getToggle().setDrawerIndicatorEnabled(true);
        }
        mCallback.getSpinner().setVisibility(View.VISIBLE);
        mCallback.getRefreshListButton().setEnabled(true);

        // Save current source for config reset. "if" condition is required since there are two
        // fragments tracking a single variable, else current_source will be "".
        if(!source_item.equals("")) {
            current_source = source_item;
        }

        // Reset source_item variable after every search, otherwise subsequent searches are limited
        // in scope
        source_item = "";

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new SearchArticlesAdapter(getActivity(), MAIN_ACTIVITY);
        mAdapter.setCursor(cursor);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(responseReceiver);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setCursor(null);
    }

    private void updateNavAdapter(){
        ListView listView = mCallback.getListView();

        if(sourceCursor!=null){
            sourceCursor.close();
        }

        sourceCursor = utils.querySources();
        NavigationAdapter navAdapter = new NavigationAdapter(getActivity(), this);
        navAdapter.setCursor(sourceCursor);
        listView.setAdapter(navAdapter);
    }

    public void hideRecyclerView(){
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    public void setSource(String source){
        source_item = source;
    }

    // Accessed from broadcast receiver
    public void startLoader(int loaderID){
        getLoaderManager().initLoader(loaderID, null, this);
    }

    // Accessed from MainActivity to reload when spinner selection changes
    public void restartLoader(int loaderID, String itemSelected){
        spinnerSelection = itemSelected;
        getLoaderManager().restartLoader(loaderID, null, this);
        mAdapter.notifyDataSetChanged();
    }

    private class MyResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(category_id){
                case TOP_ARTICLES:
                    ArrayList<Article> s = intent.getParcelableArrayListExtra("GET_TOP_ARTICLES");
                    if(s!=null) {
                        utils.insertIntoDb(s, "top");
                        startLoader(ID_TOP_ARTICLES_LOADER);
                    }
                    break;
                case LATEST_ARTICLES:
                    s = intent.getParcelableArrayListExtra("GET_LATEST_ARTICLES");
                    if(s!= null){
                        utils.insertIntoDb(s, "latest");
                        startLoader(ID_LATEST_ARTICLES_LOADER);
                    }
                    break;
            }
        }
    }
}
