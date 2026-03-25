package com.example.syntaxappproject;

import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.mock;

/**
 * Instrumented tests for {@link AdminEventDetails} fragment.
 * Verifies that event details are correctly displayed from bundle arguments
 * and that UI elements are properly rendered.
 */
@RunWith(AndroidJUnit4.class)
public class AdminEventDetailsTest {

    @Mock
    private NavController mockNavController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Tests that the event details screen correctly displays all event information
     * passed via bundle arguments. Verifies event title, description, organizer,
     * location, dates, registration period, capacity, and UI buttons.
     */
    @Test
    public void testEventDetailsScreenDisplays() {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", "testevent2");
        bundle.putString("name", "Swimming Lessons");
        bundle.putString("description", "Beginner swimming lessons");
        bundle.putString("location", "Edmonton");
        bundle.putString("organizerUid", "organizer123");
        bundle.putString("startingEventDate", "2024-01-15");
        bundle.putString("endingEventDate", "2024-01-20");
        bundle.putString("startingRegistrationPeriod", "2024-01-01");
        bundle.putString("endingRegistrationPeriod", "2024-01-10");
        bundle.putLong("capacity", 50);

        FragmentScenario<AdminEventDetails> scenario = FragmentScenario.launchInContainer(
                AdminEventDetails.class,
                bundle,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        scenario.onFragment(fragment -> {
            mockNavController = mock(NavController.class);
            Navigation.setViewNavController(fragment.requireView(), mockNavController);
        });

        onView(withId(R.id.tv_detail_event_title)).check(matches(withText("Swimming Lessons")));
        onView(withId(R.id.tv_detail_event_description)).check(matches(withText("Beginner swimming lessons")));
        onView(withId(R.id.tv_detail_event_organizer)).check(matches(withText("Organizer UID: organizer123")));
        onView(withId(R.id.tv_detail_event_location)).check(matches(withText("Edmonton")));
        onView(withId(R.id.tv_detail_event_dates)).check(matches(withText("2024-01-15 → 2024-01-20")));
        onView(withId(R.id.tv_detail_event_reg)).check(matches(withText("2024-01-01 → 2024-01-10")));
        onView(withId(R.id.tv_detail_event_capacity)).check(matches(withText("50")));

        onView(withId(R.id.doneButton)).check(matches(isDisplayed()));

        onView(withId(R.id.btn_remove_event)).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
    }
}