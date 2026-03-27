package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
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

import com.example.syntaxappproject.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented UI tests for {@link AdminCommentDetailsFragment}.
 * Verifies UI display, comment details population, and navigation actions.
 */
@RunWith(AndroidJUnit4.class)
public class AdminCommentDetailsFragmentTest {

    private NavController mockNavController;

    @Before
    public void setUp() {
        mockNavController = mock(NavController.class);
    }

    /**
     * Creates a bundle with test comment data.
     *
     * @return Bundle containing mock comment data
     */
    private Bundle createTestBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("commentId", "test-comment-123");
        bundle.putString("eventId", "test-event-456");
        bundle.putString("userId", "test-user-789");
        bundle.putString("userName", "Test User");
        bundle.putString("commentText", "This is a test comment for admin moderation.");
        bundle.putInt("reportCount", 3);
        bundle.putLong("timestamp", 1700000000L);
        return bundle;
    }

    /**
     * Launches the fragment with test data and mock NavController.
     *
     * @return FragmentScenario for the launched fragment
     */
    private FragmentScenario<AdminCommentDetailsFragment> launchFragment() {
        Bundle args = createTestBundle();

        FragmentScenario<AdminCommentDetailsFragment> scenario = FragmentScenario.launchInContainer(
                AdminCommentDetailsFragment.class,
                args,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        scenario.onFragment(fragment -> {
            Navigation.setViewNavController(fragment.requireView(), mockNavController);
        });

        return scenario;
    }

    /**
     * Tests that all UI elements are displayed correctly.
     * Elements inside ScrollView need scrollTo(), others don't.
     */
    @Test
    public void testScreenDisplays() {
        launchFragment();

        onView(withId(R.id.headerTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.commentUserName)).check(matches(isDisplayed()));
        onView(withId(R.id.commentTimestamp)).check(matches(isDisplayed()));
        onView(withId(R.id.commentText)).check(matches(isDisplayed()));
        onView(withId(R.id.reportCount)).check(matches(isDisplayed()));
        onView(withId(R.id.eventId)).check(matches(isDisplayed()));
        onView(withId(R.id.userId)).check(matches(isDisplayed()));

        onView(withId(R.id.deleteButton)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.viewEventButton)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.viewUserButton)).perform(scrollTo()).check(matches(isDisplayed()));

        onView(withId(R.id.doneButton)).check(matches(isDisplayed()));
    }

    /**
     * Tests that comment details are populated correctly from the Bundle.
     */
    @Test
    public void testCommentDetailsArePopulated() {
        launchFragment();

        onView(withId(R.id.commentUserName)).check(matches(withText("Test User")));
        onView(withId(R.id.commentText)).check(matches(withText("This is a test comment for admin moderation.")));
        onView(withId(R.id.eventId)).check(matches(withText("Event ID: test-event-456")));
        onView(withId(R.id.userId)).check(matches(withText("User ID: test-user-789")));
        onView(withId(R.id.reportCount)).check(matches(withText("3 reports")));
    }

    /**
     * Tests that the report count displays correctly when there are no reports.
     */
    @Test
    public void testNoReportsDisplaysCorrectly() {
        Bundle args = createTestBundle();
        args.putInt("reportCount", 0);

        FragmentScenario<AdminCommentDetailsFragment> scenario = FragmentScenario.launchInContainer(
                AdminCommentDetailsFragment.class,
                args,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        scenario.onFragment(fragment -> {
            Navigation.setViewNavController(fragment.requireView(), mockNavController);
        });

        onView(withId(R.id.reportCount)).check(matches(withText("No reports")));
    }

    /**
     * Tests that the report count pluralizes correctly.
     */
    @Test
    public void testReportCountPluralization() {
        Bundle args1 = createTestBundle();
        args1.putInt("reportCount", 1);

        FragmentScenario<AdminCommentDetailsFragment> scenario1 = FragmentScenario.launchInContainer(
                AdminCommentDetailsFragment.class,
                args1,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        scenario1.onFragment(fragment -> {
            Navigation.setViewNavController(fragment.requireView(), mockNavController);
        });

        onView(withId(R.id.reportCount)).check(matches(withText("1 report")));

        Bundle args5 = createTestBundle();
        args5.putInt("reportCount", 5);

        FragmentScenario<AdminCommentDetailsFragment> scenario5 = FragmentScenario.launchInContainer(
                AdminCommentDetailsFragment.class,
                args5,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        scenario5.onFragment(fragment -> {
            Navigation.setViewNavController(fragment.requireView(), mockNavController);
        });

        onView(withId(R.id.reportCount)).check(matches(withText("5 reports")));
    }

    /**
     * Tests that clicking the Delete button shows a confirmation dialog.
     */
    @Test
    public void testDeleteButton_showsConfirmationDialog() {
        launchFragment();

        onView(withId(R.id.deleteButton)).perform(scrollTo(), click());

        onView(withText("Delete Comment")).check(matches(isDisplayed()));
        onView(withText("Delete")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
    }

    /**
     * Tests that clicking the Done button navigates back.
     */
    @Test
    public void testDoneButtonNavigatesBack() {
        launchFragment();

        onView(withId(R.id.doneButton)).perform(click());

        verify(mockNavController).popBackStack();
    }

    /**
     * Tests that the timestamp displays correctly.
     */
    @Test
    public void testTimestampDisplays() {
        launchFragment();

        onView(withId(R.id.commentTimestamp)).check(matches(isDisplayed()));
    }

    /**
     * Tests that the view event button is clickable.
     */
    @Test
    public void testViewEventButtonClickable() {
        launchFragment();

        onView(withId(R.id.viewEventButton)).perform(scrollTo(), click());
    }

    /**
     * Tests that the view user button is clickable.
     */
    @Test
    public void testViewUserButtonClickable() {
        launchFragment();

        onView(withId(R.id.viewUserButton)).perform(scrollTo(), click());
    }
}