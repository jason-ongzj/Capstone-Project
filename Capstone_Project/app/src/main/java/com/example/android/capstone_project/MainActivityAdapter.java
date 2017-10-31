package com.example.android.capstone_project;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.capstone_project.data.ArticleQuery;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivityAdapter extends
        RecyclerView.Adapter<MainActivityAdapter.MainActivityAdapterViewHolder> {

    private Context mContext;
    private Cursor mCursor;
    private ArrayList<String> sourceList;

    public void setContext(Context context) {
        mContext = context;
    }

    public void setCursor(Cursor cursor){
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public void setSourceList(ArrayList<String> list){
        sourceList = list;
    }

    public class MainActivityAdapterViewHolder extends RecyclerView.ViewHolder{

        @Nullable
        @BindView(R.id.card_display)
        View card_display;
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

        @OnClick(R.id.card_display)
        public void onClick(){
            Intent intent = new Intent(mContext, WebViewActivity.class);
            mCursor.moveToPosition(getAdapterPosition());
            intent.putExtra("URL", mCursor.getString(ArticleQuery.URL));
            mContext.startActivity(intent);
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
            holder.titleTv.setText(mCursor.getString(ArticleQuery.TITLE));
            holder.descriptionTv.setText(mCursor.getString(ArticleQuery.DESCRIPTION));
            String urlToImage = mCursor.getString(ArticleQuery.URL_TO_IMAGE);
            if(urlToImage == null || urlToImage.equals("")){
                holder.imageView.setMaxHeight(0);
            } else Glide.with(mContext).load(urlToImage).into(holder.imageView);
        }

    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}
