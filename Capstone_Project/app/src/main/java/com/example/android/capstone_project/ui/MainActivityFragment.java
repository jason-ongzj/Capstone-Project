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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private boolean onRotate = false;
    private Parcelable recyclerViewState;
    private Parcelable listViewState;

    private Cursor sourceCursor;

    private int category_id;
    private SearchArticlesAdapter mAdapter;

    private MyResponseReceiver responseReceiver;
    private int mListViewSelectedItem = 999;

    private ArticleDbHelper helper;
    private String spinnerSelection = "all";
    private DbUtils utils;
    private static String categoryId = "Category_Id";

    @Nullable
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    public MainActivityFragment() {
    }

    public static MainActivityFragment newInstance(int id) {
        MainActivityFragment fragment = new MainActivityFragment();
        Bundle args = new Bundle();
        fragment.setRetainInstance(true);
        args.putInt(categoryId, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSourceItemClicked(String source, String category, View view, int pos) {
        source_item = source;
        mCallback.onItemClicked(source, category);
        view.setBackgroundColor(getResources().getColor(R.color.color_light_blue));
        mCallback.updateListViewSelected(pos);
        mListViewSelectedItem = mCallback.getListViewSelected();
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
            category_id = getArguments().getInt(categoryId);
        }

        if (savedInstanceState == null) {
            getArticlesList(getActivity());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            onRotate = true;
            source_item = savedInstanceState.getString(getString(R.string.source_item));
            spinnerSelection = savedInstanceState.getString(getString(R.string.spinnerSelection));

            recyclerViewState = savedInstanceState.getParcelable
                    (getString(R.string.layoutManagerRestore));
            listViewState = savedInstanceState.getParcelable(getString(R.string.listViewRestore));
            mListViewSelectedItem = savedInstanceState.getInt(getString(R.string.listViewSelected));

            category_id = savedInstanceState.getInt(getString(R.string.categoryId));

            if(!mCallback.getRefreshStatus()) {
                switch (category_id) {
                    case TOP_ARTICLES:
                        startLoader(ID_TOP_ARTICLES_LOADER);
                        break;
                    case LATEST_ARTICLES:
                        startLoader(ID_LATEST_ARTICLES_LOADER);
                        break;
                }
            }
        } else {
            spinnerSelection =  mCallback.getSpinnerSelection();
            source_item = mCallback.getSourceName();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(getString(R.string.categoryId), category_id);
        outState.putString(getString(R.string.source_item), source_item);
        outState.putString(getString(R.string.spinnerSelection), spinnerSelection);
        if(mRecyclerView.getLayoutManager() != null) {
            outState.putParcelable(getString(R.string.layoutManagerRestore), mRecyclerView
                    .getLayoutManager().onSaveInstanceState());
        }
        outState.putParcelable(getString(R.string.listViewRestore),
                mCallback.getListView().onSaveInstanceState());
        outState.putInt(getString(R.string.listViewSelected), mCallback.getListViewSelected());

        super.onSaveInstanceState(outState);
    }

    public void getArticlesList(Context context){
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        IntentFilter topArticlesIntentFilter = new IntentFilter
                    (getString(R.string.get_articles));

        responseReceiver = new MyResponseReceiver();
        LocalBroadcastManager.getInstance(context).registerReceiver(responseReceiver,
                    topArticlesIntentFilter);

        if(mCallback.getRefreshListButton()!= null)
            mCallback.getRefreshListButton().setEnabled(false);

        if(isConnected) {
            switch (category_id) {
                case TOP_ARTICLES:
                    GetArticlesListService.getTopArticles(context);
                    break;
                case LATEST_ARTICLES:
                    GetArticlesListService.getLatestArticles(context);
                    break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_main_activity, container, false);
        ButterKnife.bind(this, mRootView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(10);
        mRecyclerView.setDrawingCacheEnabled(true);
        return mRootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        hideRecyclerView();

        SQLiteDatabase readDb = helper.getReadableDatabase();

        String[] sourceSelection = new String[] {source_item};
        String[] selectionArgs = new String[] {spinnerSelection};

        Uri top_articles_uri = ArticleContract.ArticleEntry.TOP_URI;
        Uri latest_articles_uri = ArticleContract.ArticleEntry.LATEST_URI;
        switch(category_id){
            case TOP_ARTICLES:

                if(spinnerSelection.equals(this.getString(R.string.all))){
                    source_item = "";
                    return new CursorLoader(getActivity(), top_articles_uri, ArticleQuery.PROJECTION,
                            null, null, null);

                } else {

                    if (source_item.equals("")) {

                        // Account for categories available only in Top but not in Latest. If Latest
                        // tab is missing in info, duplicate info from Top tab.
                        Cursor cursor = readDb.query(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE,
                                ArticleQuery.PROJECTION, this.getString(R.string.category_query),
                                selectionArgs, null, null, null);
                        int count = cursor.getCount();
                        cursor.close();
                        if(count != 0) {
                            return new CursorLoader(getActivity(), top_articles_uri,
                                    ArticleQuery.PROJECTION, this.getString(R.string.category_query),
                                    selectionArgs, null);
                        } else return new CursorLoader(getActivity(), latest_articles_uri,
                                ArticleQuery.PROJECTION, this.getString(R.string.category_query),
                                selectionArgs, null);

                    } else {

                        // If source selected from navigation drawer, set spinner selection to category,
                        // then query info based on source name provided.
                        Cursor cursor = readDb.query(ArticleContract.ArticleEntry.TOP_ARTICLE_TABLE,
                                ArticleQuery.PROJECTION, this.getString(R.string.source_query),
                                sourceSelection, null, null, null);
                        int count = cursor.getCount();
                        cursor.close();
                        if(count != 0) {
                            return new CursorLoader(getActivity(), top_articles_uri,
                                    ArticleQuery.PROJECTION, this.getString(R.string.source_query),
                                    sourceSelection, null);
                        } else return new CursorLoader(getActivity(), latest_articles_uri,
                                ArticleQuery.PROJECTION, this.getString(R.string.source_query),
                                sourceSelection, null);

                    }
                }

            case LATEST_ARTICLES:

                if(spinnerSelection.equals(this.getString(R.string.all))){
                    source_item = "";
                    return new CursorLoader(getActivity(), latest_articles_uri,
                            ArticleQuery.PROJECTION, null, null, null);

                } else {

                    if (source_item.equals("")) {

                        // Account for categories available only in Latest but not in Top. If Top
                        // tab is missing in info, duplicate info from Latest tab.
                        Cursor cursor = readDb.query(ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE,
                                ArticleQuery.PROJECTION, this.getString(R.string.category_query),
                                selectionArgs, null, null, null);
                        int count = cursor.getCount();
                        cursor.close();
                        if(count != 0) {
                            return new CursorLoader(getActivity(), latest_articles_uri,
                                    ArticleQuery.PROJECTION, this.getString(R.string.category_query),
                                    selectionArgs, null);
                        } else return new CursorLoader(getActivity(), top_articles_uri,
                                ArticleQuery.PROJECTION,  this.getString(R.string.category_query),
                                selectionArgs, null);
                    } else {

                        // If source selected from navigation drawer, set spinner selection to category,
                        // then query info based on source name provided.
                        Cursor cursor = readDb.query(ArticleContract.ArticleEntry.LATEST_ARTICLE_TABLE,
                                ArticleQuery.PROJECTION, this.getString(R.string.source_query),
                                sourceSelection, null, null, null);
                        int count = cursor.getCount();
                        cursor.close();
                        if(count != 0) {
                            return new CursorLoader(getActivity(), latest_articles_uri,
                                    ArticleQuery.PROJECTION, this.getString(R.string.source_query),
                                    sourceSelection, null);
                        } else return new CursorLoader(getActivity(), top_articles_uri,
                                ArticleQuery.PROJECTION, this.getString(R.string.source_query),
                                sourceSelection, null);
                    }
                }
            default:
                throw new RuntimeException("Loader not implemented");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mRecyclerView.setVisibility(View.VISIBLE);

        // Update listView when refreshed
        if(mCallback.getRefreshStatus()) {
            updateNavAdapter();
        }

        mCallback.setSyncFinished();
        mCallback.setRefreshStatusFalse();

        mCallback.hideProgressBar();

        // Prevent spammed refreshes, enable only when data is completely fetched from server
        if(mCallback.getRefreshListButton() != null)
        mCallback.getRefreshListButton().setEnabled(true);

        // Check if network receiver was registered, and if so unregister it
        if(mCallback.isNetworkChangeReceiverSet() && mCallback.getNetworkChangeReceiver()!= null) {
            getActivity().unregisterReceiver(mCallback.getNetworkChangeReceiver());
            mCallback.setNetworkChangeReceiverFalse();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new SearchArticlesAdapter(getActivity(), MAIN_ACTIVITY);
        mAdapter.setCursor(cursor);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        // Restore rotation
        if(recyclerViewState != null && onRotate){
            updateNavAdapter();
            mRecyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
            mCallback.updateRotationStatus();
        }

        Log.d(TAG, "onLoadFinished: " + mCallback.getRotationStatus());
        onRotate = false;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.setCursor(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(responseReceiver);
    }

    private void updateNavAdapter(){
        if (sourceCursor != null) {
            sourceCursor.close();
        }

        sourceCursor = utils.querySources();
        NavigationAdapter navAdapter = new NavigationAdapter(getActivity(), this);
        if(mListViewSelectedItem != 999)
            navAdapter.setSelectedItem(mListViewSelectedItem);
        navAdapter.setCursor(sourceCursor);
        mCallback.getListView().setAdapter(navAdapter);
        mCallback.getListView().setVisibility(View.VISIBLE);

        // Callback from MainActivity
        if(listViewState != null) {
            mCallback.getListView().onRestoreInstanceState(listViewState);
        }
    }

    public void hideRecyclerView(){
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    public void setSource(String source){
        source_item = source;
    }

    // Accessed from broadcast receiver. Using restart loader will ensure there will always be only 2
    // loaders, while initLoader will create endless amount of loaders.
    public void startLoader(int loaderID){
        getLoaderManager().restartLoader(loaderID, null, this);
    }

    // Accessed from MainActivity to reload when spinner selection changes
    public void restartLoader(int loaderID, String itemSelected){
        spinnerSelection = itemSelected;
        getLoaderManager().restartLoader(loaderID, null, this);
        mAdapter.notifyDataSetChanged();
    }

    // Broadcast receiver to receive output from GetArticlesListService
    private class MyResponseReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch(category_id){
                case TOP_ARTICLES:
                    ArrayList<Article> articleArrayList = intent.getParcelableArrayListExtra
                            (getString(R.string.get_top_articles));
                    if(articleArrayList != null && isAdded()) {
                        utils.insertIntoDb(articleArrayList,
                                getString(R.string.top).toLowerCase());
                        startLoader(ID_TOP_ARTICLES_LOADER);
                    }
                    break;
                case LATEST_ARTICLES:
                    articleArrayList = intent.getParcelableArrayListExtra
                            (getString(R.string.get_latest_articles));
                    if(articleArrayList!= null && isAdded()){
                        utils.insertIntoDb(articleArrayList,
                                getString(R.string.latest).toLowerCase());
                        startLoader(ID_LATEST_ARTICLES_LOADER);
                    }
                    break;
            }
        }
    }
}
