package com.example.syntaxappproject.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.syntaxappproject.PrivateEventInvitationRepository;
import com.example.syntaxappproject.Profile;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class PrivateEventInviteFragmentTest {

    private NavController mockNavController;

    static class FakePrivateEventInvitationRepository extends PrivateEventInvitationRepository {
        List<Profile> searchResults = new ArrayList<>();
        List<String> searchIds = new ArrayList<>();

        boolean sendInvitationResult = true;
        boolean addInvitedUserResult = true;

        String lastSearchQuery = null;
        String lastEventId = null;
        String lastEventName = null;
        String lastUserId = null;

        @Override
        public void searchEntrants(String query, SearchCallback callback) {
            lastSearchQuery = query;
            callback.onResult(searchResults, searchIds);
        }

        @Override
        public void sendInvitation(String eventId, String eventName, String userId, SimpleCallback callback) {
            lastEventId = eventId;
            lastEventName = eventName;
            lastUserId = userId;
            callback.onComplete(sendInvitationResult);
        }

        @Override
        public void addInvitedUserToEvent(String eventId, String userId, SimpleCallback callback) {
            lastEventId = eventId;
            lastUserId = userId;
            callback.onComplete(addInvitedUserResult);
        }
    }

    /**
     * Initializes the mock navigation controller before each test.
     */
    @Before
    public void setUp() {
        mockNavController = mock(NavController.class);
    }

    /**
     * Creates a test profile with given values.
     *
     * @param name profile name
     * @param email profile email
     * @param phone profile phone
     * @return a Profile instance
     */
    private Profile makeProfile(String name, String email, String phone) {
        Profile profile = new Profile();
        profile.setName(name);
        profile.setEmail(email);
        profile.setPhone(phone);
        return profile;
    }

    /**
     * Injects a fake repository into the fragment using reflection.
     *
     * @param fragment the fragment instance
     * @param repository the fake repository to inject
     */
    private void injectRepository(PrivateEventInviteFragment fragment,
                                  PrivateEventInvitationRepository repository) {
        try {
            Field repoField = PrivateEventInviteFragment.class.getDeclaredField("repository");
            repoField.setAccessible(true);
            repoField.set(fragment, repository);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Launches the fragment with arguments and injects the fake repository.
     *
     * @param args fragment arguments
     * @param fakeRepo fake repository instance
     * @return FragmentScenario instance
     */
    private FragmentScenario<PrivateEventInviteFragment> launchFragment(Bundle args,
                                                                        FakePrivateEventInvitationRepository fakeRepo) {
        FragmentScenario<PrivateEventInviteFragment> scenario = FragmentScenario.launchInContainer(
                PrivateEventInviteFragment.class,
                args,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar,
                (FragmentFactory) null
        );

        scenario.onFragment(fragment -> {
            Navigation.setViewNavController(fragment.requireView(), mockNavController);
            injectRepository(fragment, fakeRepo);
        });

        return scenario;
    }

    /**
     * Verifies that all required UI components are displayed.
     */
    @Test
    public void testScreenDisplays() {
        FakePrivateEventInvitationRepository fakeRepo = new FakePrivateEventInvitationRepository();

        Bundle args = new Bundle();
        args.putString("eventId", "event-1");
        args.putString("eventName", "Private Party");

        FragmentScenario<PrivateEventInviteFragment> scenario = launchFragment(args, fakeRepo);

        scenario.onFragment(fragment -> {
            View root = fragment.requireView();

            assertNotNull(root.findViewById(R.id.searchInput));
            assertNotNull(root.findViewById(R.id.searchButton));
            assertNotNull(root.findViewById(R.id.doneButton));
            assertNotNull(root.findViewById(R.id.resultText));
            assertNotNull(root.findViewById(R.id.inviteRecyclerView));
        });
    }

    /**
     * Verifies that clicking the done button navigates back to organizer events.
     */
    @Test
    public void testDoneButtonNavigatesBack() {
        FakePrivateEventInvitationRepository fakeRepo = new FakePrivateEventInvitationRepository();

        Bundle args = new Bundle();
        args.putString("eventId", "event-1");
        args.putString("eventName", "Private Party");

        FragmentScenario<PrivateEventInviteFragment> scenario = launchFragment(args, fakeRepo);

        scenario.onFragment(fragment -> {
            MaterialButton doneButton = fragment.requireView().findViewById(R.id.doneButton);
            doneButton.performClick();
        });

        verify(mockNavController).navigate(R.id.organizerEventsFragment);
    }

    /**
     * Verifies that a successful search updates result text and RecyclerView data.
     */
    @Test
    public void testSearchUpdatesResultTextAndList() {
        FakePrivateEventInvitationRepository fakeRepo = new FakePrivateEventInvitationRepository();

        fakeRepo.searchResults.add(makeProfile("Alice", "alice@test.com", "111"));
        fakeRepo.searchResults.add(makeProfile("Bob", "bob@test.com", "222"));
        fakeRepo.searchIds.add("u1");
        fakeRepo.searchIds.add("u2");

        Bundle args = new Bundle();
        args.putString("eventId", "event-1");
        args.putString("eventName", "Private Party");

        FragmentScenario<PrivateEventInviteFragment> scenario = launchFragment(args, fakeRepo);

        scenario.onFragment(fragment -> {
            TextInputEditText searchInput = fragment.requireView().findViewById(R.id.searchInput);
            MaterialButton searchButton = fragment.requireView().findViewById(R.id.searchButton);
            TextView resultText = fragment.requireView().findViewById(R.id.resultText);
            RecyclerView recyclerView = fragment.requireView().findViewById(R.id.inviteRecyclerView);

            searchInput.setText("alice");
            searchButton.performClick();

            assertEquals("alice", fakeRepo.lastSearchQuery);
            assertEquals("Found 2 result(s)", resultText.getText().toString());
            assertEquals(2, recyclerView.getAdapter().getItemCount());
        });
    }

    /**
     * Verifies that no results message is shown when search returns empty.
     */
    @Test
    public void testSearchNoResultsShowsMessage() {
        FakePrivateEventInvitationRepository fakeRepo = new FakePrivateEventInvitationRepository();

        Bundle args = new Bundle();
        args.putString("eventId", "event-1");
        args.putString("eventName", "Private Party");

        FragmentScenario<PrivateEventInviteFragment> scenario = launchFragment(args, fakeRepo);

        scenario.onFragment(fragment -> {
            TextInputEditText searchInput = fragment.requireView().findViewById(R.id.searchInput);
            MaterialButton searchButton = fragment.requireView().findViewById(R.id.searchButton);
            TextView resultText = fragment.requireView().findViewById(R.id.resultText);
            RecyclerView recyclerView = fragment.requireView().findViewById(R.id.inviteRecyclerView);

            searchInput.setText("nobody");
            searchButton.performClick();

            assertEquals("nobody", fakeRepo.lastSearchQuery);
            assertEquals("No entrants found", resultText.getText().toString());
            assertEquals(0, recyclerView.getAdapter().getItemCount());
        });
    }
}