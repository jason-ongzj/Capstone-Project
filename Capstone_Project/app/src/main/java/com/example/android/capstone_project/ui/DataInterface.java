package com.example.android.capstone_project.ui;

import android.widget.ListView;

interface DataInterface {
    public ListView getListView();
    public void closeDrawer();
    public void updateFragments(String source);
    public void onSourceItemClicked(String source, String category);
}
