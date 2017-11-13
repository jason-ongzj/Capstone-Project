package com.example.android.capstone_project;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;

import com.example.android.capstone_project.ui.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.typeTextIntoFocusedView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

// Test assumes wifi/mobile connection
public class SearchActivityBehaviourTest {

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

    // Click search button after load. Type in editText, then search for relevant articles. Check that
    // recyclerview does not return null.

    // Then click on editText again, ensure search history is displayed if there is at least one
    // previous entry. Perform a deletion of history, make sure delete history button does not appear.
    @Test
    public void searchBehaviourTest(){
        onView(withId(R.id.search)).perform(click());

        onView(withId(R.id.editText)).perform(typeTextIntoFocusedView("Donald Trump"));
        onView(withId(R.id.editText)).perform(pressImeActionButton());
        onView(withId(R.id.search_recyclerView)).check(matches(isDisplayed()));

        onView(withId(R.id.editText)).perform(click());
        onView(withId(R.id.search_history)).check(matches(isDisplayed()));

        onView(withId(R.id.delete_history)).perform(click());
        onView(withId(R.id.clear_history)).check(matches(not(isDisplayed())));
    }
}
