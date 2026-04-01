package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import java.lang.reflect.Field;

/**
 * Instrumented UI tests for {@link EditProfileFragment}.
 *
 * <p>Tests cover profile field population, save validation, navigation on save,
 * delete confirmation dialog display, delete confirmation navigation, and
 * delete cancellation behaviour.</p>
 *
 * <p>Mocks are injected into the fragment's private fields via reflection inside
 * a {@link FragmentFactory}, so the real {@link EditProfileFragment} class is
 * instantiated while dependencies on Firestore and Firebase Auth are fully isolated.</p>
 */
/*
@RunWith(AndroidJUnit4.class)
public class EditProfileFragmentTest {

    private AuthenticationService mockAuth;
    private ProfileRepository mockRepo;
    private NavController mockNavController;
    private Profile fakeProfile;

    @Before
    public void setUp() {
        mockAuth = mock(AuthenticationService.class);
        mockRepo = mock(ProfileRepository.class);
        mockNavController = mock(NavController.class);

        when(mockAuth.getCurrentUserId()).thenReturn("test-uid-123");

        fakeProfile = new Profile(
                "Jane Doe",
                "jane@example.com",
                "5551234567",
                true,
                false,
                false,
                true,
                false,
                "test-uid"
        );

        doAnswer(inv -> {
            ProfileRepository.ProfileCallback cb = inv.getArgument(1);
            cb.onResult(fakeProfile);
            return null;
        }).when(mockRepo).getProfile(anyString(), any());

        doAnswer(inv -> {
            ProfileRepository.RepositoryCallback cb = inv.getArgument(2);
            cb.onComplete(true);
            return null;
        }).when(mockRepo).updateProfile(anyString(), any(Profile.class), any());

        doAnswer(inv -> {
            ProfileRepository.RepositoryCallback cb = inv.getArgument(1);
            cb.onComplete(true);
            return null;
        }).when(mockRepo).deleteProfile(anyString(), any());
    }

    private FragmentScenario<EditProfileFragment> launchFragment() {
        FragmentFactory factory = new FragmentFactory() {
            @Override
            public Fragment instantiate(ClassLoader classLoader, String className) {
                if (!className.equals(EditProfileFragment.class.getName())) {
                    return super.instantiate(classLoader, className);
                }
                EditProfileFragment fragment = new EditProfileFragment();
                injectMocks(fragment);
                return fragment;
            }
        };

        FragmentScenario<EditProfileFragment> scenario = FragmentScenario.launchInContainer(
                EditProfileFragment.class,
                new Bundle(),
                R.style.Theme_SyntaxAppProject,
                factory
        );

        scenario.onFragment(fragment ->
                Navigation.setViewNavController(fragment.requireView(), mockNavController)
        );

        return scenario;
    }

    private void injectMocks(EditProfileFragment fragment) {
        try {
            Field repoField = EditProfileFragment.class.getDeclaredField("profileRepo");
            repoField.setAccessible(true);
            repoField.set(fragment, mockRepo);

            Field authField = EditProfileFragment.class.getDeclaredField("authService");
            authField.setAccessible(true);
            authField.set(fragment, mockAuth);
        } catch (Exception e) {
            throw new RuntimeException("Mock injection failed: " + e.getMessage(), e);
        }
    }

    @Test
    public void profileFields_arePopulatedOnLoad() {
        launchFragment();
        onView(withId(R.id.editFirstName)).check(matches(withText("Jane")));
        onView(withId(R.id.editLastName)).check(matches(withText("Doe")));
        onView(withId(R.id.editEmail)).check(matches(withText("jane@example.com")));
        onView(withId(R.id.editPhone)).check(matches(withText("5551234567")));
    }

    @Test
    public void saveEdit_withValidInput_navigatesUp() {
        launchFragment();
        onView(withId(R.id.editFirstName)).perform(clearText(), replaceText("John"), closeSoftKeyboard());
        onView(withId(R.id.editLastName)).perform(clearText(), replaceText("Smith"), closeSoftKeyboard());
        onView(withId(R.id.editEmail)).perform(clearText(), replaceText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.saveEdit)).perform(click());
        verify(mockNavController).navigateUp();
    }

    @Test
    public void saveEdit_emptyFirstName_showsToastAndStays() {
        launchFragment();
        onView(withId(R.id.editFirstName)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.editEmail)).perform(clearText(), replaceText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.saveEdit)).perform(click());
        onView(withId(R.id.saveEdit)).check(matches(isDisplayed()));
    }

    @Test
    public void saveEdit_emptyEmail_showsToastAndStays() {
        launchFragment();
        onView(withId(R.id.editFirstName)).perform(clearText(), replaceText("John"), closeSoftKeyboard());
        onView(withId(R.id.editEmail)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.saveEdit)).perform(click());
        onView(withId(R.id.saveEdit)).check(matches(isDisplayed()));
    }

    @Test
    public void deleteButton_showsConfirmationDialog() {
        launchFragment();
        onView(withId(R.id.deleteProfile)).perform(click());
        onView(withText("Delete Account")).check(matches(isDisplayed()));
    }

    @Test
    public void deleteConfirm_navigatesToSplash() {
        FragmentScenario<EditProfileFragment> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            Navigation.setViewNavController(fragment.requireView(), mockNavController);
        });

        onView(withId(R.id.deleteProfile)).perform(click());
        onView(withText("Delete")).perform(click());

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        verify(mockNavController).navigate(R.id.splashFragment);
    }

    @Test
    public void deleteCancel_dismissesDialog() {
        launchFragment();
        onView(withId(R.id.deleteProfile)).perform(click());
        onView(withText("Cancel")).perform(click());
        onView(withId(R.id.deleteProfile)).check(matches(isDisplayed()));
    }
}*/