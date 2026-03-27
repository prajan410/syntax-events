package com.example.syntaxappproject;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.ui.AdminFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumented UI tests for {@link AdminFragment}.
 *
 * <p>Verifies that the admin dashboard displays the header title and all
 * four tool navigation rows on launch.</p>
 */
@RunWith(AndroidJUnit4.class)
public class AdminFragmentTest {

    /**
     * Verifies that the admin header and all four tool rows are visible
     * when the fragment is launched.
     */
    @Test
    public void testAdminFragmentDisplaysButtons() {
        FragmentScenario.launchInContainer(
                AdminFragment.class,
                null,
                R.style.Theme_SyntaxAppProject
        );

        onView(withText("Admin")).check(matches(isDisplayed()));
        onView(withId(R.id.cardBrowseEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.cardBrowseProfiles)).check(matches(isDisplayed()));
        onView(withId(R.id.cardBrowseImages)).check(matches(isDisplayed()));
        onView(withId(R.id.cardNotificationLogs)).check(matches(isDisplayed()));
    }
}
