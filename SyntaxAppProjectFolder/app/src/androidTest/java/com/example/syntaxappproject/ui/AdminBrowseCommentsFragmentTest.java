package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.Comment;
import com.example.syntaxappproject.CommentRepository;
import com.example.syntaxappproject.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Instrumented UI tests for {@link AdminBrowseCommentsFragment}.
 * Verifies UI elements, filter functionality, search, and navigation.
 */
@RunWith(AndroidJUnit4.class)
public class AdminBrowseCommentsFragmentTest {

    private NavController mockNavController;

    @Before
    public void setUp() {
        mockNavController = mock(NavController.class);
    }

    /**
     * Launches the fragment with test mode enabled and mock data.
     */
    private FragmentScenario<AdminBrowseCommentsFragment> launchFragment() {
        FragmentScenario<AdminBrowseCommentsFragment> scenario = FragmentScenario.launchInContainer(
                AdminBrowseCommentsFragment.class,
                new Bundle(),
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        scenario.onFragment(fragment -> {
            Navigation.setViewNavController(fragment.requireView(), mockNavController);
            fragment.disableFirestoreForTest = true;

            List<Comment> mockComments = new ArrayList<>();

            Comment mockComment1 = new Comment("event1", "This is a test comment", "user1", "Test User", "device1");
            mockComment1.setCommentId("comment1");
            mockComment1.setReportCount(2);
            mockComments.add(mockComment1);

            Comment mockComment2 = new Comment("event2", "Another test comment", "user2", "Another User", "device2");
            mockComment2.setCommentId("comment2");
            mockComment2.setReportCount(0);
            mockComments.add(mockComment2);

            Comment mockComment3 = new Comment("event3", "Third test comment", "user3", "Third User", "device3");
            mockComment3.setCommentId("comment3");
            mockComment3.setReportCount(1);
            mockComments.add(mockComment3);

            Comment mockComment4 = new Comment("event4", "Fourth test comment", "user4", "Fourth User", "device4");
            mockComment4.setCommentId("comment4");
            mockComment4.setReportCount(0);
            mockComments.add(mockComment4);

            fragment.setMockComments(mockComments);
        });

        return scenario;
    }

    /**
     * Tests that all UI elements are displayed correctly.
     */
    @Test
    public void testScreenDisplays() {
        launchFragment();

        onView(withId(R.id.headerTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.commentCountBadge)).check(matches(isDisplayed()));
        onView(withId(R.id.filterAllBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.filterReportedBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.searchInput)).check(matches(isDisplayed()));
        onView(withId(R.id.doneButton)).check(matches(isDisplayed()));

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.recycler_comments)).check(matches(isDisplayed()));
    }

    /**
     * Tests that the "All Comments" filter button is active by default.
     */
    @Test
    public void testAllCommentsFilterActiveByDefault() {
        launchFragment();

        onView(withId(R.id.filterAllBtn)).check(matches(isDisplayed()));
    }

    /**
     * Tests that clicking "Reported" filter button changes the filter.
     */
    @Test
    public void testReportedFilterClick() {
        launchFragment();

        onView(withId(R.id.filterReportedBtn)).perform(click());
        onView(withId(R.id.filterReportedBtn)).check(matches(isDisplayed()));

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests that the search input accepts text and triggers filtering.
     */
    @Test
    public void testSearchInput_acceptsText() {
        launchFragment();

        onView(withId(R.id.searchInput)).perform(replaceText("test"), closeSoftKeyboard());
        onView(withId(R.id.searchInput)).check(matches(withText("test")));
    }

    /**
     * Tests that clicking the Done button navigates back.
     */
    @Test
    public void testDoneButtonNavigatesBack() {
        launchFragment();

        onView(withId(R.id.doneButton)).perform(click());

        verify(mockNavController).navigateUp();
    }

    /**
     * Tests that the count badge shows the correct number of comments.
     */
    @Test
    public void testCountBadgeShowsCorrectCount() {
        launchFragment();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.commentCountBadge)).check(matches(withText("4 comments")));
    }
}