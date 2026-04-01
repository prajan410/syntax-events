package com.example.syntaxappproject.ui;

import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented test class for {@link EventSignupListFragment}.
 * This test verifies that the signup list correctly displays entrant names
 * fetched from Firebase when an event ID is provided.
 */
@RunWith(AndroidJUnit4.class)
public class SignupListTest {

    /**
     * Tests the content of the Event Signup List.
     * It launches the fragment with a test event ID, expands all entrant categories
     * (Final, Invited, Waiting List, Cancelled), and asserts that specific expected
     * names are visible in the UI after loading.
     *
     * @throws InterruptedException if the thread sleep is interrupted.
     */
    @Test
    public void testEventSignupListContent() throws InterruptedException {
        // Arguments for the fragment
        Bundle bundle = new Bundle();
        bundle.putString("eventId", "testevent2");

        // Launch the fragment in a container
        FragmentScenario<EventSignupListFragment> scenario = FragmentScenario.launchInContainer(
                EventSignupListFragment.class,
                bundle,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        // Expand all categories by clicking their titles
        onView(withId(R.id.finalEntrantsTitle)).perform(ViewActions.click());
        onView(withId(R.id.invitedEntrantsTitle)).perform(ViewActions.scrollTo(), ViewActions.click());
        onView(withId(R.id.waitingListTitle)).perform(ViewActions.scrollTo(), ViewActions.click());
        onView(withId(R.id.cancelledEntrantsTitle)).perform(ViewActions.scrollTo(), ViewActions.click());

        // Wait for data to load from Firebase.
        // In a production environment, IdlingResource should be used instead of Thread.sleep.
        Thread.sleep(5000);

        // Check if the expected names are displayed in the list
        onView(withText("Willam Li")).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withText("fahhh mi bombo")).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
        onView(withText("Joe Doe")).perform(ViewActions.scrollTo()).check(matches(isDisplayed()));
    }
}
