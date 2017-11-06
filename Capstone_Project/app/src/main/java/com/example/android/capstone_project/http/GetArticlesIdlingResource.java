package com.example.android.capstone_project.http;

import android.support.annotation.Nullable;
import android.support.test.espresso.IdlingResource;

import com.example.android.capstone_project.ui.MainActivity;

public class GetArticlesIdlingResource implements IdlingResource {

    @Nullable
    private volatile ResourceCallback mCallback;

    private MainActivity activity;
    // Idleness is controlled with this boolean.
//    private AtomicBoolean mIsIdleNow = new AtomicBoolean(false);

    public GetArticlesIdlingResource(MainActivity activity){
        this.activity = activity;
    }

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public boolean isIdleNow() {
        boolean idle = isIdle();
        if(idle) mCallback.onTransitionToIdle();
        return idle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        mCallback = callback;
    }

    public boolean isIdle(){
        return activity != null && mCallback != null && activity.isSyncFinished();
    }
}