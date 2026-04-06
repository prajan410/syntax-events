package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.Bundle;
import android.os.IBinder;
import android.view.WindowManager;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.espresso.Root;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.CoOrganizerRepository;
import com.example.syntaxappproject.Profile;
import com.example.syntaxappproject.R;


import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Instrumented tests for {@link HomeFragment}.
 * This version uses a TestHomeFragment subclass to override setupHotbar()
 * and avoid NavHostFragment navigation crashes during isolation testing.
 */
@RunWith(AndroidJUnit4.class)
public class CoOrganizerInviteFragmentTest {

    private NavController mockNavController;

    /**
     * Fake repo to bypass database
     */
    public static class FakeRepo extends CoOrganizerRepository {

        ArrayList<Profile> fakeProfiles = new ArrayList<>();
        ArrayList<String> fakeUserIds = new ArrayList<>();
        @Override
        public void searchEntrants(String query, searchCallback callback) {
            // 直接返回 fake 数据
            callback.onResult(fakeProfiles, fakeUserIds);
        }

    }

    /**
     * Test Fragment to inject fake repo
     */
    public static class TestFragment extends CoOrganizerInviteFragment {}

    private FakeRepo fakeRepo;

    @Before
    public void setUp() {
        mockNavController = mock(NavController.class);
        fakeRepo = new FakeRepo();
    }

    /**
     * Launch fragment with fake repo
     */
    private FragmentScenario<TestFragment> launchFragment() {

        FragmentFactory factory = new FragmentFactory() {
            @Override
            public Fragment instantiate(ClassLoader classLoader, String className) {
                if (!className.equals(TestFragment.class.getName())) {
                    return super.instantiate(classLoader, className);
                }

                TestFragment fragment = new TestFragment();

                try {
                    Field repoField =
                            CoOrganizerInviteFragment.class.getDeclaredField("repo");
                    repoField.setAccessible(true);
                    repoField.set(fragment, fakeRepo);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                return fragment;
            }
        };

        FragmentScenario<TestFragment> scenario =
                FragmentScenario.launchInContainer(
                        TestFragment.class,
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
     * Test if the ui display.
     */
    @Test
    public void testElementsDisplayed() {
        launchFragment();

        onView(withId(R.id.searchInput)).check(matches(isDisplayed()));
        onView(withId(R.id.searchButton)).check(matches(isDisplayed()));
        onView(withId(R.id.resultText)).check(matches(isDisplayed()));
        onView(withId(R.id.inviteRecyclerView)).check(matches(isDisplayed()));
    }

    /**
     * Test the emptyInput in search.
     */
    @Test
    public void testSearch_emptyInput_doesNotChangeUI() {
        launchFragment();

        onView(withId(R.id.searchButton))
                .perform(click());

        onView(withId(R.id.resultText))
                .check(matches(withText("Search results")));
    }

    /**
     * Test search with result.
     */
    @Test
    public void testSearch_withResults_updatesRecyclerView() {

        fakeRepo.fakeProfiles.add(new Profile());
        fakeRepo.fakeUserIds.add("user1");

        launchFragment();

        onView(withId(R.id.searchInput))
                .perform(typeText("test"));

        onView(withId(R.id.searchButton))
                .perform(click());

        onView(withText("Found 1 result(s)"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test search with no result.
     */
    @Test
    public void testSearch_noResults_showsNoEntrants() {

        launchFragment();

        onView(withId(R.id.searchInput))
                .perform(typeText("xyz"));

        onView(withId(R.id.searchButton))
                .perform(click());

        onView(withText("No entrants found"))
                .check(matches(isDisplayed()));
    }

    /**
     * Test navigate of done button
     */
    @Test
    public void testClickingDone_navigates() {
        launchFragment();

        onView(withId(R.id.doneButton))
                .perform(click());

        verify(mockNavController).navigate(R.id.notifyEntrantsFragment);
    }
}
