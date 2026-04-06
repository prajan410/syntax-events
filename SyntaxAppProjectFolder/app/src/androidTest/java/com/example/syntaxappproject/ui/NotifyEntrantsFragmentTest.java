package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
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
import java.util.List;

/**
 * Instrumented intent tests for {@link NotifyEntrantsFragment}.
 */
@RunWith(AndroidJUnit4.class)
public class NotifyEntrantsFragmentTest {

    private AuthenticationService mockAuth;
    private NotificationRepository mockNotifRepo;

    @Before
    public void setUp() {
        mockAuth = mock(AuthenticationService.class);
        mockNotifRepo = mock(NotificationRepository.class);
        when(mockAuth.getCurrentUserId()).thenReturn("organizer-uid");
    }

    // -------------------------------------------------------------------------
    // Test subclass — stubs Nav calls so popBackStack() / navigate() don't crash
    // -------------------------------------------------------------------------

    public static class TestNotifyEntrantsFragment extends NotifyEntrantsFragment {
        @Override
        public void onViewCreated(android.view.View view,
                                  @androidx.annotation.Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Stub out nav buttons so NavHostFragment is never touched
            view.findViewById(R.id.doneButton)
                    .setOnClickListener(v -> { /* no-op */ });
            view.findViewById(R.id.coOrganizerInviteButton)
                    .setOnClickListener(v -> { /* no-op */ });
        }
    }

    // -------------------------------------------------------------------------
    // Launch helpers
    // -------------------------------------------------------------------------

    private FragmentScenario<TestNotifyEntrantsFragment> launchFragment() {
        return launchFragment("event-abc", "Test Event");
    }

    private FragmentScenario<TestNotifyEntrantsFragment> launchFragment(
            String eventId, String eventName) {

        Bundle args = new Bundle();
        args.putString("eventId", eventId);
        args.putString("eventName", eventName);

        FragmentFactory factory = new FragmentFactory() {
            @Override
            public Fragment instantiate(ClassLoader classLoader, String className) {
                if (!className.equals(TestNotifyEntrantsFragment.class.getName())) {
                    return super.instantiate(classLoader, className);
                }
                TestNotifyEntrantsFragment fragment = new TestNotifyEntrantsFragment();
                fragment.setArguments(args);
                injectMocks(fragment);
                return fragment;
            }
        };

        return FragmentScenario.launchInContainer(
                TestNotifyEntrantsFragment.class,
                args,
                R.style.Theme_SyntaxAppProject,
                factory
        );
    }

    private void injectMocks(NotifyEntrantsFragment fragment) {
        try {
            Field authField = NotifyEntrantsFragment.class.getDeclaredField("authService");
            authField.setAccessible(true);
            authField.set(fragment, mockAuth);

            Field repoField = NotifyEntrantsFragment.class.getDeclaredField("notificationRepository");
            repoField.setAccessible(true);
            repoField.set(fragment, mockNotifRepo);
        } catch (Exception e) {
            throw new RuntimeException("Mock injection failed", e);
        }
    }

    // -------------------------------------------------------------------------
    // UI state tests
    // -------------------------------------------------------------------------

    @Test
    public void testInitialState_noGroupsSelected_showsNoGroupsText() {
        launchFragment();

        onView(withId(R.id.recipientCount))
                .check(matches(withText("No groups selected")));
    }

    @Test
    public void testSelectingOneChip_showsOneGroupSelected() {
        launchFragment();

        onView(withId(R.id.chipWaitlist)).perform(click());

        onView(withId(R.id.recipientCount))
                .check(matches(withText("1 group selected")));
    }

    @Test
    public void testSelectingTwoChips_showsTwoGroupsSelected() {
        launchFragment();

        onView(withId(R.id.chipWaitlist)).perform(click());
        onView(withId(R.id.chipSelected)).perform(click());

        onView(withId(R.id.recipientCount))
                .check(matches(withText("2 groups selected")));
    }

    @Test
    public void testSelectingAllChips_showsThreeGroupsSelected() {
        launchFragment();

        onView(withId(R.id.chipWaitlist)).perform(click());
        onView(withId(R.id.chipSelected)).perform(click());
        onView(withId(R.id.chipCancelled)).perform(click());

        onView(withId(R.id.recipientCount))
                .check(matches(withText("3 groups selected")));
    }

    @Test
    public void testDeselectingChip_updatesCount() {
        launchFragment();

        onView(withId(R.id.chipWaitlist)).perform(click()); // select
        onView(withId(R.id.chipWaitlist)).perform(click()); // deselect

        onView(withId(R.id.recipientCount))
                .check(matches(withText("No groups selected")));
    }

    // -------------------------------------------------------------------------
    // Char counter tests
    // -------------------------------------------------------------------------

    @Test
    public void testTitleCharCounter_updatesAsUserTypes() {
        launchFragment();

        onView(withId(R.id.titleInput))
                .perform(typeText("Hello"), closeSoftKeyboard());

        onView(withId(R.id.titleCharCount))
                .check(matches(withText("5 / 50")));
    }

    @Test
    public void testMessageCharCounter_updatesAsUserTypes() {
        launchFragment();

        onView(withId(R.id.messageInput))
                .perform(typeText("Test message"), closeSoftKeyboard());

        onView(withId(R.id.charCount))
                .check(matches(withText("12 / 500")));
    }

    @Test
    public void testInitialCharCounters_showZero() {
        launchFragment();

        onView(withId(R.id.titleCharCount)).check(matches(withText("0 / 50")));
        onView(withId(R.id.charCount)).check(matches(withText("0 / 500")));
    }

    // -------------------------------------------------------------------------
    // Send validation tests — repo should NOT be called
    // -------------------------------------------------------------------------

    @Test
    public void testSend_emptyTitle_doesNotCallRepo() {
        launchFragment();

        onView(withId(R.id.chipWaitlist)).perform(click());
        onView(withId(R.id.messageInput))
                .perform(typeText("Some message"), closeSoftKeyboard());
        onView(withId(R.id.sendButton)).perform(click());

        verify(mockNotifRepo, never())
                .sendNotification(any(Notification.class), anyString(), anyList(), any());
    }

    @Test
    public void testSend_emptyMessage_doesNotCallRepo() {
        launchFragment();

        onView(withId(R.id.chipWaitlist)).perform(click());
        onView(withId(R.id.titleInput))
                .perform(typeText("My Title"), closeSoftKeyboard());
        onView(withId(R.id.sendButton)).perform(click());

        verify(mockNotifRepo, never())
                .sendNotification(any(Notification.class), anyString(), anyList(), any());
    }

    @Test
    public void testSend_noGroupSelected_doesNotCallRepo() {
        launchFragment();

        onView(withId(R.id.titleInput))
                .perform(typeText("My Title"), closeSoftKeyboard());
        onView(withId(R.id.messageInput))
                .perform(typeText("My message"), closeSoftKeyboard());
        onView(withId(R.id.sendButton)).perform(click());

        verify(mockNotifRepo, never())
                .sendNotification(any(Notification.class), anyString(), anyList(), any());
    }

    // -------------------------------------------------------------------------
    // Send success test — repo SHOULD be called with correct groups
    // -------------------------------------------------------------------------

    @Test
    public void testSend_validInputs_callsRepoAndClearsFields() {
        // Make the mock immediately invoke the callback with success=true
        org.mockito.Mockito.doAnswer(invocation -> {
            NotificationRepository.NotificationCallback cb =
                    invocation.getArgument(3);
            cb.onComplete(true);
            return null;
        }).when(mockNotifRepo).sendNotification(
                any(Notification.class), anyString(), anyList(), any());

        launchFragment();

        onView(withId(R.id.chipSelected)).perform(click());
        onView(withId(R.id.titleInput))
                .perform(typeText("Event Update"), closeSoftKeyboard());
        onView(withId(R.id.messageInput))
                .perform(typeText("Please check your status"), closeSoftKeyboard());
        onView(withId(R.id.sendButton)).perform(click());

        // Repo was called
        verify(mockNotifRepo).sendNotification(
                any(Notification.class),
                anyString(),
                anyList(),
                any());

        // Fields cleared after success
        onView(withId(R.id.titleInput)).check(matches(withText("")));
        onView(withId(R.id.messageInput)).check(matches(withText("")));
    }

    @Test
    public void testSend_validInputs_sendsCorrectGroups() {
        // Capture the groups list passed to the repo
        final List<?>[] capturedGroups = {null};

        org.mockito.Mockito.doAnswer(invocation -> {
            capturedGroups[0] = invocation.getArgument(2);
            NotificationRepository.NotificationCallback cb = invocation.getArgument(3);
            cb.onComplete(true);
            return null;
        }).when(mockNotifRepo).sendNotification(
                any(Notification.class), anyString(), anyList(), any());

        launchFragment();

        onView(withId(R.id.chipWaitlist)).perform(click());
        onView(withId(R.id.chipCancelled)).perform(click());
        onView(withId(R.id.titleInput))
                .perform(typeText("Title"), closeSoftKeyboard());
        onView(withId(R.id.messageInput))
                .perform(typeText("Body"), closeSoftKeyboard());
        onView(withId(R.id.sendButton)).perform(click());

        // Verify exactly WAITLIST and CANCELLED were sent, not SELECTED
        assert capturedGroups[0] != null;
        assert capturedGroups[0].contains("WAITLIST");
        assert capturedGroups[0].contains("CANCELLED");
        assert !capturedGroups[0].contains("SELECTED");
    }
}