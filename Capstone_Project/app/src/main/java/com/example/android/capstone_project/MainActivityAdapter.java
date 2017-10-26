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

import com.bumptech.glide.Glide;
import com.example.android.capstone_project.data.ArticleQuery;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivityAdapter extends
        RecyclerView.Adapter<MainActivityAdapter.MainActivityAdapterViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    private int count = 10;


    public void setContext(Context context) {
        mContext = context;
    }

    public void setCursor(Cursor cursor){
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public class MainActivityAdapterViewHolder extends RecyclerView.ViewHolder{

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
//            Log.d("MainActivityAdapter", "onBindViewHolder: " + mCursor.getString(ArticleQuery.AUTHOR));
            holder.titleTv.setText(mCursor.getString(ArticleQuery.TITLE));
            holder.descriptionTv.setText(mCursor.getString(ArticleQuery.DESCRIPTION));
            String urlToImage = mCursor.getString(ArticleQuery.URL_TO_IMAGE);
//            if( !urlToImage.equals("") && urlToImage!=null)
//                Picasso.with(mContext).load(mCursor.getString(ArticleQuery.URL_TO_IMAGE)).into(holder.imageView);
                Glide.with(mContext).load(mCursor.getString(ArticleQuery.URL_TO_IMAGE)).into(holder.imageView);
//            else holder.imageView.setMaxHeight(0);
        }
        if(position == count - 1){
            count += 10;
            notifyDataSetChanged();
            Log.d("MainActivityAdapter", "onBindViewHolder: " + mCursor.getCount());
        }
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public void setItemCount(){
        count += 10;
    }
}
