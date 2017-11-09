package com.example.android.capstone_project.ui;

import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Spinner;

interface DataInterface {
    ListView getListView();
    void closeDrawer();
    void updateFragments(String source);
    void onSourceItemClicked(String source, String category);
    Spinner getSpinner();
    ActionBarDrawerToggle getToggle();
    MenuItem getRefreshListButton();
    void setSyncFinished();
    void setItemSelectedTrue();
}
