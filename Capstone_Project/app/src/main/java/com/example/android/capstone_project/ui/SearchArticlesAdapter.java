package com.example.android.capstone_project.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.capstone_project.R;
import com.example.android.capstone_project.data.ArticleQuery;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

// Adapter class can be reused for MainActivity as well as SearchActivity

public class SearchArticlesAdapter extends
        RecyclerView.Adapter<SearchArticlesAdapter.SearchArticlesAdapterViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    private int MAIN_ACTIVITY = 0;
    private int SEARCH_ACTIVITY = 1;

    private int WIDGET_PROVIDER = 100;

    private int activity_id;

    public SearchArticlesAdapter(Context context, int id){
        mContext = context;
        activity_id = id;
    }

    public void setCursor(Cursor cursor){
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public class SearchArticlesAdapterViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.card_display)
        View card_display;
        @BindView(R.id.card_display_title)
        TextView titleTv;
        @BindView(R.id.card_display_description)
        TextView descriptionTv;
        @BindView(R.id.card_display_image)
        ImageView imageView;

        public SearchArticlesAdapterViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.card_display)
        public void onClick(){
            Intent intent = new Intent(mContext, WebViewActivity.class);
            mCursor.moveToPosition(getAdapterPosition());
            String url = "";
            if(activity_id == MAIN_ACTIVITY)
                url = mCursor.getString(ArticleQuery.URL);
            else
                url = mCursor.getString(ArticleQuery.SEARCH_URL);
            intent.putExtra("URL", url);
            intent.setAction("Search");
            mContext.startActivity(intent);
        }
    }

    @Override
    public SearchArticlesAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_display, parent, false);
        return new SearchArticlesAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SearchArticlesAdapterViewHolder holder, int position) {
        String urlToImage = "";
        if(mCursor != null) {
            mCursor.moveToPosition(position);
            if(activity_id == MAIN_ACTIVITY) {
                holder.titleTv.setText(mCursor.getString(ArticleQuery.TITLE));
                holder.descriptionTv.setText(mCursor.getString(ArticleQuery.DESCRIPTION));
                urlToImage = mCursor.getString(ArticleQuery.URL_TO_IMAGE);
            } else {
                holder.titleTv.setText(mCursor.getString(ArticleQuery.SEARCH_TITLE));
                holder.descriptionTv.setText(mCursor.getString(ArticleQuery.SEARCH_DESCRIPTION));
                urlToImage = mCursor.getString(ArticleQuery.SEARCH_URL_TO_IMAGE);
            }
            if (urlToImage == null || urlToImage.equals("")) {
                holder.imageView.setMaxHeight(0);
            } else Glide.with(mContext).load(urlToImage).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }
}
