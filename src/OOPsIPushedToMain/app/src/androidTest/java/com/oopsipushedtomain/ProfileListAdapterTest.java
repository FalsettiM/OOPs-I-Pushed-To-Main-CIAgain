package com.oopsipushedtomain;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
@MediumTest
public class ProfileListAdapterTest {

    // Prepare your mock data
    private List<Profile> mockProfileList = new ArrayList<>();
    public void setUp() {
        // Initialize your activity with the RecyclerView
        ActivityScenario<ProfileListActivity> scenario = ActivityScenario.launch(ProfileListActivity.class);

        // Prepare your mock data here
        List<Profile> mockProfileList = new ArrayList<>();
        mockProfileList.add(new Profile("1", "John Doe", "Johnny", "1990-01-01", "http://john.doe", "123 Main St", "555-1234", "john@example.com"));

        // Use onActivity to interact with the activity
        scenario.onActivity(activity -> {
            // Here, you directly use the activity instance
            ProfileListAdapter adapter = new ProfileListAdapter(activity, mockProfileList);
            activity.profilesRecyclerView.setAdapter(adapter);
        });
    }

    @Test
    public void profileNameDisplaysCorrectly() {
        // Scroll to the position that needs to be matched and click on it.
        Espresso.onView(ViewMatchers.withId(R.id.profilesRecyclerView))
                .perform(RecyclerViewActions.scrollToPosition(0));

        // Check if the item at position 0 has the correct name
        Espresso.onView(withText("John Doe")).check(matches(isDisplayed()));
    }
}

