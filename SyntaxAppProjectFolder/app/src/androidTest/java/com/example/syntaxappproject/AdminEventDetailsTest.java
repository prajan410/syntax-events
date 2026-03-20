package com.example.syntaxappproject;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented UI tests for {@link AdminEventDetails}.
 *
 * <p>Verifies that event detail fields are correctly populated from a {@link Bundle}
 * and that the remove event button is visible on screen.</p>
 */
@RunWith(AndroidJUnit4.class)
public class AdminEventDetailsTest {

    /**
     * Verifies that launching {@link AdminEventDetails} with a populated {@link Bundle}
     * correctly displays the event title, description, location, and remove button.
     *
     * <p>The fragment is launched in isolation via {@link FragmentScenario} with
     * pre-set bundle arguments. Each field is asserted using its expected formatted
     * string (e.g. {@code "Title: Swimming Lessons"}).</p>
     */
    @Test
    public void testEventDetailsScreenDisplays() {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", "testevent2");
        bundle.putString("title", "Swimming Lessons");
        bundle.putString("description", "Beginner swimming lessons");
        bundle.putString("location", "Edmonton");
        bundle.putString("organizer", "organizer123");

        FragmentScenario.launchInContainer(AdminEventDetails.class, bundle);

        onView(withId(R.id.tv_detail_event_title)).check(matches(withText("Title: Swimming Lessons")));
        onView(withId(R.id.tv_detail_event_description)).check(matches(withText("Description: Beginner swimming lessons")));
        onView(withId(R.id.tv_detail_event_location)).check(matches(withText("Location: Edmonton")));
        onView(withId(R.id.btn_remove_event)).check(matches(isDisplayed()));
    }
}
