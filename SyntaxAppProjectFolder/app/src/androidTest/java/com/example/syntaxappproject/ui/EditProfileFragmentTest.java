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
 * instantiated (required by {@link FragmentScenario}) while dependencies on
 * Firestore and Firebase Auth are fully isolated.</p>
 *
 * <p>The mock {@link NavController} is attached to the fragment's root view via
 * {@link Navigation#setViewNavController} immediately after launch. All navigation
 * calls in the fragment use {@link Navigation#findNavController(android.view.View)},
 * which reads from this view tag, making the mock reachable.</p>
 */
@RunWith(AndroidJUnit4.class)
public class EditProfileFragmentTest {

    /** Mock for {@link AuthenticationService} to return a fixed test UID. */
    private AuthenticationService mockAuth;

    /** Mock for {@link ProfileRepository} to stub Firestore operations. */
    private ProfileRepository mockRepo;

    /** Mock {@link NavController} to verify navigation calls without a real nav graph. */
    private NavController mockNavController;

    /** Fake profile returned by the stubbed {@link ProfileRepository#getProfile} call. */
    private Profile fakeProfile;

    /**
     * Sets up mocks and stubs before each test.
     *
     * <ul>
     *   <li>{@code mockAuth.getCurrentUserId()} returns {@code "test-uid-123"}</li>
     *   <li>{@code mockRepo.getProfile()} immediately calls back with {@code fakeProfile}</li>
     *   <li>{@code mockRepo.updateProfile()} immediately calls back with {@code success = true}</li>
     *   <li>{@code mockRepo.deleteProfile()} immediately calls back with {@code success = true}</li>
     * </ul>
     */
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
                "test-uid-123"
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

    /**
     * Launches {@link EditProfileFragment} in isolation using a {@link FragmentFactory}
     * that injects mocks via reflection before the fragment lifecycle begins.
     *
     * <p>After launch, attaches the mock {@link NavController} to the fragment's root
     * view so that all {@code Navigation.findNavController(requireView())} calls resolve
     * to the mock.</p>
     *
     * @return the active {@link FragmentScenario} for further interaction
     */
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

    /**
     * Injects {@link #mockRepo} and {@link #mockAuth} into the private fields of
     * the given fragment instance using reflection.
     *
     * @param fragment the fragment instance to inject mocks into
     * @throws RuntimeException if reflection access fails
     */
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

    /**
     * Verifies that the form fields are populated with the data from the
     * loaded profile when the fragment is first displayed.
     *
     * <p>Expects first name "Jane", last name "Doe", email "jane@example.com",
     * and phone "5551234567" from {@link #fakeProfile}.</p>
     */
    @Test
    public void profileFields_arePopulatedOnLoad() {
        launchFragment();
        onView(withId(R.id.editFirstName)).check(matches(withText("Jane")));
        onView(withId(R.id.editLastName)).check(matches(withText("Doe")));
        onView(withId(R.id.editEmail)).check(matches(withText("jane@example.com")));
        onView(withId(R.id.editPhone)).check(matches(withText("5551234567")));
    }

    /**
     * Verifies that submitting the form with a valid first name and email
     * results in a call to {@link NavController#navigateUp()}.
     */
    @Test
    public void saveEdit_withValidInput_navigatesUp() {
        launchFragment();
        onView(withId(R.id.editFirstName)).perform(clearText(), replaceText("John"), closeSoftKeyboard());
        onView(withId(R.id.editLastName)).perform(clearText(), replaceText("Smith"), closeSoftKeyboard());
        onView(withId(R.id.editEmail)).perform(clearText(), replaceText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.saveEdit)).perform(click());
        verify(mockNavController).navigateUp();
    }

    /**
     * Verifies that submitting the form with an empty first name does not
     * navigate away — the save button remains visible on screen.
     */
    @Test
    public void saveEdit_emptyFirstName_showsToastAndStays() {
        launchFragment();
        onView(withId(R.id.editFirstName)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.editEmail)).perform(clearText(), replaceText("john@example.com"), closeSoftKeyboard());
        onView(withId(R.id.saveEdit)).perform(click());
        onView(withId(R.id.saveEdit)).check(matches(isDisplayed()));
    }

    /**
     * Verifies that submitting the form with an empty email does not
     * navigate away — the save button remains visible on screen.
     */
    @Test
    public void saveEdit_emptyEmail_showsToastAndStays() {
        launchFragment();
        onView(withId(R.id.editFirstName)).perform(clearText(), replaceText("John"), closeSoftKeyboard());
        onView(withId(R.id.editEmail)).perform(clearText(), closeSoftKeyboard());
        onView(withId(R.id.saveEdit)).perform(click());
        onView(withId(R.id.saveEdit)).check(matches(isDisplayed()));
    }

    /**
     * Verifies that tapping the delete button shows the confirmation
     * {@link android.app.AlertDialog} with the title "Delete Account".
     */
    @Test
    public void deleteButton_showsConfirmationDialog() {
        launchFragment();
        onView(withId(R.id.deleteProfile)).perform(click());
        onView(withText("Delete Account")).check(matches(isDisplayed()));
    }

    /**
     * Verifies that confirming the delete dialog triggers navigation to
     * {@code R.id.splashFragment} via the mock {@link NavController}.
     */
    @Test
    public void deleteConfirm_navigatesToSplash() {
        launchFragment();
        onView(withId(R.id.deleteProfile)).perform(click());
        onView(withText("Delete")).perform(click());
        verify(mockNavController).navigate(R.id.splashFragment);
    }

    /**
     * Verifies that cancelling the delete dialog dismisses it without
     * navigating away — the delete button remains visible on screen.
     */
    @Test
    public void deleteCancel_dismissesDialog() {
        launchFragment();
        onView(withId(R.id.deleteProfile)).perform(click());
        onView(withText("Cancel")).perform(click());
        onView(withId(R.id.deleteProfile)).check(matches(isDisplayed()));
    }
}
