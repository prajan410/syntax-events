package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.R;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test for {@link CreateEventQRFragment}.
 * Verifies that the fragment correctly displays a success message containing
 * the provided event ID and shows the QR code preview.
 */
@RunWith(AndroidJUnit4.class)
public class CreateEventQRFragmentTest {

    /**
     * Tests that passing an event ID to the fragment displays the expected success text
     * and that the QR preview image is visible.
     */
    @Test
    public void testQRCodeDisplayWithEventId() {
        String testEventId = "random_event_123";
        Bundle args = new Bundle();
        args.putString("eventId", testEventId);

        // Launch the fragment with arguments
        FragmentScenario.launchInContainer(CreateEventQRFragment.class, args, R.style.Theme_SyntaxAppProject);

        // Check if the success message contains the event ID
        onView(withId(R.id.success_text))
                .check(matches(withText(testEventId + " successfully created!")));

        // Verify the QR code preview is displayed
        onView(withId(R.id.event_qr_preview))
                .check(matches(isDisplayed()));
    }
}
