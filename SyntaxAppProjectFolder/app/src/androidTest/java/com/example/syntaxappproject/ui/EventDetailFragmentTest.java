package com.example.syntaxappproject.ui;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented UI tests for {@link EventDetailFragment}.
 *
 * Uses {@code testingMode = true} to bypass Firestore, Firebase, and
 * NavController dependencies so tests run in isolation without network calls.
 */
@RunWith(AndroidJUnit4.class)
public class EventDetailFragmentTest {

    private Bundle args;

    /**
     * Prepares a {@link Bundle} with a test event ID and {@code testingMode} enabled,
     * shared across all test cases.
     */
    @Before
    public void setUp() {
        args = new Bundle();
        args.putString("eventId", "test_event_123");
        args.putBoolean("testingMode", true);
    }

    /**
     * Launches {@link EventDetailFragment} in a container using a Material theme
     * required by {@code MaterialCardView} and related Material components.
     *
     * @return the active {@link FragmentScenario} for the fragment under test
     */
    private FragmentScenario<EventDetailFragment> launch() {
        return FragmentScenario.launchInContainer(
                EventDetailFragment.class,
                args,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (androidx.fragment.app.FragmentFactory) null
        );
    }

    /**
     * Verifies that the poster, QR code, join button, and done button are
     * present and visible immediately after the fragment is created.
     * The done button uses an effective-visibility check since it may be
     * scrolled outside the test container's visible rectangle.
     */
    @Test
    public void testViewsAreVisible() {
        launch();

        onView(withId(R.id.eventPoster)).check(matches(isDisplayed()));
        onView(withId(R.id.eventQRCode)).check(matches(isDisplayed()));
        onView(withId(R.id.joinButton)).check(matches(isDisplayed()));
        onView(withId(R.id.doneButton)).check(matches(
                withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)
        ));
    }

    /**
     * Verifies that tapping the Join button changes its label to "Leave"
     * and increments the waitlist count by one.
     */
    @Test
    public void testJoinButtonTogglesLeave() {
        FragmentScenario<EventDetailFragment> scenario = launch();

        scenario.onFragment(fragment ->
                fragment.requireActivity().runOnUiThread(() ->
                        fragment.requireView()
                                .<android.widget.TextView>findViewById(R.id.eventWLCount)
                                .setText("Waitlist: 3")
                )
        );

        onView(withId(R.id.joinButton)).check(matches(withText("Join")));
        onView(withId(R.id.joinButton)).perform(click());
        onView(withId(R.id.joinButton)).check(matches(withText("Leave")));
        onView(withId(R.id.eventWLCount)).check(matches(withText("Waitlist: 4")));
    }

    /**
     * Verifies that tapping the Leave button after joining restores the label
     * to "Join" and decrements the waitlist count back to its original value.
     */
    @Test
    public void testLeaveButtonTogglesJoin() {
        FragmentScenario<EventDetailFragment> scenario = launch();

        scenario.onFragment(fragment ->
                fragment.requireActivity().runOnUiThread(() ->
                        fragment.requireView()
                                .<android.widget.TextView>findViewById(R.id.eventWLCount)
                                .setText("Waitlist: 5")
                )
        );

        onView(withId(R.id.joinButton)).perform(click());
        onView(withId(R.id.joinButton)).check(matches(withText("Leave")));

        onView(withId(R.id.joinButton)).perform(click());
        onView(withId(R.id.joinButton)).check(matches(withText("Join")));
        onView(withId(R.id.eventWLCount)).check(matches(withText("Waitlist: 5")));
    }

    /**
     * Verifies that the join/leave toggle and waitlist counter remain consistent
     * across three consecutive button taps.
     */
    @Test
    public void testMultipleToggles() {
        FragmentScenario<EventDetailFragment> scenario = launch();

        scenario.onFragment(fragment ->
                fragment.requireActivity().runOnUiThread(() ->
                        fragment.requireView()
                                .<android.widget.TextView>findViewById(R.id.eventWLCount)
                                .setText("Waitlist: 2")
                )
        );

        onView(withId(R.id.joinButton)).perform(click());
        onView(withId(R.id.joinButton)).check(matches(withText("Leave")));
        onView(withId(R.id.eventWLCount)).check(matches(withText("Waitlist: 3")));

        onView(withId(R.id.joinButton)).perform(click());
        onView(withId(R.id.joinButton)).check(matches(withText("Join")));
        onView(withId(R.id.eventWLCount)).check(matches(withText("Waitlist: 2")));

        onView(withId(R.id.joinButton)).perform(click());
        onView(withId(R.id.joinButton)).check(matches(withText("Leave")));
        onView(withId(R.id.eventWLCount)).check(matches(withText("Waitlist: 3")));
    }
}