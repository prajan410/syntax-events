package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
 * Instrumented tests for {@link SplashFragment} navigation routing.
 */
@RunWith(AndroidJUnit4.class)
public class SplashFragmentTest {

    private AuthenticationService mockAuth;
    private ProfileRepository mockRepo;
    private NavController mockNavController;
    private Profile fakeProfile;

    @Before
    public void setUp() {
        mockAuth = mock(AuthenticationService.class);
        mockRepo = mock(ProfileRepository.class);
        mockNavController = mock(NavController.class);

        doAnswer(inv -> "test-uid").when(mockAuth).getCurrentUserId();

        doAnswer(inv -> {
            AuthenticationService.AuthCallback cb = inv.getArgument(0);
            cb.onComplete(true);
            return null;
        }).when(mockAuth).signInAnonymously(any(AuthenticationService.AuthCallback.class));

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
    }

    private FragmentScenario<TestSplashFragment> launchFragment(Profile profileToReturn) {
        doAnswer(inv -> {
            String uid = inv.getArgument(0);
            ProfileRepository.ProfileCallback cb = inv.getArgument(1);
            cb.onResult(profileToReturn);
            return null;
        }).when(mockRepo).getProfile(anyString(), any(ProfileRepository.ProfileCallback.class));

        FragmentScenario<TestSplashFragment> scenario = FragmentScenario.launchInContainer(
                TestSplashFragment.class,
                new Bundle(),
                R.style.Theme_SyntaxAppProject,
                new FragmentFactory() {
                    @Override
                    public Fragment instantiate(ClassLoader classLoader, String className) {
                        if (className.equals(TestSplashFragment.class.getName())) {
                            TestSplashFragment fragment = new TestSplashFragment();
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

    @Test
    public void enterButton_newUser_navigatesToProfile() {
        launchFragment(null);
        onView(withId(R.id.enterButton)).perform(click());
        verify(mockNavController).navigate(R.id.action_splash_to_profile);
    }

    @Test
    public void enterButton_existingUser_navigatesToHome() {
        launchFragment(fakeProfile);
        onView(withId(R.id.enterButton)).perform(click());
        verify(mockNavController).navigate(R.id.action_splash_to_home);
    }

    @Test
    public void enterButton_adminUser_navigatesToAdmin() {
        Profile adminProfile = new Profile(
                "Admin User",
                "admin@example.com",
                null,
                false,
                false,
                true,
                true,
                "admin-uid"
        );
        launchFragment(adminProfile);
        onView(withId(R.id.enterButton)).perform(click());
        verify(mockNavController).navigate(R.id.action_splash_to_admin);
    }

    /**
     * Test subclass that bypasses heavy loading (events/images) and routes immediately.
     * We keep the same auth flow but skip the image‑caching loop.
     */
    public static class TestSplashFragment extends SplashFragment {

        @Override
        protected void handleEnterButton() {
            if (authService == null) {
                authService = new AuthenticationService();
            }

            authService.signInAnonymously(success -> {
                if (!success || !isAdded()) {
                    return;
                }

                String uid = authService.getCurrentUserId();
                navigateByProfile(uid);
            });
        }
    }
}
