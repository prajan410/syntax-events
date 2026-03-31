package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented UI tests for {@link ManageEventFragment}.
 *
 * <p>Tests the event management functionality including:</p>
 * <ul>
 *   <li>UI display and layout verification</li>
 *   <li>Cancel button navigation behavior</li>
 *   <li>Change detection and Save button appearance</li>
 *   <li>Delete confirmation dialog display</li>
 *   <li>Gallery intent launching for poster selection</li>
 *   <li>Bullet point behavior in lottery criteria field</li>
 * </ul>
 *
 * <p>Uses {@link FragmentFactory} to inject test mode before fragment creation,
 * preventing actual Firestore calls. Mock {@link NavController} is used to verify
 * navigation actions without performing real fragment transitions.</p>
 */
@RunWith(AndroidJUnit4.class)
public class ManageEventFragmentTest {

    /** Mock navigation controller used to verify navigation calls. */
    private NavController mockNavController;

    /**
     * Sets up the test environment before each test.
     * Initializes mocks and Espresso Intents for intent verification.
     */
    @Before
    public void setUp() {
        mockNavController = mock(NavController.class);
        Intents.init();
    }

    /**
     * Cleans up after each test.
     * Releases Espresso Intents resources.
     */
    @After
    public void tearDown() {
        Intents.release();
    }

    /**
     * Launches the fragment with test mode enabled using FragmentFactory.
     * This ensures the {@link ManageEventFragment#disableFirestoreForTest} flag
     * is set BEFORE the fragment's lifecycle methods run, preventing any
     * Firestore operations during tests.
     *
     * @return a FragmentScenario for interacting with the launched fragment
     */
    private FragmentScenario<ManageEventFragment> launchFragment() {
        Bundle args = new Bundle();
        args.putString("eventId", "test-event-123");

        FragmentFactory factory = new FragmentFactory() {
            @NonNull
            @Override
            public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
                if (className.equals(ManageEventFragment.class.getName())) {
                    ManageEventFragment fragment = new ManageEventFragment();
                    fragment.disableFirestoreForTest = true;
                    return fragment;
                }
                return super.instantiate(classLoader, className);
            }
        };

        FragmentScenario<ManageEventFragment> scenario = FragmentScenario.launchInContainer(
                ManageEventFragment.class,
                args,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                factory
        );

        scenario.onFragment(fragment -> {
            Navigation.setViewNavController(fragment.requireView(), mockNavController);
        });

        return scenario;
    }

    /**
     * Verifies that all essential UI elements are displayed correctly
     * when the fragment is first launched.
     */
    @Test
    public void testScreenDisplays() {
        launchFragment();

        onView(withId(R.id.headerTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.actionButton)).check(matches(withText("Cancel")));
        onView(withId(R.id.deleteButton)).check(matches(isDisplayed()));
    }

    /**
     * Tests that clicking the Cancel button without making any changes
     * triggers navigation back to the previous screen.
     */
    @Test
    public void testCancelWithoutChanges_navigatesBack() {
        launchFragment();

        onView(withId(R.id.actionButton)).perform(click());

        verify(mockNavController).navigateUp();
    }

    /**
     * Tests that making a change to any input field changes the action button
     * text from "Cancel" to "Save", indicating pending changes.
     */
    @Test
    public void testMakingChange_showsSaveButton() {
        launchFragment();

        onView(withId(R.id.eventNameInput)).perform(scrollTo(), replaceText("Updated"), closeSoftKeyboard());

        onView(withId(R.id.actionButton)).check(matches(withText("Save")));
    }

    /**
     * Tests that clicking the Delete button displays a confirmation dialog
     * with the appropriate title and action buttons.
     */
    @Test
    public void testDeleteButton_showsDialog() {
        launchFragment();

        onView(withId(R.id.deleteButton)).perform(click());

        onView(withText("Delete Event")).check(matches(isDisplayed()));
        onView(withText("Delete")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
    }

    /**
     * Tests that clicking the poster area launches the gallery intent
     * with the correct action and MIME type for image selection.
     *
     * <p>Uses Espresso Intents to stub the gallery result and verify
     * that the intent was sent correctly.</p>
     */
    @Test
    public void testOpenGalleryIntent() {
        launchFragment();

        Intent resultData = new Intent();
        resultData.setData(Uri.parse("content://test/image"));
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);

        intending(hasAction(Intent.ACTION_PICK)).respondWith(result);

        onView(withId(R.id.posterTapArea)).perform(click());

        intended(hasAction(Intent.ACTION_PICK));
        intended(hasType("image/*"));
    }

    /**
     * Tests that the lottery criteria input field has bullet point behavior.
     * When cleared, the field should automatically reinitialize with "• ".
     */
    @Test
    public void testLotteryCriteria_hasBulletPointBehavior() {
        launchFragment();

        onView(withId(R.id.lotteryCriteriaInput)).perform(scrollTo()).check(matches(isDisplayed()));

        onView(withId(R.id.lotteryCriteriaInput)).perform(scrollTo(), replaceText(""), closeSoftKeyboard());
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.lotteryCriteriaInput)).perform(scrollTo()).check(matches(withText("• ")));
    }
}