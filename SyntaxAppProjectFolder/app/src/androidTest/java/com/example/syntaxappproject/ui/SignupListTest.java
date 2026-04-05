package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.EntrantNameAdapter;
import com.example.syntaxappproject.Profile;
import com.example.syntaxappproject.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class SignupListTest {

    public static class TestableEventSignupListFragment extends EventSignupListFragment {

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Profile alice = new Profile();
            alice.setName("Alice Johnson");
            alice.setEmail("alice@test.com");

            Profile bob = new Profile();
            bob.setName("Bob Smith");
            bob.setEmail("bob@test.com");

            Profile carol = new Profile();
            carol.setName("Carol Davis");
            carol.setEmail("carol@test.com");

            List<Profile> waiting   = new ArrayList<>(Arrays.asList(alice, bob, carol));
            List<Profile> selected  = new ArrayList<>(Arrays.asList(alice, bob));
            List<Profile> cancelled = new ArrayList<>(Collections.singletonList(carol));
            List<Profile> finalList = new ArrayList<>(Arrays.asList(alice, bob));

            requireActivity().runOnUiThread(() -> {
                try {
                    setAdapter("waitingAdapter",   waiting);
                    setAdapter("selectedAdapter",  selected);
                    setAdapter("cancelledAdapter", cancelled);
                    setAdapter("finalAdapter",     finalList);

                    setTextView("waitingCount",   String.valueOf(waiting.size()));
                    setTextView("selectedCount",  String.valueOf(selected.size()));
                    setTextView("cancelledCount", String.valueOf(cancelled.size()));
                    setTextView("finalCount",     String.valueOf(finalList.size()));

                    setViewVisibility("waitingEmpty",   View.GONE);
                    setViewVisibility("selectedEmpty",  View.GONE);
                    setViewVisibility("cancelledEmpty", View.GONE);
                    setViewVisibility("finalEmpty",     View.GONE);

                } catch (Exception e) {
                    throw new RuntimeException("Reflection injection failed: " + e.getMessage(), e);
                }
            });
        }

        private void setAdapter(String fieldName, List<Profile> profiles) throws Exception {
            Field f = EventSignupListFragment.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            EntrantNameAdapter adapter = (EntrantNameAdapter) f.get(this);
            adapter.setProfiles(profiles);
        }

        private void setTextView(String fieldName, String text) throws Exception {
            Field f = EventSignupListFragment.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            ((TextView) f.get(this)).setText(text);
        }

        private void setViewVisibility(String fieldName, int visibility) throws Exception {
            Field f = EventSignupListFragment.class.getDeclaredField(fieldName);
            f.setAccessible(true);
            ((View) f.get(this)).setVisibility(visibility);
        }
    }

    private Bundle makeBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", "fake_id");
        bundle.putBoolean("testingMode", true);
        return bundle;
    }

    private FragmentScenario<TestableEventSignupListFragment> launchTestable() {
        return FragmentScenario.launchInContainer(
                TestableEventSignupListFragment.class,
                makeBundle(),
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );
    }

    @Test
    public void testCountBadgesAreCorrect() {
        FragmentScenario<TestableEventSignupListFragment> scenario = launchTestable();

        scenario.onFragment(fragment -> {
            View v = fragment.requireView();
            assertEquals("3", ((TextView) v.findViewById(R.id.waitingCount)).getText().toString());
            assertEquals("2", ((TextView) v.findViewById(R.id.selectedCount)).getText().toString());
            assertEquals("1", ((TextView) v.findViewById(R.id.cancelledCount)).getText().toString());
            assertEquals("2", ((TextView) v.findViewById(R.id.finalCount)).getText().toString());
        });
    }

    @Test
    public void testSectionsToggleVisibility() {
        FragmentScenario<TestableEventSignupListFragment> scenario = launchTestable();

        scenario.onFragment(fragment -> {
            View v = fragment.requireView();
            View waitingContent = v.findViewById(R.id.waitingContent);
            View waitingHeader  = v.findViewById(R.id.waitingHeader);

            assertEquals(View.VISIBLE, waitingContent.getVisibility());

            waitingHeader.performClick();
            assertEquals(View.GONE, waitingContent.getVisibility());

            waitingHeader.performClick();
            assertEquals(View.VISIBLE, waitingContent.getVisibility());
        });
    }

    @Test
    public void testDownloadButtonExistsAndIsClickable() {
        FragmentScenario<EventSignupListFragment> scenario = FragmentScenario.launchInContainer(
                EventSignupListFragment.class,
                makeBundle(),
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        scenario.onFragment(fragment -> {
            View btn = fragment.requireView().findViewById(R.id.downloadFinalEntrantsBtn);
            assertNotNull(btn);
            btn.performClick();
        });
    }

    @Test
    public void testRecyclerViewsExist() {
        FragmentScenario<TestableEventSignupListFragment> scenario = launchTestable();

        scenario.onFragment(fragment -> {
            View v = fragment.requireView();
            assertNotNull(v.findViewById(R.id.waitingList));
            assertNotNull(v.findViewById(R.id.selectedList));
            assertNotNull(v.findViewById(R.id.finalList));
            assertNotNull(v.findViewById(R.id.cancelledList));
        });
    }
}