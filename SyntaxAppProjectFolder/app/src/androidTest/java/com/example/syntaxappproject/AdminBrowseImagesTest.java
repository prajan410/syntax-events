package com.example.syntaxappproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented UI tests for AdminBrowseImages
 * avoid Espresso input/click injection on API 36
 */
@RunWith(AndroidJUnit4.class)
public class AdminBrowseImagesTest {

    private NavController mockNavController;

    @Before
    public void setUp() {
        mockNavController = mock(NavController.class);
        ImageCacheManager.clear();
    }

    @After
    public void tearDown() {
        ImageCacheManager.clear();
    }

    private FragmentScenario<AdminBrowseImages> launchFragment() {
        FragmentScenario<AdminBrowseImages> scenario = FragmentScenario.launchInContainer(
                AdminBrowseImages.class,
                new Bundle(),
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        scenario.onFragment(fragment ->
                Navigation.setViewNavController(fragment.requireView(), mockNavController)
        );

        return scenario;
    }

    /**
     * Test display of screen
     */
    @Test
    public void testScreenDisplays() {
        FragmentScenario<AdminBrowseImages> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            View root = fragment.requireView();

            assertNotNull(root.findViewById(R.id.headerTitle));
            assertNotNull(root.findViewById(R.id.imageCountBadge));
            assertNotNull(root.findViewById(R.id.recycler_images));
            assertNotNull(root.findViewById(R.id.emptyText));
            assertNotNull(root.findViewById(R.id.doneButton));
        });
    }

    /**
     * Test empty state display
     */
    @Test
    public void testEmptyCacheShowsEmptyState() {
        FragmentScenario<AdminBrowseImages> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            View root = fragment.requireView();
            TextView emptyText = root.findViewById(R.id.emptyText);
            TextView countBadge = root.findViewById(R.id.imageCountBadge);
            RecyclerView recyclerView = root.findViewById(R.id.recycler_images);

            assertEquals(View.VISIBLE, emptyText.getVisibility());
            assertEquals("No images", emptyText.getText().toString());
            assertEquals("0 posters", countBadge.getText().toString());
            assertEquals(View.GONE, recyclerView.getVisibility());
        });
    }

    /**
     * Test display of image recycler
     */
    @Test
    public void testCachedImagesShowRecyclerAndCount() {
        Bitmap bitmap = Bitmap.createBitmap(20, 20, Bitmap.Config.ARGB_8888);
        ImageCacheManager.put("event-001", bitmap);

        FragmentScenario<AdminBrowseImages> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            View root = fragment.requireView();
            TextView countBadge = root.findViewById(R.id.imageCountBadge);
            TextView emptyText = root.findViewById(R.id.emptyText);
            RecyclerView recyclerView = root.findViewById(R.id.recycler_images);

            assertEquals("1 poster", countBadge.getText().toString());
            assertEquals(View.GONE, emptyText.getVisibility());
            assertEquals(View.VISIBLE, recyclerView.getVisibility());
            assertEquals(1, recyclerView.getAdapter().getItemCount());
        });
    }

    /**
     * Test navigate back with done button
     */
    @Test
    public void testDoneButtonNavigatesBack() {
        FragmentScenario<AdminBrowseImages> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            View doneButton = fragment.requireView().findViewById(R.id.doneButton);
            doneButton.performClick();
        });

        verify(mockNavController).navigate(R.id.adminFragment);
    }
}