package com.example.syntaxappproject.ui;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import androidx.lifecycle.ViewModelProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.EventFilterViewModel;
import com.example.syntaxappproject.MainActivity;
import com.example.syntaxappproject.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumentation tests for {@link ManageEventFragment.FilterDialogFragment}.
 *
 * <p>This test verifies that:
 * - The dialog is displayed correctly
 * - User input is accepted
 * - Apply updates the ViewModel
 * - Clear resets the filters</p>
 */
@RunWith(AndroidJUnit4.class)
public class FilterDialogFragmentTest {

    /**
     * Activity scenario to host the dialog (needed for ViewModel scope).
     */
    private ActivityScenario<MainActivity> scenario;

    /**
     * Set up the test environment before each test.
     */
    @Before
    public void setup() {
        scenario = ActivityScenario.launch(MainActivity.class);

        scenario.onActivity(activity -> {
            ManageEventFragment.FilterDialogFragment dialog = new ManageEventFragment.FilterDialogFragment();
            dialog.show(activity.getSupportFragmentManager(), "filterDialog");
        });
    }

    /**
     * Test that the dialog and all input fields are displayed.
     */
    @Test
    public void testDialog_isDisplayedCorrectly() {

        onView(withText("Filter Events"))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.startDateFilter))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.endDateFilter))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withId(R.id.capacityFilter))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    /**
     * Test that entering values and clicking Apply updates the ViewModel.
     */
    @Test
    public void testApplyFilters_updatesViewModel() {

        onView(withId(R.id.startDateFilter))
                .inRoot(isDialog())
                .perform(replaceText("2026-04-01"), closeSoftKeyboard());

        onView(withId(R.id.endDateFilter))
                .inRoot(isDialog())
                .perform(replaceText("2026-04-02"), closeSoftKeyboard());

        onView(withId(R.id.capacityFilter))
                .inRoot(isDialog())
                .perform(typeText("50"), closeSoftKeyboard());

        onView(withText("Apply"))
                .inRoot(isDialog())
                .perform(click());

        scenario.onActivity(activity -> {
            EventFilterViewModel vm =
                    new ViewModelProvider(activity).get(EventFilterViewModel.class);

            assert("2026-04-01".equals(vm.getStartValue()));
            assert("2026-04-02".equals(vm.getEndValue()));
            assert(vm.getCapacityValue() == 50L);
        });
    }

    /**
     * Test that clicking Clear resets all filters in the ViewModel.
     */
    @Test
    public void testClearFilters_resetsViewModel() {

        scenario.onActivity(activity -> {
            EventFilterViewModel vm =
                    new ViewModelProvider(activity).get(EventFilterViewModel.class);

            vm.setFilters("2026-01-01", "2026-01-02", 100);
        });

        onView(withText("Clear"))
                .inRoot(isDialog())
                .perform(click());

        scenario.onActivity(activity -> {
            EventFilterViewModel vm =
                    new ViewModelProvider(activity).get(EventFilterViewModel.class);

            assert(vm.getStartValue() == null);
            assert(vm.getEndValue() == null);
            assert(vm.getCapacityValue() == -1L);
        });
    }
}
