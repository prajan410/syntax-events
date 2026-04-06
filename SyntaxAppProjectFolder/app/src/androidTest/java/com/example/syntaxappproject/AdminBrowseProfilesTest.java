package com.example.syntaxappproject;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.Bundle;

/**
 * Instrumented tests for {@link AdminBrowseProfiles} fragment.
 * Verifies UI display and navigation functionality.
 */
@RunWith(AndroidJUnit4.class)
public class AdminBrowseProfilesTest {

    @Mock
    private NavController mockNavController;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test screen display
     */
    @Test
    public void testAdminBrowseProfilesScreenDisplays() {
        FragmentScenario<AdminBrowseProfiles> scenario = FragmentScenario.launchInContainer(
                AdminBrowseProfiles.class,
                (Bundle) null,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        onView(withId(R.id.headerTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.profileCountBadge)).check(matches(isDisplayed()));
        onView(withId(R.id.doneButton)).check(matches(isDisplayed()));
        onView(withId(R.id.loadingSpinner)).check(matches(isDisplayed()));
    }

    /**
     * Test navigation of done button
     */
    @Test
    public void testDoneButtonNavigatesBack() {
        FragmentScenario<AdminBrowseProfiles> scenario = FragmentScenario.launchInContainer(
                AdminBrowseProfiles.class,
                (Bundle) null,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        scenario.onFragment(fragment -> {
            mockNavController = mock(NavController.class);
            Navigation.setViewNavController(fragment.requireView(), mockNavController);
        });

        onView(withId(R.id.doneButton)).perform(click());

        scenario.onFragment(fragment -> {
            verify(mockNavController).navigate(R.id.adminFragment);
        });
    }
}