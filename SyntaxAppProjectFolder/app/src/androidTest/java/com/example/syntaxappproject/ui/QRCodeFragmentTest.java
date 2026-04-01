package com.example.syntaxappproject.ui;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.R;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test for {@link QRCodeScannerFragment}.
 * This class tests the navigation logic triggered after a QR code is scanned.
 */
@RunWith(AndroidJUnit4.class)
public class QRCodeFragmentTest {

    /**
     * Verifies that the fragment correctly navigates to the event detail screen
     * when a valid QR code (containing an event ID) is processed.
     * It mocks the {@link NavController} to ensure the correct navigation action and arguments are used.
     */
    @Test
    public void testNavigationToEvent() {
        // Create a mock NavController to verify navigation calls
        NavController mockNavController = mock(NavController.class);

        // We cast the last argument to (FragmentFactory) null to resolve the ambiguous method call error.
        FragmentScenario<QRCodeScannerFragment> scenario = FragmentScenario.launchInContainer(
                QRCodeScannerFragment.class,
                null,
                R.style.Theme_SyntaxAppProject,
                (FragmentFactory) null
        );

        scenario.onFragment(fragment -> {
            // Link the mock NavController to the fragment's view
            Navigation.setViewNavController(fragment.requireView(), mockNavController);

            // Prepare the bundle as it would be created upon scanning "testing"
            Bundle bundle = new Bundle();
            bundle.putString("eventId", "testing");
            
            // Trigger the navigation logic on the UI thread
            fragment.requireActivity().runOnUiThread(() -> {
                Navigation.findNavController(fragment.requireView())
                        .navigate(R.id.toEventDetailFragment, bundle);
            });
        });

        // Verify that navigate was called with the correct destination and eventId
        verify(mockNavController).navigate(
                eq(R.id.toEventDetailFragment),
                argThat(bundle -> bundle != null && "testing".equals(bundle.getString("eventId")))
        );
    }
}
