package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
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
import java.util.List;

/**
 * Instrumented intent tests for {@link AdminBrowseNotificationsFragment}.
 */
@RunWith(AndroidJUnit4.class)
public class AdminBrowseNotificationsFragmentTest {

    private AuthenticationService mockAuth;
    private NotificationRepository mockNotifRepo;

    @Before
    public void setUp() {
        mockAuth = mock(AuthenticationService.class);
        mockNotifRepo = mock(NotificationRepository.class);
        when(mockAuth.getCurrentUserId()).thenReturn("admin-uid");

        // Default: getAllNotifications returns empty list immediately
        org.mockito.Mockito.doAnswer(invocation -> {
            NotificationRepository.NotificationListCallback cb = invocation.getArgument(0);
            cb.onLoaded(new ArrayList<>());
            return null;
        }).when(mockNotifRepo).getAllNotifications(any());
    }

    // -------------------------------------------------------------------------
    // Test subclass — no NavController needed, doneButton uses FragmentManager
    // -------------------------------------------------------------------------

    public static class TestAdminBrowseNotificationsFragment
            extends AdminBrowseNotificationsFragment {
        // No nav stubs needed — doneButton calls popBackStack() on the
        // FragmentManager directly, which is safe in FragmentScenario.
    }

    // -------------------------------------------------------------------------
    // Launch helpers
    // -------------------------------------------------------------------------

    private FragmentScenario<TestAdminBrowseNotificationsFragment> launchFragment() {
        FragmentFactory factory = new FragmentFactory() {
            @Override
            public Fragment instantiate(ClassLoader classLoader, String className) {
                if (!className.equals(
                        TestAdminBrowseNotificationsFragment.class.getName())) {
                    return super.instantiate(classLoader, className);
                }
                TestAdminBrowseNotificationsFragment fragment =
                        new TestAdminBrowseNotificationsFragment();
                injectMocks(fragment);
                return fragment;
            }
        };

        return FragmentScenario.launchInContainer(
                TestAdminBrowseNotificationsFragment.class,
                new Bundle(),
                R.style.Theme_SyntaxAppProject,
                factory
        );
    }

    private void injectMocks(AdminBrowseNotificationsFragment fragment) {
        try {
            Field authField = AdminBrowseNotificationsFragment.class
                    .getDeclaredField("authService");
            authField.setAccessible(true);
            authField.set(fragment, mockAuth);

            Field repoField = AdminBrowseNotificationsFragment.class
                    .getDeclaredField("notifRepository");
            repoField.setAccessible(true);
            repoField.set(fragment, mockNotifRepo);
        } catch (Exception e) {
            throw new RuntimeException("Mock injection failed", e);
        }
    }

    // -------------------------------------------------------------------------
    // Loading / empty state tests
    // -------------------------------------------------------------------------

    /**
     * Test with empty state show nothing
     */
    @Test
    public void testEmptyState_showsZeroBadge() {
        // setUp() already stubs getAllNotifications → empty list
        launchFragment();

        onView(withId(R.id.notifCountBadge))
                .check(matches(withText("0 notifications")));
    }

    /**
     * Test empty state with empty text show
     */
    @Test
    public void testEmptyState_emptyTextIsDisplayed() {
        launchFragment();

        onView(withId(R.id.emptyText))
                .check(matches(isDisplayed()));
    }

    /**
     * Test correct display of notification
     */
    @Test
    public void testNotificationsLoaded_showsCorrectBadgeCount() {
        // Stub two notifications coming back
        Notification n1 = new Notification();
        n1.setTitle("Admin Alert 1");
        Notification n2 = new Notification();
        n2.setTitle("Admin Alert 2");

        org.mockito.Mockito.doAnswer(invocation -> {
            NotificationRepository.NotificationListCallback cb =
                    invocation.getArgument(0);
            List<Notification> list = new ArrayList<>();
            list.add(n1);
            list.add(n2);
            cb.onLoaded(list);
            return null;
        }).when(mockNotifRepo).getAllNotifications(any());

        launchFragment();

        onView(withId(R.id.notifCountBadge))
                .check(matches(withText("2 notifications")));
    }

    /**
     * Test single notification
     */
    @Test
    public void testSingleNotification_badgeUseSingular() {
        Notification n = new Notification();
        n.setTitle("Solo Alert");

        org.mockito.Mockito.doAnswer(invocation -> {
            NotificationRepository.NotificationListCallback cb =
                    invocation.getArgument(0);
            List<Notification> list = new ArrayList<>();
            list.add(n);
            cb.onLoaded(list);
            return null;
        }).when(mockNotifRepo).getAllNotifications(any());

        launchFragment();

        onView(withId(R.id.notifCountBadge))
                .check(matches(withText("1 notification")));
    }

    // -------------------------------------------------------------------------
    // Send validation tests — repo should NOT be called
    // -------------------------------------------------------------------------

    /**
     * Test send notification with empty title
     */

    @Test
    public void testSend_emptyTitle_doesNotCallRepo() {
        launchFragment();

        onView(withId(R.id.notifMessageInput))
                .perform(typeText("Some message"), closeSoftKeyboard());
        onView(withId(R.id.sendNotifBtn)).perform(click());

        verify(mockNotifRepo, never())
                .sendNotification(any(Notification.class), anyString(), anyList(), any());
    }

    /**
     * Test send notification with empty body
     */
    @Test
    public void testSend_emptyMessage_doesNotCallRepo() {
        launchFragment();

        onView(withId(R.id.notifTitleInput))
                .perform(typeText("My Title"), closeSoftKeyboard());
        onView(withId(R.id.sendNotifBtn)).perform(click());

        verify(mockNotifRepo, never())
                .sendNotification(any(Notification.class), anyString(), anyList(), any());
    }

    // -------------------------------------------------------------------------
    // Send success tests
    // -------------------------------------------------------------------------

    /**
     * Test success send notification and call repo
     */

    @Test
    public void testSend_validInputs_callsRepo() {
        org.mockito.Mockito.doAnswer(invocation -> {
            NotificationRepository.NotificationCallback cb =
                    invocation.getArgument(3);
            cb.onComplete(true);
            return null;
        }).when(mockNotifRepo).sendNotification(
                any(Notification.class), anyString(), anyList(), any());

        launchFragment();

        onView(withId(R.id.notifTitleInput))
                .perform(typeText("Global Alert"), closeSoftKeyboard());
        onView(withId(R.id.notifMessageInput))
                .perform(typeText("Attention all users"), closeSoftKeyboard());
        onView(withId(R.id.sendNotifBtn)).perform(click());

        verify(mockNotifRepo).sendNotification(
                any(Notification.class), anyString(), anyList(), any());
    }

    /**
     * Test success send notification with clear field
     */
    @Test
    public void testSend_onSuccess_clearsInputFields() {
        org.mockito.Mockito.doAnswer(invocation -> {
            NotificationRepository.NotificationCallback cb =
                    invocation.getArgument(3);
            cb.onComplete(true);
            return null;
        }).when(mockNotifRepo).sendNotification(
                any(Notification.class), anyString(), anyList(), any());

        launchFragment();

        onView(withId(R.id.notifTitleInput))
                .perform(typeText("Title"), closeSoftKeyboard());
        onView(withId(R.id.notifMessageInput))
                .perform(typeText("Body"), closeSoftKeyboard());
        onView(withId(R.id.sendNotifBtn)).perform(click());

        onView(withId(R.id.notifTitleInput)).check(matches(withText("")));
        onView(withId(R.id.notifMessageInput)).check(matches(withText("")));
    }

    /**
     * Test button enable after success send notification
     */
    @Test
    public void testSend_onSuccess_buttonIsReEnabled() {
        org.mockito.Mockito.doAnswer(invocation -> {
            NotificationRepository.NotificationCallback cb =
                    invocation.getArgument(3);
            cb.onComplete(true);
            return null;
        }).when(mockNotifRepo).sendNotification(
                any(Notification.class), anyString(), anyList(), any());

        launchFragment();

        onView(withId(R.id.notifTitleInput))
                .perform(typeText("Title"), closeSoftKeyboard());
        onView(withId(R.id.notifMessageInput))
                .perform(typeText("Body"), closeSoftKeyboard());
        onView(withId(R.id.sendNotifBtn)).perform(click());

        onView(withId(R.id.sendNotifBtn))
                .check(matches(isEnabled()));
    }

    /**
     * Test button restore
     */
    @Test
    public void testSend_onSuccess_buttonTextRestored() {
        org.mockito.Mockito.doAnswer(invocation -> {
            NotificationRepository.NotificationCallback cb =
                    invocation.getArgument(3);
            cb.onComplete(true);
            return null;
        }).when(mockNotifRepo).sendNotification(
                any(Notification.class), anyString(), anyList(), any());

        launchFragment();

        onView(withId(R.id.notifTitleInput))
                .perform(typeText("Title"), closeSoftKeyboard());
        onView(withId(R.id.notifMessageInput))
                .perform(typeText("Body"), closeSoftKeyboard());
        onView(withId(R.id.sendNotifBtn)).perform(click());

        onView(withId(R.id.sendNotifBtn))
                .check(matches(withText("Send to All Entrants")));
    }

    /**
     * Test button enable when send notification fail
     */
    @Test
    public void testSend_onFailure_buttonIsReEnabled() {
        org.mockito.Mockito.doAnswer(invocation -> {
            NotificationRepository.NotificationCallback cb =
                    invocation.getArgument(3);
            cb.onComplete(false);
            return null;
        }).when(mockNotifRepo).sendNotification(
                any(Notification.class), anyString(), anyList(), any());

        launchFragment();

        onView(withId(R.id.notifTitleInput))
                .perform(typeText("Title"), closeSoftKeyboard());
        onView(withId(R.id.notifMessageInput))
                .perform(typeText("Body"), closeSoftKeyboard());
        onView(withId(R.id.sendNotifBtn)).perform(click());

        onView(withId(R.id.sendNotifBtn))
                .check(matches(isEnabled()));
    }

    /**
     * Test the field clear when sent notification fail
     */
    @Test
    public void testSend_onFailure_fieldsNotCleared() {
        org.mockito.Mockito.doAnswer(invocation -> {
            NotificationRepository.NotificationCallback cb =
                    invocation.getArgument(3);
            cb.onComplete(false);
            return null;
        }).when(mockNotifRepo).sendNotification(
                any(Notification.class), anyString(), anyList(), any());

        launchFragment();

        onView(withId(R.id.notifTitleInput))
                .perform(typeText("Title"), closeSoftKeyboard());
        onView(withId(R.id.notifMessageInput))
                .perform(typeText("Body"), closeSoftKeyboard());
        onView(withId(R.id.sendNotifBtn)).perform(click());

        // Fields should NOT be cleared on failure
        onView(withId(R.id.notifTitleInput)).check(matches(withText("Title")));
        onView(withId(R.id.notifMessageInput)).check(matches(withText("Body")));
    }
}