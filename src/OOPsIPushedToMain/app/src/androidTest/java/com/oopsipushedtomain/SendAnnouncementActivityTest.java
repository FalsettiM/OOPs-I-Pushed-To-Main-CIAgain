package com.oopsipushedtomain;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.oopsipushedtomain.Announcements.SendAnnouncementActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SendAnnouncementActivityTest {

    @Test
    public void testAnnouncements() {
        Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), SendAnnouncementActivity.class);
        i.putExtra("eventId", "EVNT-10BYFLTVS9RBMZESPDG5");
        ActivityScenario.launch(i).onActivity(activity -> {
            // Tests go here, which runs on UI-Thread.
            // Open UITest event

            // Send an announcement
            onView(withId(R.id.btnSendNotification)).perform(click());
            onView(withId(R.id.announcement_title_e)).perform(ViewActions.typeText("UI Test Notification"));
            onView(withId(R.id.announcement_body_e)).perform(ViewActions.typeText("This is a test notification"));
            Espresso.closeSoftKeyboard();
            onView(withId(R.id.btnSendNotification)).perform(click());

            // Look for announcement
            onView(withId(R.id.btnViewAnnouncements)).perform(click());
            onView(withText("UI Test Notification")).check(matches(isDisplayed()));
        });
        // Create new test event
//        onView(withId(R.id.eventsButton)).perform(click());
//        onView(withId(R.id.create_event_button)).perform(click());
//        onView(withId(R.id.event_details_organizer_title_e)).perform(ViewActions.typeText("UI Test Event"));
//        onView(withId(R.id.new_event_start_time_e)).perform(clearText(), typeText("01/01/2025 12:00"));
//        onView(withId(R.id.new_event_end_time_e)).perform(clearText(), typeText("01/01/2025 14:00"));
//        onView(withId(R.id.new_event_description_e)).perform(clearText(), typeText("This is a test event."));
//        Espresso.closeSoftKeyboard();
//        onView(withId(R.id.btnCreateNewEvent)).perform(click());
//        Espresso.pressBack();
//        onData(anything()).inAdapterView(withId(R.id.EventListView)).atPosition(0).perform(click());

    }
}
