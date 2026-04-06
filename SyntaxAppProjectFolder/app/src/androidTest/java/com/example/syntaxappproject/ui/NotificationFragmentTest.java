package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyInt;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.Notification;
import com.example.syntaxappproject.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Instrumented tests for {@link NotificationFragment}.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationFragmentTest {

    private AuthenticationService mockAuth;
    private NavController mockNavController;
    private Notification fakeNotification;

    @Before
    public void setUp() {
        mockAuth = mock(AuthenticationService.class);
        mockNavController = mock(NavController.class);
        when(mockAuth.getCurrentUserId()).thenReturn("test-user");

        fakeNotification = new Notification();
        fakeNotification.setNotificationId("notif1");
        fakeNotification.setTitle("Test Notification");
        fakeNotification.setBody("This is a test notification");
        fakeNotification.setSenderRole("ADMIN");
    }

    // -------------------------------------------------------------------------
    // Test subclass
    // -------------------------------------------------------------------------

    /**
     * Concrete subclass that:
     *  - stubs setupHotbar() to prevent NavHostFragment crash
     *  - skips the real onViewCreated() Firestore calls by overriding it
     *  - exposes a helper to populate the adapter via reflection
     */
    public static class TestNotificationFragment extends NotificationFragment {

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater,
                                 @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_notification, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            // Skip the real implementation entirely — no Firestore, no auth calls.
            // We wire just the bare minimum the tests need.
            setupHotbar(view);

            // Set up the RecyclerView with a fresh adapter so tests can populate it
            try {
                RecyclerView rv = view.findViewById(R.id.notificationsRecyclerView);
                rv.setLayoutManager(new LinearLayoutManager(getContext()));

                // Instantiate the private inner adapter via reflection
                Class<?> adapterClass = getAdapterClass();
                Object adapterInstance = adapterClass
                        .getDeclaredConstructor(NotificationFragment.class)
                        .newInstance(this);

                Field adapterField = NotificationFragment.class.getDeclaredField("adapter");
                adapterField.setAccessible(true);
                adapterField.set(this, adapterInstance);

                rv.setAdapter((RecyclerView.Adapter<?>) adapterInstance);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set up adapter via reflection", e);
            }
        }

        @Override
        protected void setupHotbar(View view) {
            // No-op: prevents NavHostFragment.findNavController() crash
        }

        /**
         * Finds the private CombinedNotificationAdapter inner class by name.
         */
        private Class<?> getAdapterClass() {
            for (Class<?> c : NotificationFragment.class.getDeclaredClasses()) {
                if (c.getSimpleName().equals("CombinedNotificationAdapter")) {
                    return c;
                }
            }
            throw new RuntimeException("CombinedNotificationAdapter class not found");
        }

        /**
         * Populates the RecyclerView adapter with test notifications via reflection,
         * bypassing the private inner class visibility restriction.
         */
        public void setNotificationsForTest(List<Notification> notifications) {
            try {
                Field adapterField = NotificationFragment.class.getDeclaredField("adapter");
                adapterField.setAccessible(true);
                Object adapterInstance = adapterField.get(this);

                // CombinedNotificationAdapter.setItems(List<Object>, String)
                Method setItems = adapterInstance.getClass()
                        .getDeclaredMethod("setItems", List.class, String.class);
                setItems.setAccessible(true);
                setItems.invoke(adapterInstance, new ArrayList<>(notifications), "test-user");

                // Make the notificationsCard visible so items can be seen
                View card = requireView().findViewById(R.id.notificationsCard);
                if (card != null) card.setVisibility(View.VISIBLE);

            } catch (Exception e) {
                throw new RuntimeException("setNotificationsForTest failed", e);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Launch helper
    // -------------------------------------------------------------------------

    private FragmentScenario<TestNotificationFragment> launchFragment() {
        FragmentFactory factory = new FragmentFactory() {
            @NonNull
            @Override
            public Fragment instantiate(@NonNull ClassLoader classLoader,
                                        @NonNull String className) {
                if (!className.equals(TestNotificationFragment.class.getName())) {
                    return super.instantiate(classLoader, className);
                }
                TestNotificationFragment fragment = new TestNotificationFragment();
                injectMocks(fragment);
                return fragment;
            }
        };

        FragmentScenario<TestNotificationFragment> scenario =
                FragmentScenario.launchInContainer(
                        TestNotificationFragment.class,
                        new Bundle(),
                        R.style.Theme_SyntaxAppProject,
                        factory
                );

        scenario.onFragment(fragment -> {
            Navigation.setViewNavController(fragment.requireView(), mockNavController);

            List<Notification> list = new ArrayList<>();
            list.add(fakeNotification);
            fragment.setNotificationsForTest(list);
        });

        return scenario;
    }

    private void injectMocks(NotificationFragment fragment) {
        try {
            Field authField = NotificationFragment.class.getDeclaredField("authService");
            authField.setAccessible(true);
            authField.set(fragment, mockAuth);
        } catch (Exception e) {
            throw new RuntimeException("Mock injection failed", e);
        }
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /**
     * Test title display
     */
    @Test
    public void testNotificationTitle_isDisplayed() {
        launchFragment();

        onView(withText("Test Notification"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test body display
     */
    @Test
    public void testNotificationBody_isDisplayed() {
        launchFragment();

        onView(withText("This is a test notification"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test admin notification display
     */
    @Test
    public void testAdminNotification_showsFromAdministration() {
        launchFragment();

        onView(withText("From: Administration"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test click notification
     */
    @Test
    public void testClickNotification_doesNotNavigate() {
        launchFragment();

        onView(withId(R.id.notificationsRecyclerView))
                .perform(actionOnItemAtPosition(0, click()));

        verify(mockNavController, never()).navigate(anyInt());
        verify(mockNavController, never()).navigate(anyInt(), any(Bundle.class));
    }

    /**
     * Test multiple notification display
     */
    @Test
    public void testMultipleNotifications_allDisplayed() {
        FragmentScenario<TestNotificationFragment> scenario =
                FragmentScenario.launchInContainer(
                        TestNotificationFragment.class,
                        new Bundle(),
                        R.style.Theme_SyntaxAppProject
                );

        scenario.onFragment(fragment -> {
            Navigation.setViewNavController(fragment.requireView(), mockNavController);

            Notification second = new Notification();
            second.setNotificationId("notif2");
            second.setTitle("Second Notification");
            second.setBody("Another body");
            second.setSenderRole("ADMIN");

            List<Notification> list = new ArrayList<>();
            list.add(fakeNotification);
            list.add(second);
            fragment.setNotificationsForTest(list);
        });

        onView(withText("Test Notification")).check(matches(isDisplayed()));
        onView(withText("Second Notification")).check(matches(isDisplayed()));
    }
}