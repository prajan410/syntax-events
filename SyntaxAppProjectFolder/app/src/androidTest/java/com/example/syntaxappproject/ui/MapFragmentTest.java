package com.example.syntaxappproject.ui;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.EventDetailRepository;
import com.example.syntaxappproject.Profile;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osmdroid.views.overlay.Marker;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented test for MapFragment to verify pin addition logic.
 * This test uses FragmentScenario and Mockito to simulate the environment without real Firebase.
 */
@RunWith(AndroidJUnit4.class)
public class MapFragmentTest {

    private ProfileRepository mockProfileRepo;
    private AuthenticationService mockAuthService;
    private EventDetailRepository mockEventRepo;
    private FirebaseFirestore mockDb;

    @Before
    public void setUp() {

    }

    /**
     * Test add pin on map
     * @throws InterruptedException exception
     */
    @Test
    public void testAddPinToMap() throws InterruptedException {
        // 1. Create a fake event ID and put it in a bundle
        String fakeEventId = "test-event-id";
        Bundle args = new Bundle();
        args.putString("eventId", fakeEventId);

        // 2. Mock profile data for the entrant
        String testUserId = "test-user-id";
        Profile testProfile = new Profile();
        testProfile.setName("Test User Name");

        // Mock ProfileRepository.getProfile behavior to return the test profile
        doAnswer(invocation -> {
            ProfileRepository.ProfileCallback callback = invocation.getArgument(1);
            callback.onResult(testProfile);
            return null;
        }).when(mockProfileRepo).getProfile(eq(testUserId), any());

        // 3. Use FragmentFactory to inject mocks into MapFragment before onCreate/onViewCreated
        FragmentFactory factory = new FragmentFactory() {
            @NonNull
            @Override
            public Fragment instantiate(@NonNull ClassLoader classLoader, @NonNull String className) {
                if (className.equals(MapFragment.class.getName())) {
                    MapFragment fragment = new MapFragment();
                    fragment.db = mockDb;
                    fragment.profileRepo = mockProfileRepo;
                    fragment.authService = mockAuthService;
                    fragment.eventRepo = mockEventRepo;
                    return fragment;
                }
                return super.instantiate(classLoader, className);
            }
        };

        // 4. Launch fragment in container with a Material theme to avoid InflateException with MaterialButton
        FragmentScenario<MapFragment> scenario = FragmentScenario.launchInContainer(
                MapFragment.class, args, R.style.Theme_SyntaxAppProject, factory);

        // 5. Trigger the pin addition logic
        scenario.onFragment(fragment -> {
            // Simulate adding a marker for a user at a specific location
            fragment.addMarkerForUser(testUserId, 53.5461, -113.4938);
        });

        // 6. Verify that the marker was added to the map overlays
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] markerFound = {false};

        scenario.onFragment(fragment -> {
            if (fragment.map != null) {
                for (Object overlay : fragment.map.getOverlays()) {
                    if (overlay instanceof Marker) {
                        Marker marker = (Marker) overlay;
                        if ("Test User Name".equals(marker.getTitle())) {
                            markerFound[0] = true;
                            break;
                        }
                    }
                }
            }
            latch.countDown();
        });

        // Wait for the check to complete
        assertTrue("Test timed out waiting for marker verification", latch.await(5, TimeUnit.SECONDS));
        assertTrue("Marker with title 'Test User Name' should be present on the map", markerFound[0]);
    }
}
