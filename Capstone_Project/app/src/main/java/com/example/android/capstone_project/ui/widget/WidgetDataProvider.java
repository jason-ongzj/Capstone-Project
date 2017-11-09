package com.example.android.capstone_project.ui.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.example.android.capstone_project.R;
import com.example.android.capstone_project.data.ArticleQuery;
import com.example.android.capstone_project.ui.SearchActivity;

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    private Cursor mCursor;
    private Context mContext;

    public static final String TAG = "WidgetDataProvider";

    public WidgetDataProvider(Context context, Cursor cursor, int widgetId){
        mCursor = cursor;
        mContext = context;
    }

    @Override
    public void onCreate() {
        mCursor.moveToFirst();
    }

    @Override
    public void onDataSetChanged() {
        Log.d(TAG, "onDataSetChanged: ");
    }

    @Override
    public void onDestroy() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        return mCursor.getCount() < 10 ? mCursor.getCount() : 10;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        mCursor.moveToPosition(position);
        RemoteViews view = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_item_display);
        String title = mCursor.getString(ArticleQuery.TITLE);
        String description = mCursor.getString(ArticleQuery.DESCRIPTION);
        String imageUrl = mCursor.getString(ArticleQuery.URL_TO_IMAGE);
        String url = mCursor.getString(ArticleQuery.URL);

        try {
            Bitmap bitmap = Glide.with(mContext).load(imageUrl).asBitmap().into(150, 150).get();
            view.setImageViewBitmap(R.id.widget_display_image, bitmap);
            view.setTextViewText(R.id.widget_display_title, title);
            view.setTextViewText(R.id.widget_display_description, description);

            Intent intent = new Intent(mContext, SearchActivity.class);
            intent.putExtra("URL", url);
            intent.setAction("Browse");
            view.setOnClickFillInIntent(R.id.widget_display, intent);

            return view;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
