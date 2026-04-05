package com.example.syntaxappproject;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AdminImageDetailsTest {

    /**
     * Verifies that the image details screen displays the title and
     * remove image button when launched with valid image data.
     */
    @Test
    public void testImageDetailsDisplaysRemoveButton() {
        Bundle bundle = new Bundle();
        bundle.putString("imageId", "testevent2");
        bundle.putString("imageUrl", "https://example.com/poster.jpg");
        bundle.putString("uploadedBy", "Joe");

        FragmentScenario.launchInContainer(AdminImageDetails.class, bundle);

        onView(withText("Image Details")).check(matches(isDisplayed()));
        onView(withId(R.id.btn_remove_image)).check(matches(isDisplayed()));
    }
}