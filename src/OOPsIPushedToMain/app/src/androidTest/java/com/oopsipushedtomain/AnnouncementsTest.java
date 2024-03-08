package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AnnouncementsTest {

    /**
     * Tests all announcement functionalities by sending an announcement and checking that the
     * announcement appears in the AnnouncementListActivity
     * @throws InterruptedException For Thread.sleep()
     */
    @Test
    public void testAnnouncements() throws InterruptedException {
        // Launch EventListActivity
        Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), EventListActivity.class);
        ActivityScenario.launch(i).onActivity(activity -> {
        });

        // Open an event
        Thread.sleep(2000);  // Wait for EventList to populate
        onData(anything()).inAdapterView(withId(R.id.EventListView)).atPosition(0).perform(click());

        // Send an announcement
        String toType = "UI Test Notification " + (Math.random() * 10 + 1);
        onView(withId(R.id.btnSendNotification)).perform(click());
        onView(withId(R.id.announcement_title_e)).perform(ViewActions.typeText(toType));
        onView(withId(R.id.announcement_body_e)).perform(ViewActions.typeText("This is a test notification"));
        Espresso.closeSoftKeyboard();
        onView(withId(R.id.btnSendNotification)).perform(click());

        // Look for announcement
        onView(withId(R.id.btnViewAnnouncements)).perform(click());
        Thread.sleep(1000);
        onView(withText(toType)).check(matches(isDisplayed()));

    }
}
