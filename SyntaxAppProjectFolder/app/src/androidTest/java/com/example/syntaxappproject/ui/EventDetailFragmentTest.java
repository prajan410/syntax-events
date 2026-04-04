package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented UI tests for {@link EventDetailFragment}.
 *
 * Uses {@code testingMode = true} to bypass Firestore, Firebase, and
 * NavController dependencies so tests run in isolation without network calls.
 *
 * These tests use direct fragment view access instead of Espresso matchers
 * to avoid Hamcrest dependency issues.
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
     */
    @Test
    public void testViewsAreVisible() {
        FragmentScenario<EventDetailFragment> scenario = launch();

        scenario.onFragment(fragment -> {
            assertEquals(View.VISIBLE, fragment.requireView().findViewById(R.id.eventPoster).getVisibility());
            assertEquals(View.VISIBLE, fragment.requireView().findViewById(R.id.eventQRCode).getVisibility());
            assertEquals(View.VISIBLE, fragment.requireView().findViewById(R.id.joinButton).getVisibility());
            assertEquals(View.VISIBLE, fragment.requireView().findViewById(R.id.doneButton).getVisibility());
        });
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
                        ((TextView) fragment.requireView().findViewById(R.id.eventWLCount))
                                .setText("Waitlist: 3")
                )
        );

        scenario.onFragment(fragment -> {
            Button joinButton = fragment.requireView().findViewById(R.id.joinButton);
            TextView wlCount = fragment.requireView().findViewById(R.id.eventWLCount);

            assertEquals("Join", joinButton.getText().toString());
            assertEquals("Waitlist: 3", wlCount.getText().toString());

            joinButton.performClick();

            assertEquals("Leave", joinButton.getText().toString());
            assertEquals("Waitlist: 4", wlCount.getText().toString());
        });
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
                        ((TextView) fragment.requireView().findViewById(R.id.eventWLCount))
                                .setText("Waitlist: 5")
                )
        );

        scenario.onFragment(fragment -> {
            Button joinButton = fragment.requireView().findViewById(R.id.joinButton);
            TextView wlCount = fragment.requireView().findViewById(R.id.eventWLCount);

            joinButton.performClick();
            assertEquals("Leave", joinButton.getText().toString());
            assertEquals("Waitlist: 6", wlCount.getText().toString());

            joinButton.performClick();
            assertEquals("Join", joinButton.getText().toString());
            assertEquals("Waitlist: 5", wlCount.getText().toString());
        });
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
                        ((TextView) fragment.requireView().findViewById(R.id.eventWLCount))
                                .setText("Waitlist: 2")
                )
        );

        scenario.onFragment(fragment -> {
            Button joinButton = fragment.requireView().findViewById(R.id.joinButton);
            TextView wlCount = fragment.requireView().findViewById(R.id.eventWLCount);

            joinButton.performClick();
            assertEquals("Leave", joinButton.getText().toString());
            assertEquals("Waitlist: 3", wlCount.getText().toString());

            joinButton.performClick();
            assertEquals("Join", joinButton.getText().toString());
            assertEquals("Waitlist: 2", wlCount.getText().toString());

            joinButton.performClick();
            assertEquals("Leave", joinButton.getText().toString());
            assertEquals("Waitlist: 3", wlCount.getText().toString());
        });
    }
}