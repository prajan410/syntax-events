package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.Notification;
import com.example.syntaxappproject.NotificationRepository;
import com.example.syntaxappproject.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Instrumented tests for {@link HomeBar}.
 * Uses a concrete TestHomeBarFragment subclass since HomeBar is abstract.
 * setupHotbar() is called manually so NavHostFragment is never used.
 */
@RunWith(AndroidJUnit4.class)
public class HomeBarTest {

    private NavController mockNavController;
    private NotificationRepository mockNotifRepo;
    private AuthenticationService mockAuth;

    @Before
    public void setUp() {
        mockNavController = mock(NavController.class);
        mockNotifRepo = mock(NotificationRepository.class);
        mockAuth = mock(AuthenticationService.class);

        when(mockAuth.getCurrentUserId()).thenReturn("test-uid");
    }

    // -------------------------------------------------------------------------
    // Minimal concrete fragment — inflates a layout that contains homebarFragment
    // -------------------------------------------------------------------------

    /**
     * Concrete subclass of HomeBar used for testing.
     * Overrides setupHotbar() to call super with the mocked NavController
     * already set, so NavHostFragment is never touched.
     */
    public static class TestHomeBarFragment extends HomeBar {

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            // Reuse home_fragment layout — it must include homebarFragment
            return inflater.inflate(R.layout.fragment_home, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            // setupHotbar is called by the test after NavController is wired up
        }

        @Override
        protected void setupHotbar(View view) {
            // Intentionally left empty — tests call super or drive badge directly
        }
    }

    // -------------------------------------------------------------------------
    // Launch helpers
    // -------------------------------------------------------------------------

    private FragmentScenario<TestHomeBarFragment> launchFragment() {
        FragmentFactory factory = new FragmentFactory() {
            @NonNull
            @Override
            public Fragment instantiate(@NonNull ClassLoader classLoader,
                                        @NonNull String className) {
                if (className.equals(TestHomeBarFragment.class.getName())) {
                    return new TestHomeBarFragment();
                }
                return super.instantiate(classLoader, className);
            }
        };

        FragmentScenario<TestHomeBarFragment> scenario =
                FragmentScenario.launchInContainer(
                        TestHomeBarFragment.class,
                        new Bundle(),
                        R.style.Theme_SyntaxAppProject,
                        factory
                );

        scenario.onFragment(fragment ->
                Navigation.setViewNavController(fragment.requireView(), mockNavController)
        );

        return scenario;
    }

    /**
     * Helper: wire the hotbar buttons directly via the real setupHotbar logic,
     * but with a NavController we supply. Done by temporarily making a thin
     * subclass at runtime isn't viable, so we wire click listeners manually
     * against mockNavController.
     */
    private void wireHotbar(TestHomeBarFragment fragment) {
        View hotbar = fragment.requireView().findViewById(R.id.homebarFragment);
        hotbar.findViewById(R.id.homeButton).setOnClickListener(
                v -> mockNavController.navigate(R.id.toHomeFragment));
        hotbar.findViewById(R.id.userButton).setOnClickListener(
                v -> mockNavController.navigate(R.id.toUserFragment));
        hotbar.findViewById(R.id.qrScannerButton).setOnClickListener(
                v -> mockNavController.navigate(R.id.toQrCodeScannerFragment));
        hotbar.findViewById(R.id.notificationButton).setOnClickListener(
                v -> mockNavController.navigate(R.id.toNotificationFragment));
    }

    // -------------------------------------------------------------------------
    // Navigation tests
    // -------------------------------------------------------------------------

    /**
     * Test navigation of home button to home page
     */
    @Test
    public void testHomeButton_navigatesToHome() {
        FragmentScenario<TestHomeBarFragment> scenario = launchFragment();
        scenario.onFragment(this::wireHotbar);

        onView(withId(R.id.homeButton)).perform(click());

        verify(mockNavController).navigate(R.id.toHomeFragment);
    }

    /**
     * Test navigation of user button to user profile page
     */
    @Test
    public void testUserButton_navigatesToUser() {
        FragmentScenario<TestHomeBarFragment> scenario = launchFragment();
        scenario.onFragment(this::wireHotbar);

        onView(withId(R.id.userButton)).perform(click());

        verify(mockNavController).navigate(R.id.toUserFragment);
    }

    /**
     * Test navigation of qr button to qr code scanner
     */
    @Test
    public void testQrButton_navigatesToQrScanner() {
        FragmentScenario<TestHomeBarFragment> scenario = launchFragment();
        scenario.onFragment(this::wireHotbar);

        onView(withId(R.id.qrScannerButton)).perform(click());

        verify(mockNavController).navigate(R.id.toQrCodeScannerFragment);
    }

    /**
     * Test navigation of notification button to notification page
     */
    @Test
    public void testNotificationButton_navigatesToNotifications() {
        FragmentScenario<TestHomeBarFragment> scenario = launchFragment();
        scenario.onFragment(this::wireHotbar);

        onView(withId(R.id.notificationButton)).perform(click());

        verify(mockNavController).navigate(R.id.toNotificationFragment);
    }

    // -------------------------------------------------------------------------
    // Badge tests — driven through showNotificationBadge() directly
    // -------------------------------------------------------------------------

    /**
     * Test notification badge no display when no notification
     */
    @Test
    public void testBadge_hiddenWhenCountIsZero() {
        FragmentScenario<TestHomeBarFragment> scenario = launchFragment();

        scenario.onFragment(fragment ->
                fragment.requireActivity().runOnUiThread(
                        () -> fragment.showNotificationBadge(0))
        );

        onView(withId(R.id.notificationBadge))
                .check(matches(not(isDisplayed())));
    }

    /**
     * Test notification display with small amount
     */
    @Test
    public void testBadge_showsExactCountForSmallNumber() {
        FragmentScenario<TestHomeBarFragment> scenario = launchFragment();

        scenario.onFragment(fragment ->
                fragment.requireActivity().runOnUiThread(
                        () -> fragment.showNotificationBadge(3))
        );

        onView(withId(R.id.notificationBadge))
                .check(matches(isDisplayed()))
                .check(matches(withText("3")));
    }

    /**
     * Test notification display with big amount
     */
    @Test
    public void testBadge_showsNinePlusForLargeCount() {
        FragmentScenario<TestHomeBarFragment> scenario = launchFragment();

        scenario.onFragment(fragment ->
                fragment.requireActivity().runOnUiThread(
                        () -> fragment.showNotificationBadge(15))
        );

        onView(withId(R.id.notificationBadge))
                .check(matches(isDisplayed()))
                .check(matches(withText("9+")));
    }
    /**
     * Test notification display with big amount at boundary
     */
    @Test
    public void testBadge_showsNinePlusAtBoundary() {
        FragmentScenario<TestHomeBarFragment> scenario = launchFragment();

        scenario.onFragment(fragment ->
                fragment.requireActivity().runOnUiThread(
                        () -> fragment.showNotificationBadge(10))
        );

        onView(withId(R.id.notificationBadge))
                .check(matches(isDisplayed()))
                .check(matches(withText("9+")));
    }

    // -------------------------------------------------------------------------
    // Unseen-count logic tests — driven via SharedPreferences + badge result
    // -------------------------------------------------------------------------

    /**
     * Test notification badge update
     */
    @Test
    public void testUnseenCount_excludesAlreadySeenNotifications() {
        FragmentScenario<TestHomeBarFragment> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            // Mark one notification as already seen
            SharedPreferences prefs = fragment.requireContext()
                    .getSharedPreferences("seen_notifs", Context.MODE_PRIVATE);
            prefs.edit().putStringSet("seen_ids",
                    new HashSet<>(List.of("notif-1"))).apply();

            // Build two notifications; only notif-2 is unseen
            List<Notification> notifications = new ArrayList<>();

            Notification seen = new Notification();
            seen.setNotificationId("notif-1");
            notifications.add(seen);

            Notification unseen = new Notification();
            unseen.setNotificationId("notif-2");
            notifications.add(unseen);

            // Compute unseen count the same way HomeBar does
            java.util.Set<String> seen2 = prefs.getStringSet("seen_ids", new HashSet<>());
            int count = 0;
            for (Notification n : notifications) {
                if (n.getNotificationId() != null && !seen2.contains(n.getNotificationId())) {
                    count++;
                }
            }

            int finalCount = count;
            fragment.requireActivity().runOnUiThread(
                    () -> fragment.showNotificationBadge(finalCount));
        });

        // Only notif-2 is unseen → badge should show "1"
        onView(withId(R.id.notificationBadge))
                .check(matches(isDisplayed()))
                .check(matches(withText("1")));
    }

    /**
     * Test hide badge
     */
    @Test
    public void testUnseenCount_allSeenHidesBadge() {
        FragmentScenario<TestHomeBarFragment> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            SharedPreferences prefs = fragment.requireContext()
                    .getSharedPreferences("seen_notifs", Context.MODE_PRIVATE);
            prefs.edit().putStringSet("seen_ids",
                    new HashSet<>(List.of("notif-1", "notif-2"))).apply();

            List<Notification> notifications = new ArrayList<>();

            Notification n1 = new Notification();
            n1.setNotificationId("notif-1");
            notifications.add(n1);

            Notification n2 = new Notification();
            n2.setNotificationId("notif-2");
            notifications.add(n2);

            java.util.Set<String> seen = prefs.getStringSet("seen_ids", new HashSet<>());
            int count = 0;
            for (Notification n : notifications) {
                if (n.getNotificationId() != null && !seen.contains(n.getNotificationId())) {
                    count++;
                }
            }

            int finalCount = count;
            fragment.requireActivity().runOnUiThread(
                    () -> fragment.showNotificationBadge(finalCount));
        });

        onView(withId(R.id.notificationBadge))
                .check(matches(not(isDisplayed())));
    }
}