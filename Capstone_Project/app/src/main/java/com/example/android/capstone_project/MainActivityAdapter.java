package com.example.android.capstone_project;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.capstone_project.data.ArticleQuery;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivityAdapter extends
        RecyclerView.Adapter<MainActivityAdapter.MainActivityAdapterViewHolder> {

    private Context mContext;
    private Cursor mCursor;


    public void setContext(Context context) {
        mContext = context;
    }

    public void setCursor(Cursor cursor){
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public class MainActivityAdapterViewHolder extends RecyclerView.ViewHolder{

        @Nullable
        @BindView(R.id.card_display_author)
        TextView authorTv;
        @Nullable
        @BindView(R.id.card_display_title)
        TextView titleTv;
        @Nullable
        @BindView(R.id.card_display_description)
        TextView descriptionTv;
        @Nullable
        @BindView(R.id.card_display_image)
        ImageView imageView;

        public MainActivityAdapterViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public MainActivityAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_display, parent, false);
        return new MainActivityAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainActivityAdapterViewHolder holder, int position) {
        if(mCursor != null) {
            mCursor.moveToPosition(position);
            Log.d("MainActivityAdapter", "onBindViewHolder: " + mCursor.getString(ArticleQuery.AUTHOR));
            holder.authorTv.setText(mCursor.getString(ArticleQuery.AUTHOR));
        }
    }

    @Override
    public int getItemCount() {
        return 20;
    }
}
