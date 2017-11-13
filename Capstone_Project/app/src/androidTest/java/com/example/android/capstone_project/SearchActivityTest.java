package com.example.android.capstone_project;


import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.example.android.capstone_project.ui.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

// Test assumes wifi/mobile connection
@RunWith(AndroidJUnit4.class)
public class SearchActivityTest {

    private IdlingResource mIdlingResource;

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void registerIdlingResource(){
        mIdlingResource = mActivityTestRule.getActivity().getIdleResource();
        Espresso.registerIdlingResources(mIdlingResource);
    }

    @After
    public void unregisterIdlingResource(){
        Espresso.unregisterIdlingResources(mIdlingResource);
    }

    // Click search button after load, type in editText and search for relevant articles. Check that
    // recycler view does not return null, then click on the first article to view on URL.
    @Test
    public void searchListingTest() {
        onView(withId(R.id.search)).perform(click());

        onView(withId(R.id.editText)).perform(typeTextIntoFocusedView("Donald Trump"));
        onView(withId(R.id.editText)).perform(pressImeActionButton());
        onView(withId(R.id.search_recyclerView)).check(matches(isDisplayed()));

        onView(withId(R.id.search_recyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.webView)).check(matches(isDisplayed()));
    }
}
