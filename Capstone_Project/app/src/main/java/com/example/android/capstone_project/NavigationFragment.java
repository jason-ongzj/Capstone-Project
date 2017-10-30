package com.example.android.capstone_project;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigationFragment extends Fragment{

    @Nullable
    @BindView(R.id.listView)
    ListView listView;

    private ArrayList<String> sourceList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_nav_display, container, false);
        ButterKnife.bind(this, mRootView);

        return mRootView;
    }

    public ListView getListView() {
        return listView;
    }

    public void setSourceList(ArrayList<String> articleSourceList){
        sourceList = articleSourceList;
    }
}
