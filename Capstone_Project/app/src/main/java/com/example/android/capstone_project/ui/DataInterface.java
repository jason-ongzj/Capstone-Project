package com.example.android.capstone_project.ui;

import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.android.capstone_project.others.NetworkChangeReceiver;

interface DataInterface {
    void onItemClicked(String source, String category);

    ListView getListView();
    int getListViewSelected();
    void updateListViewSelected(int position);

    String getSourceName();
    Spinner getSpinner();
    String getSpinnerSelection();

    NetworkChangeReceiver getNetworkChangeReceiver();
    boolean isNetworkChangeReceiverSet();
    void setNetworkChangeReceiverFalse();

    ActionBarDrawerToggle getToggle();
    void closeDrawer();

    void updateRotationStatus();
    boolean getRotationStatus();

    MenuItem getRefreshListButton();
    boolean getRefreshStatus();
    void setRefreshStatusFalse();

    void setSyncFinished();

    void hideProgressBar();




}
