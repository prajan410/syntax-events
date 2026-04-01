package com.example.syntaxappproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented UI tests for AdminBrowseEvents.
 * avoid Espresso input/click injection on API 36
 */
@RunWith(AndroidJUnit4.class)
public class AdminBrowseEventsTest {

    private NavController mockNavController;

    @Before
    public void setUp() {
        mockNavController = mock(NavController.class);
    }

    private FragmentScenario<AdminBrowseEvents> launchFragment() {
        FragmentScenario<AdminBrowseEvents> scenario = FragmentScenario.launchInContainer(
                AdminBrowseEvents.class,
                new Bundle(),
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        scenario.onFragment(fragment ->
                Navigation.setViewNavController(fragment.requireView(), mockNavController)
        );

        return scenario;
    }

    @Test
    public void testScreenDisplays() {
        FragmentScenario<AdminBrowseEvents> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            View root = fragment.requireView();

            assertNotNull(root.findViewById(R.id.headerTitle));
            assertNotNull(root.findViewById(R.id.eventCountBadge));
            assertNotNull(root.findViewById(R.id.searchInput));
            assertNotNull(root.findViewById(R.id.doneButton));
            assertNotNull(root.findViewById(R.id.loadingSpinner));

            assertEquals(View.VISIBLE, root.findViewById(R.id.loadingSpinner).getVisibility());
        });
    }

    @Test
    public void testDoneButtonNavigatesBack() {
        FragmentScenario<AdminBrowseEvents> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            View doneButton = fragment.requireView().findViewById(R.id.doneButton);
            doneButton.performClick();
        });

        verify(mockNavController).navigate(R.id.adminFragment);
    }

    @Test
    public void testSearchWithNoMatchesShowsEmptyState() {
        FragmentScenario<AdminBrowseEvents> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            View root = fragment.requireView();
            EditText searchInput = root.findViewById(R.id.searchInput);
            TextView emptyText = root.findViewById(R.id.emptyText);
            TextView countBadge = root.findViewById(R.id.eventCountBadge);

            searchInput.setText("zzz");

            assertEquals(View.VISIBLE, emptyText.getVisibility());
            assertEquals("No matching events", emptyText.getText().toString());
            assertEquals("0 events", countBadge.getText().toString());
        });
    }
}