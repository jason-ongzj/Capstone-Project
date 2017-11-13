package com.example.android.capstone_project.ui.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.android.capstone_project.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ApiNewsStandWidgetConfigureActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "com.example.android.capstone_project.ui.widget.ApiNewsStandWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = ApiNewsStandWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            setResult(RESULT_CANCELED);
            saveCategoryPref(context, mAppWidgetId);
            saveSortByPref(context, mAppWidgetId);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ApiNewsStandWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, ApiNewsStandWidget.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listView);

            finish();
        }
    };

    @BindView(R.id.add_button)
    Button mAddButton;
    @BindView(R.id.radioGroup_category)
    RadioGroup mCategory;
    @BindView(R.id.radioGroup_sortBy)
    RadioGroup mSortBy;

    public ApiNewsStandWidgetConfigureActivity() {
        super();
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    private void saveCategoryPref(Context context, int appWidgetId){
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        RadioButton mRadioButtonCategory = (RadioButton) findViewById(mCategory.getCheckedRadioButtonId());
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + " Category", mRadioButtonCategory.getText().toString());
        prefs.apply();
    }

    public static String loadCategoryPref(Context context, int appWidgetId){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String categoryValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId + " Category", null);
        return categoryValue;
    }

    private void saveSortByPref(Context context, int appWidgetId){
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        RadioButton mRadioButtonSortBy = (RadioButton) findViewById(mSortBy.getCheckedRadioButtonId());
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + " SortBy", mRadioButtonSortBy.getText().toString());
        prefs.apply();
    }

    public static String loadSortByPref(Context context, int appWidgetId){
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String sortByValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId + " SortBy", null);
        return sortByValue;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.api_news_stand_widget_configure);
        ButterKnife.bind(this);
        mAddButton.setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }
}

