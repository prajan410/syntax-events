package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.EntrantHomeRepository;
import com.example.syntaxappproject.EventDetail;
import com.example.syntaxappproject.EventJoinRepository;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Instrumented tests for {@link HomeFragment}.
 * This version uses a TestHomeFragment subclass to override setupHotbar()
 * and avoid NavHostFragment navigation crashes during isolation testing.
 */
@RunWith(AndroidJUnit4.class)
public class HomeFragmentTest {

    private AuthenticationService mockAuth;
    private EntrantHomeRepository mockRepo;
    private EventJoinRepository mockJoinRepo;
    private ProfileRepository mockProfileRepo;
    private NavController mockNavController;

    private EventDetail fakeEvent;

    /**
     * Initializes mocks and test data.
     */
    @Before
    public void setUp() {
        mockAuth = mock(AuthenticationService.class);
        mockRepo = mock(EntrantHomeRepository.class);
        mockJoinRepo = mock(EventJoinRepository.class);
        mockProfileRepo = mock(ProfileRepository.class);
        mockNavController = mock(NavController.class);

        when(mockAuth.getCurrentUserId()).thenReturn("test-uid");

        fakeEvent = new EventDetail(
                "event123",
                "Test Event",
                "desc",
                "loc",
                10,
                false,
                "2026-01-01",
                "2026-12-31",
                "2026-01-01",
                "2026-12-31",
                0,
                "",
                ""
        );
    }

    /**
     * Test fragment subclass to disable NavHost dependency.
     */
    public static class TestHomeFragment extends HomeFragment {
        @Override
        protected void setupHotbar(android.view.View view) {
            // Prevent NavHostFragment crash
        }

    }

    /**
     * Launch fragment with injected mocks.
     */
    private FragmentScenario<TestHomeFragment> launchFragment() {

        FragmentFactory factory = new FragmentFactory() {
            @Override
            public Fragment instantiate(ClassLoader classLoader, String className) {
                if (!className.equals(TestHomeFragment.class.getName())) {
                    return super.instantiate(classLoader, className);
                }
                TestHomeFragment fragment = new TestHomeFragment();
                injectMocks(fragment);
                return fragment;
            }
        };

        FragmentScenario<TestHomeFragment> scenario =
                FragmentScenario.launchInContainer(
                        TestHomeFragment.class,
                        new Bundle(),
                        R.style.Theme_SyntaxAppProject,
                        factory
                );

        scenario.onFragment(fragment -> {

            Navigation.setViewNavController(fragment.requireView(), mockNavController);

            List<EventDetail> list = new ArrayList<>();
            list.add(fakeEvent);

            fragment.setEventsForTest(list);

            RecyclerView recyclerView =
                    fragment.requireView().findViewById(R.id.eventList);
            recyclerView.setVisibility(android.view.View.VISIBLE);
        });

        return scenario;
    }

    /**
     * Inject mock dependencies via reflection.
     */
    private void injectMocks(HomeFragment fragment) {
        try {
            Field authField = HomeFragment.class.getDeclaredField("authService");
            authField.setAccessible(true);
            authField.set(fragment, mockAuth);

            Field repoField = HomeFragment.class.getDeclaredField("entrantHomeRepo");
            repoField.setAccessible(true);
            repoField.set(fragment, mockRepo);

            Field joinField = HomeFragment.class.getDeclaredField("joinRepo");
            joinField.setAccessible(true);
            joinField.set(fragment, mockJoinRepo);

            Field profileField = HomeFragment.class.getDeclaredField("profileRepo");
            profileField.setAccessible(true);
            profileField.set(fragment, mockProfileRepo);

        } catch (Exception e) {
            throw new RuntimeException("Mock injection failed", e);
        }
    }

    /**
     * Test search filters results.
     */
    @Test
    public void testSearch_filtersEvent() {
        launchFragment();

        onView(withId(R.id.searchInput))
                .perform(typeText("Test"));

        onView(withText("Test Event"))
                .check(matches(isDisplayed()));
    }
    /**
     * Test event name is displayed.
     */
    @Test
    public void testRecyclerView_displaysEventTitle() {
        launchFragment();

        onView(withText("Test Event"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test clicking event navigates to detail screen.
     */
    @Test
    public void testClickingEvent_navigatesToEventDetail() {
        launchFragment();

        onView(withId(R.id.eventList))
                .perform(actionOnItemAtPosition(0, click()));

        verify(mockNavController).navigate(
                eq(R.id.toEventDetailFragment),
                any(Bundle.class)
        );
    }

    /**
     * Test with different case search.
     */
    @Test
    public void testSearch_caseInsensitive() {
        FragmentScenario<TestHomeFragment> scenario = launchFragment();

        scenario.onFragment(fragment -> {
            try {
                List<EventDetail> list = new ArrayList<>();
                list.add(fakeEvent);

                Field allEvents = HomeFragment.class.getDeclaredField("allEvents");
                allEvents.setAccessible(true);
                allEvents.set(fragment, list);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        onView(withId(R.id.searchInput))
                .perform(typeText("test"));

        onView(withText("Test Event"))
                .check(matches(isDisplayed()));
    }

}