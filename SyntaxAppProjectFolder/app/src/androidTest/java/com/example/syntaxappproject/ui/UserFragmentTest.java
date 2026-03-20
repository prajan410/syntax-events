package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
 * Instrumented tests for {@link UserFragment} navigation and profile display.
 */
@RunWith(AndroidJUnit4.class)
public class UserFragmentTest {

    private AuthenticationService mockAuth;
    private ProfileRepository mockRepo;
    private NavController mockNavController;
    private Profile fakeProfile;

    /**
     * Initializes mocks and test profile before each test method.
     */
    @Before
    public void setUp() {
        mockAuth = mock(AuthenticationService.class);
        mockRepo = mock(ProfileRepository.class);
        mockNavController = mock(NavController.class);

        fakeProfile = new Profile(
                "Jane Doe",
                "jane@example.com",
                "5551234567",
                true,
                false,
                false,
                true,
                "test-uid"
        );

        doAnswer(inv -> "test-uid").when(mockAuth).getCurrentUserId();
    }

    /**
     * Launches the fragment with injected mocks and a configured profile response.
     *
     * @param profileToReturn the {@link Profile} the mock repo will return, or {@code null}
     * @return the active {@link FragmentScenario}
     */
    private FragmentScenario<TestUserFragment> launchFragment(Profile profileToReturn) {
        doAnswer(inv -> {
            ProfileRepository.ProfileCallback cb = inv.getArgument(1);
            cb.onResult(profileToReturn);
            return null;
        }).when(mockRepo).getProfile(anyString(), any(ProfileRepository.ProfileCallback.class));

        FragmentScenario<TestUserFragment> scenario = FragmentScenario.launchInContainer(
                TestUserFragment.class,
                null,
                R.style.Theme_SyntaxAppProject,
                new FragmentFactory() {
                    @Override
                    public Fragment instantiate(ClassLoader classLoader, String className) {
                        if (className.equals(TestUserFragment.class.getName())) {
                            TestUserFragment fragment = new TestUserFragment();
                            fragment.setAuthService(mockAuth);
                            fragment.setProfileRepo(mockRepo);
                            return fragment;
                        }
                        return super.instantiate(classLoader, className);
                    }
                }
        );

        scenario.onFragment(fragment ->
                Navigation.setViewNavController(fragment.requireView(), mockNavController)
        );

        return scenario;
    }

    /**
     * Verifies personalization button navigates to edit profile screen.
     */
    @Test
    public void personalizationButton_navigatesToEditProfile() {
        launchFragment(fakeProfile);
        onView(withId(R.id.personalizationButton)).perform(click());
        verify(mockNavController).navigate(R.id.editProfileFragment);
    }

    /**
     * Verifies event history button navigates to history screen.
     */
    @Test
    public void eventHistoryButton_navigatesToHistory() {
        launchFragment(fakeProfile);
        onView(withId(R.id.eventHistoryButton)).perform(click());
        verify(mockNavController).navigate(R.id.eventHistoryFragment);
    }

    /**
     * Verifies profile name and email display correctly after load.
     */
    @Test
    public void profileLoadsAndDisplaysCorrectly() {
        launchFragment(fakeProfile);
        onView(withId(R.id.profileName)).check(matches(withText("Jane Doe")));
        onView(withId(R.id.profileEmail)).check(matches(withText("jane@example.com")));
    }

    /**
     * Test subclass that skips hotbar setup to avoid NavHostFragment crashes in isolation.
     */
    public static class TestUserFragment extends UserFragment {
        @Override
        protected void setupHotbar(android.view.View view) {}
    }
}
