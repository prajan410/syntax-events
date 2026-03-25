package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.Navigation;
import androidx.navigation.testing.TestNavHostController;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.EventDetail;
import com.example.syntaxappproject.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

/**
 * Espresso instrumentation tests for {@link EventHistoryFragment}.
 *
 * <p>This test verifies that selecting an event from the RecyclerView
 * correctly triggers navigation to the EventDetail screen.</p>
 *
 * <p>The test attaches a {@link TestNavHostController} so that navigation
 * actions inside the fragment can be captured and verified.</p>
 */
@RunWith(AndroidJUnit4.class)
public class EventHistoryFragmentTest {

    /** Fragment test scenario used to launch the fragment. */
    private FragmentScenario<EventHistoryFragment> scenario;

    /** Navigation controller used to verify navigation actions. */
    private TestNavHostController navController;

    /**
     * Sets up the fragment and attaches a TestNavHostController before each test.
     */
    @Before
    public void setup() {
        scenario = FragmentScenario.launchInContainer(
                EventHistoryFragment.class,
                new Bundle(),
                R.style.Theme_SyntaxAppProject
        );

        scenario.onFragment(fragment -> {
            fragment.disableFirestoreForTest = true;

            navController = new TestNavHostController(
                    ApplicationProvider.getApplicationContext());

            navController.setGraph(R.navigation.nav_graph);
            navController.setCurrentDestination(R.id.eventHistoryFragment);

            Navigation.setViewNavController(fragment.requireView(), navController);

            EventDetail fakeEvent = new EventDetail(
                    "testEvent123",
                    "Test Event",
                    "Test description",
                    "Location",
                    100, false,
                    "2026-03-01", "2026-03-02",
                    "2026-03-01", "2026-03-02",
                    0, "", "");

            fragment.setEventsForTest(Collections.singletonList(fakeEvent));
            try {
                java.lang.reflect.Field loadingSpinnerField =
                        EventHistoryFragment.class.getDeclaredField("loadingSpinner");
                loadingSpinnerField.setAccessible(true);
                View loadingSpinner = (View) loadingSpinnerField.get(fragment);
                loadingSpinner.setVisibility(View.GONE);

                java.lang.reflect.Field recyclerViewField =
                        EventHistoryFragment.class.getDeclaredField("recyclerView");
                recyclerViewField.setAccessible(true);
                RecyclerView recyclerView = (RecyclerView) recyclerViewField.get(fragment);
                recyclerView.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Tests that clicking an event in the RecyclerView triggers navigation
     * to the EventDetailFragment.
     */
    @Test
    public void clickingEvent_navigatesToEventDetail() {
        onView(withId(R.id.eventHistoryList))
                .check(matches(isDisplayed()));

        onView(withId(R.id.eventHistoryList))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        assert(navController.getCurrentDestination().getId()
                == R.id.eventDetailFragment);
    }
}