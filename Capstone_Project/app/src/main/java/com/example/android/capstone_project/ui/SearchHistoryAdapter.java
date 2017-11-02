package com.example.android.capstone_project.ui;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.android.capstone_project.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchHistoryAdapter extends BaseAdapter {

    private Cursor mCursor;
    private Context mContext;

    @Nullable
    @BindView(R.id.search_history_item)
    TextView historyItemTv;

    private final OnClickHandler mClickHandler;

    public interface OnClickHandler{
        void onHistoryItemClicked(String history_item);
    }

    public SearchHistoryAdapter(Context context, OnClickHandler clickHandler){
        mContext = context;
        mClickHandler = clickHandler;
    }

    public void setCursor(Cursor cursor){
        mCursor = cursor;
    }

    @Override
    public int getCount() {
        if(null == mCursor) return 0;
        return mCursor.getCount();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        mCursor.moveToPosition(position);
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.search_list_item, null);
        ButterKnife.bind(this, view);
        final String history_item = mCursor.getString(0);
        historyItemTv.setText(history_item);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClickHandler.onHistoryItemClicked(history_item);
            }
        });
        return view;
    }
}
