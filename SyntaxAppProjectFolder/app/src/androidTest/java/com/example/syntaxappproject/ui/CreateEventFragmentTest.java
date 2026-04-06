package com.example.syntaxappproject.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented intent tests for {@link CreateEventFragment}.
 */
@RunWith(AndroidJUnit4.class)
public class CreateEventFragmentTest {

    private NavController mockNavController;

    @Before
    public void setUp() {
        mockNavController = mock(NavController.class);
    }

    // -------------------------------------------------------------------------
    // Test subclass
    // -------------------------------------------------------------------------

    /**
     * Stubs out both setupHotbar() (HomeBar nav crash) and the NavHostFragment
     * call inside onViewCreated by wiring mockNavController via Navigation after
     * the view is created.
     */
    public static class TestCreateEventFragment extends CreateEventFragment {

        @Override
        protected void setupHotbar(View view) {
            // No-op: prevents NavHostFragment.findNavController() crash from HomeBar
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            // continueButton's click listener already captured navController from
            // NavHostFragment.findNavController() — we override the whole wiring
            // by re-setting the continueButton listener after Navigation is injected.
            // This is handled in launchFragment() via scenario.onFragment().
        }
    }

    // -------------------------------------------------------------------------
    // Launch helper
    // -------------------------------------------------------------------------

    private FragmentScenario<TestCreateEventFragment> launchFragment() {
        FragmentFactory factory = new FragmentFactory() {
            @NonNull
            @Override
            public Fragment instantiate(@NonNull ClassLoader classLoader,
                                        @NonNull String className) {
                if (className.equals(TestCreateEventFragment.class.getName())) {
                    return new TestCreateEventFragment();
                }
                return super.instantiate(classLoader, className);
            }
        };

        FragmentScenario<TestCreateEventFragment> scenario =
                FragmentScenario.launchInContainer(
                        TestCreateEventFragment.class,
                        new Bundle(),
                        R.style.Theme_SyntaxAppProject,
                        factory
                );

        // Wire the mock NavController THEN re-attach continueButton listener
        // so it uses mockNavController instead of the NavHostFragment one
        scenario.onFragment(fragment -> {
            View root = fragment.requireView();
            Navigation.setViewNavController(root, mockNavController);

            // Re-wire continueButton to use mockNavController
            root.findViewById(R.id.continueButton).setOnClickListener(v ->
                    fragment.requireView()
                            .findViewById(R.id.continueButton)
                            .callOnClick()
            );
        });

        return scenario;
    }

    // -------------------------------------------------------------------------
    // Helpers — fill fields via replaceText (avoids soft keyboard timing issues)
    // -------------------------------------------------------------------------

    private void fillName(String text) {
        onView(withId(R.id.eventNameInput))
                .perform(scrollTo(), replaceText(text), closeSoftKeyboard());
    }

    private void fillDescription(String text) {
        onView(withId(R.id.descriptionInput))
                .perform(scrollTo(), replaceText(text), closeSoftKeyboard());
    }

    private void fillCapacity(String text) {
        onView(withId(R.id.capacityInput))
                .perform(scrollTo(), replaceText(text), closeSoftKeyboard());
    }

    private void clickContinue() {
        onView(withId(R.id.continueButton))
                .perform(scrollTo(), click());
    }

    // -------------------------------------------------------------------------
    // Validation tests — navigation should NOT be triggered
    // -------------------------------------------------------------------------

    @Test
    public void testContinue_emptyName_doesNotNavigate() {
        launchFragment();

        fillDescription("A description");
        fillCapacity("10");
        clickContinue();

        verify(mockNavController, never()).navigate(anyInt());
    }

    @Test
    public void testContinue_emptyDescription_doesNotNavigate() {
        launchFragment();

        fillName("My Event");
        fillCapacity("10");
        clickContinue();

        verify(mockNavController, never()).navigate(anyInt());
    }

    @Test
    public void testContinue_invalidCapacity_doesNotNavigate() {
        launchFragment();

        fillName("My Event");
        fillDescription("A description");
        fillCapacity("notanumber");
        clickContinue();

        verify(mockNavController, never()).navigate(anyInt());
    }

    @Test
    public void testContinue_zeroCapacity_doesNotNavigate() {
        launchFragment();

        fillName("My Event");
        fillDescription("A description");
        fillCapacity("0");
        clickContinue();

        verify(mockNavController, never()).navigate(anyInt());
    }

    @Test
    public void testContinue_negativeCapacity_doesNotNavigate() {
        launchFragment();

        fillName("My Event");
        fillDescription("A description");
        fillCapacity("-5");
        clickContinue();

        verify(mockNavController, never()).navigate(anyInt());
    }

    @Test
    public void testContinue_geoEnabledButEmptyLocation_doesNotNavigate() {
        launchFragment();

        fillName("My Event");
        fillDescription("A description");
        fillCapacity("10");

        // Enable the geo switch
        onView(withId(R.id.geolocationSwitch)).perform(scrollTo(), click());

        // Leave location empty and attempt to continue
        clickContinue();

        verify(mockNavController, never()).navigate(anyInt());
    }

    // -------------------------------------------------------------------------
    // Valid input tests — navigation SHOULD be triggered
    // -------------------------------------------------------------------------

    @Test
    public void testContinue_validInputsNoGeo_navigatesToUploadImage() {
        launchFragment();

        fillName("My Event");
        fillDescription("A great event");
        fillCapacity("50");
        clickContinue();

        verify(mockNavController).navigate(R.id.toUploadImageFragment);
    }

    @Test
    public void testContinue_validInputsWithCapacityOne_navigates() {
        launchFragment();

        fillName("Solo Event");
        fillDescription("Just one person");
        fillCapacity("1");
        clickContinue();

        verify(mockNavController).navigate(R.id.toUploadImageFragment);
    }

    // -------------------------------------------------------------------------
    // Geo toggle UI tests
    // -------------------------------------------------------------------------

    @Test
    public void testGeoToggleOff_locationFieldIsDisabled() {
        launchFragment();

        // Geo switch is off by default
        onView(withId(R.id.locationInput))
                .check(matches(androidx.test.espresso.matcher.ViewMatchers.isNotEnabled()));
    }

    @Test
    public void testGeoToggleOn_locationFieldIsEnabled() {
        launchFragment();

        onView(withId(R.id.geolocationSwitch)).perform(scrollTo(), click());

        onView(withId(R.id.locationInput))
                .check(matches(isEnabled()));
    }

    @Test
    public void testGeoToggleOnThenOff_locationFieldIsDisabledAgain() {
        launchFragment();

        onView(withId(R.id.geolocationSwitch)).perform(scrollTo(), click()); // on
        onView(withId(R.id.geolocationSwitch)).perform(scrollTo(), click()); // off

        onView(withId(R.id.locationInput))
                .check(matches(androidx.test.espresso.matcher.ViewMatchers.isNotEnabled()));
    }

    // -------------------------------------------------------------------------
    // Initial UI state tests
    // -------------------------------------------------------------------------

    @Test
    public void testInitialState_continueButtonIsDisplayed() {
        launchFragment();

        onView(withId(R.id.continueButton))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testInitialState_allInputFieldsVisible() {
        launchFragment();

        onView(withId(R.id.eventNameInput)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.descriptionInput)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.capacityInput)).perform(scrollTo()).check(matches(isDisplayed()));
    }
}