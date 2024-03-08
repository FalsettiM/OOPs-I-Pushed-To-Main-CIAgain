package com.oopsipushedtomain;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import com.oopsipushedtomain.AdminActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminActivityTest {

    @Test
    public void ensureAdminDashboardFragmentIsDisplayed() {
        // Launch the AdminActivity
        ActivityScenario.launch(AdminActivity.class);

        // Check if the "Browse Events" button is displayed
        Espresso.onView(ViewMatchers.withId(R.id.btnBrowseEvents))
                .check(matches(isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.btnBrowseProfiles))
                .check(matches(isDisplayed()));

        Espresso.onView(ViewMatchers.withId(R.id.btnBrowseImages))
                .check(matches(isDisplayed()));

    }
}
