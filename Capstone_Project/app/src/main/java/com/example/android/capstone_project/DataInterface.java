package com.example.android.capstone_project;

import android.view.Menu;
import android.widget.ListView;

interface DataInterface {
    public Menu getMenu();
    public ListView getListView();
    public void closeDrawer();
}
