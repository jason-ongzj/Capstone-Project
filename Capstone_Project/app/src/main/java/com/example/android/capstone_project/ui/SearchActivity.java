package com.example.android.capstone_project.ui;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.capstone_project.R;
import com.example.android.capstone_project.data.ArticleContract;
import com.example.android.capstone_project.data.ArticleDbHelper;
import com.example.android.capstone_project.data.DbUtils;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SearchHistoryAdapter.OnClickHandler{

    @Nullable
    @BindView(R.id.search_toolbar)
    android.support.v7.widget.Toolbar toolbar;
    @BindView(R.id.deleteText)
    ImageView deleteText;
    @BindView(R.id.editText)
    EditText editText;
    @BindView(R.id.search_recyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.search_listView)
    ListView mListView;
    @BindView(R.id.search_history)
    View searchHistoryView;
    @BindView(R.id.clear_history)
    View clearHistoryView;

    private Cursor mCursor;
    private ArticleDbHelper helper;
    private DbUtils utils;
    private Tracker mTracker;
    private static GoogleAnalytics sAnalytics;
    private FirebaseAnalytics firebaseAnalytics;

    private int SEARCH_ACTIVITY = 1;
    private SearchHistoryAdapter mSearchHistoryAdapter;

    private int ID_SEARCH_ARTICLES = 289;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportLoaderManager().initLoader(ID_SEARCH_ARTICLES, null, this);

        mSearchHistoryAdapter = new SearchHistoryAdapter(this, this);
        mListView.setAdapter(mSearchHistoryAdapter);

        helper = new ArticleDbHelper(this);
        utils = new DbUtils(helper);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

//        sAnalytics = GoogleAnalytics.getInstance(this);
//        mTracker = sAnalytics.newTracker(R.xml.global_tracker);
        editText.setSingleLine(true);

        clearHistoryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase writableDb = utils.getHelper().getWritableDatabase();
                utils.getHelper().deleteRecordsFromSearchHistoryTable(writableDb);
                getSupportLoaderManager().restartLoader(ID_SEARCH_ARTICLES, null, SearchActivity.this);
            }
        });

        deleteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editText.setText("");
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(editText.getText().length() > 0){
                    deleteText.setVisibility(View.VISIBLE);
                } else deleteText.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i == EditorInfo.IME_ACTION_SEARCH){
                    InputMethodManager inm = (InputMethodManager) textView.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    editText.setCursorVisible(false);

                    ContentValues cv = new ContentValues();
                    cv.put(ArticleContract.SearchEntry.COLUMN_SEARCH_ITEM, editText.getText().toString());
                    utils.getDb().insert(ArticleContract.SearchEntry.SEARCH_ARTICLE_TABLE, null, cv);
                    mSearchHistoryAdapter.notifyDataSetChanged();

                    mCursor = utils.queryCombinedArticleLists(editText.getText().toString());

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this);
                    SearchArticlesAdapter adapter = new SearchArticlesAdapter(SearchActivity.this, SEARCH_ACTIVITY);
                    adapter.setCursor(mCursor);
                    mRecyclerView.setAdapter(adapter);
                    mRecyclerView.setLayoutManager(linearLayoutManager);

                    searchHistoryView.setVisibility(View.INVISIBLE);

                    Bundle bundle = new Bundle();
                    bundle.putString("search_term", editText.getText().toString());
                    firebaseAnalytics.logEvent("Search", bundle);

                    return true;
                } else if (i == EditorInfo.IME_FLAG_NAVIGATE_PREVIOUS) {
                    editText.setCursorVisible(false);
                    return true;
                } else return false;
            }
        });

        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                getSupportLoaderManager().restartLoader(ID_SEARCH_ARTICLES, null, SearchActivity.this);
                editText.setCursorVisible(true);
                searchHistoryView.setVisibility(View.VISIBLE);
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onHistoryItemClicked(String history_item) {
        editText.setText(history_item);
        editText.setCursorVisible(false);
        InputMethodManager inputManager = (InputMethodManager) getSystemService
                (Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        searchHistoryView.setVisibility(View.INVISIBLE);
        mCursor = utils.queryCombinedArticleLists(editText.getText().toString());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SearchActivity.this);
        SearchArticlesAdapter adapter = new SearchArticlesAdapter(SearchActivity.this, SEARCH_ACTIVITY);
        adapter.setCursor(mCursor);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = "(" + ArticleContract.SearchEntry.COLUMN_SEARCH_ITEM
                + " NOT NULL) GROUP BY (" + ArticleContract.SearchEntry.COLUMN_SEARCH_ITEM  + ")";
        if(id == ID_SEARCH_ARTICLES){
            Uri searchUri = ArticleContract.SearchEntry.SEARCH_URI;
            return new CursorLoader(this,
                    searchUri,
                    null,
                    selection,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if(mSearchHistoryAdapter != null) {
            mSearchHistoryAdapter.setCursor(cursor);
            mListView.setAdapter(mSearchHistoryAdapter);
            if(cursor.getCount() != 0){
                clearHistoryView.setVisibility(View.VISIBLE);
            } else clearHistoryView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(mSearchHistoryAdapter != null)
            mSearchHistoryAdapter.setCursor(null);
    }
}
