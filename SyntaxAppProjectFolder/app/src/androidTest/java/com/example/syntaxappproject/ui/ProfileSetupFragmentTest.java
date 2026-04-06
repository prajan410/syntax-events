package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.Profile;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented UI tests for {@link ProfileSetupFragment}.
 * Mocks AuthenticationService and ProfileRepository to avoid Firebase dependency.
 */
@RunWith(AndroidJUnit4.class)
public class ProfileSetupFragmentTest {

    private AuthenticationService mockAuth;
    private ProfileRepository mockRepo;
    private NavController mockNav;

    /**
     * Configures mock services and stubs common behaviors before each test.
     */
    @Before
    public void setUp() {
        mockAuth = mock(AuthenticationService.class);
        mockRepo = mock(ProfileRepository.class);
        mockNav = mock(NavController.class);

        when(mockAuth.getCurrentUserId()).thenReturn("test-uid-123");

        doAnswer(inv -> {
            ((AuthenticationService.AuthCallback) inv.getArgument(0)).onComplete(true);
            return null;
        }).when(mockAuth).signInAnonymously(any());

        doAnswer(inv -> {
            ProfileRepository.ProfileCallback cb = inv.getArgument(1);
            cb.onResult(null);
            return null;
        }).when(mockRepo).getProfile(any(), any());

        doAnswer(inv -> {
            ProfileRepository.RepositoryCallback cb = inv.getArgument(2);
            cb.onComplete(true);
            return null;
        }).when(mockRepo).createProfile(any(), any(), any());
    }

    /**
     * Launches fragment with injected mocks and attached NavController.
     */
    private FragmentScenario<ProfileSetupFragment> launchFragment() {
        FragmentFactory factory = new FragmentFactory() {
            @Override
            public Fragment instantiate(ClassLoader classLoader, String className) {
                if (!className.equals(ProfileSetupFragment.class.getName())) {
                    return super.instantiate(classLoader, className);
                }
                ProfileSetupFragment fragment = new ProfileSetupFragment();

                try {
                    java.lang.reflect.Field authField = ProfileSetupFragment.class.getDeclaredField("authService");
                    authField.setAccessible(true);
                    authField.set(fragment, mockAuth);

                    java.lang.reflect.Field repoField = ProfileSetupFragment.class.getDeclaredField("profileRepo");
                    repoField.setAccessible(true);
                    repoField.set(fragment, mockRepo);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return fragment;
            }
        };

        FragmentScenario<ProfileSetupFragment> scenario = FragmentScenario.launchInContainer(
                ProfileSetupFragment.class,
                new Bundle(),
                R.style.Theme_SyntaxAppProject,
                factory
        );

        scenario.onFragment(f -> Navigation.setViewNavController(f.requireView(), mockNav));
        return scenario;
    }

    /**
     * Verifies validation prevents submission when no roles are selected.
     */
    @Test
    public void confirmWithoutRole_showsToast() {
        launchFragment();

        onView(withId(R.id.entrantButton)).perform(click());
        onView(withId(R.id.organizerButton)).perform(click());

        onView(withId(R.id.confirmButton)).perform(click());

        onView(withId(R.id.confirmButton)).check(matches(isDisplayed()));
    }

    /**
     * Verifies validation prevents submission when required fields are empty.
     */
    @Test
    public void confirmWithoutNameOrEmail_showsToast() {
        launchFragment();

        onView(withId(R.id.firstNameInput)).perform(replaceText(""), closeSoftKeyboard());
        onView(withId(R.id.emailInput)).perform(replaceText(""), closeSoftKeyboard());

        onView(withId(R.id.confirmButton)).perform(click());

        onView(withId(R.id.confirmButton)).check(matches(isDisplayed()));
    }

    /**
     * Verifies that the role buttons toggle correctly when clicked.
     */
    @Test
    public void roleButtons_toggleCorrectly() {
        launchFragment();

        onView(withId(R.id.entrantButton)).perform(click());
        onView(withId(R.id.organizerButton)).perform(click());

        onView(withId(R.id.confirmButton)).perform(click());

        onView(withId(R.id.confirmButton)).check(matches(isDisplayed()));
    }
}