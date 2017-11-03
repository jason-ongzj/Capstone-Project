package com.example.android.capstone_project.ui;

import android.widget.ListView;

interface DataInterface {
    ListView getListView();
    void closeDrawer();
    void updateFragments(String source);
    void onSourceItemClicked(String source, String category);
}
