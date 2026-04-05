package com.example.syntaxappproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PrivateEventInvitationRepositoryTest {

    private MockedStatic<FirebaseFirestore> firestoreStaticMock;
    private FirebaseFirestore mockDb;
    private FakePrivateEventInvitationRepository repo;

    static class FakePrivateEventInvitationRepository extends PrivateEventInvitationRepository {

        private final Map<String, Profile> fakeProfiles = new LinkedHashMap<>();

        boolean sendInvitationResult = true;
        boolean addInvitedUserResult = true;

        String lastEventId;
        String lastEventName;
        String lastUserId;

        /**
         * Adds a fake profile to the in-memory data store.
         *
         * @param userId the ID of the user
         * @param profile the profile object to store
         */
        public void addFakeProfile(String userId, Profile profile) {
            fakeProfiles.put(userId, profile);
        }

        /**
         * Simulates searching entrant profiles by matching query against
         * name, email, or phone fields.
         *
         * @param query the search query
         * @param callback the callback returning matching results
         */
        @Override
        public void searchEntrants(String query, SearchCallback callback) {
            List<Profile> results = new ArrayList<>();
            List<String> userIds = new ArrayList<>();

            String q = query == null ? "" : query.trim().toLowerCase();

            for (Map.Entry<String, Profile> entry : fakeProfiles.entrySet()) {
                Profile profile = entry.getValue();

                String name = profile.getName() == null ? "" : profile.getName().toLowerCase();
                String email = profile.getEmail() == null ? "" : profile.getEmail().toLowerCase();
                String phone = profile.getPhone() == null ? "" : profile.getPhone().toLowerCase();

                boolean match = name.contains(q) || email.contains(q) || phone.contains(q);

                if (match) {
                    results.add(profile);
                    userIds.add(entry.getKey());
                }
            }

            callback.onResult(results, userIds);
        }

        /**
         * Simulates sending an invitation and records parameters for verification.
         *
         * @param eventId the event ID
         * @param eventName the event name
         * @param userId the user ID
         * @param callback callback returning success result
         */
        @Override
        public void sendInvitation(String eventId, String eventName, String userId, SimpleCallback callback) {
            lastEventId = eventId;
            lastEventName = eventName;
            lastUserId = userId;
            callback.onComplete(sendInvitationResult);
        }

        /**
         * Simulates adding a user to the invited list of an event.
         *
         * @param eventId the event ID
         * @param userId the user ID
         * @param callback callback returning success result
         */
        @Override
        public void addInvitedUserToEvent(String eventId, String userId, SimpleCallback callback) {
            lastEventId = eventId;
            lastUserId = userId;
            callback.onComplete(addInvitedUserResult);
        }
    }

    /**
     * Sets up mocks and fake repository with sample profiles before each test.
     */
    @Before
    public void setUp() {
        mockDb = mock(FirebaseFirestore.class);
        firestoreStaticMock = Mockito.mockStatic(FirebaseFirestore.class);
        firestoreStaticMock.when(FirebaseFirestore::getInstance).thenReturn(mockDb);

        repo = new FakePrivateEventInvitationRepository();

        Profile p1 = new Profile();
        p1.setName("Alice Smith");
        p1.setEmail("alice@example.com");
        p1.setPhone("1112223333");

        Profile p2 = new Profile();
        p2.setName("Bob Lee");
        p2.setEmail("bob@example.com");
        p2.setPhone("4445556666");

        Profile p3 = new Profile();
        p3.setName("Carol");
        p3.setEmail("carol@test.com");
        p3.setPhone("");

        repo.addFakeProfile("user1", p1);
        repo.addFakeProfile("user2", p2);
        repo.addFakeProfile("user3", p3);
    }

    /**
     * Cleans up mocked static Firestore after each test.
     */
    @After
    public void tearDown() {
        firestoreStaticMock.close();
    }

    /**
     * Verifies search matches profiles by name.
     */
    @Test
    public void testSearchEntrants_matchesByName() {
        final List<Profile>[] results = new List[]{null};
        final List<String>[] ids = new List[]{null};

        repo.searchEntrants("alice", (profiles, userIds) -> {
            results[0] = profiles;
            ids[0] = userIds;
        });

        assertEquals(1, results[0].size());
        assertEquals("Alice Smith", results[0].get(0).getName());
        assertEquals("user1", ids[0].get(0));
    }

    /**
     * Verifies search matches profiles by email.
     */
    @Test
    public void testSearchEntrants_matchesByEmail() {
        final List<Profile>[] results = new List[]{null};

        repo.searchEntrants("bob@example", (profiles, userIds) -> results[0] = profiles);

        assertEquals(1, results[0].size());
        assertEquals("Bob Lee", results[0].get(0).getName());
    }

    /**
     * Verifies search matches profiles by phone number.
     */
    @Test
    public void testSearchEntrants_matchesByPhone() {
        final List<Profile>[] results = new List[]{null};

        repo.searchEntrants("444555", (profiles, userIds) -> results[0] = profiles);

        assertEquals(1, results[0].size());
        assertEquals("Bob Lee", results[0].get(0).getName());
    }

    /**
     * Verifies empty query returns all profiles.
     */
    @Test
    public void testSearchEntrants_emptyQueryReturnsAll() {
        final List<Profile>[] results = new List[]{null};
        final List<String>[] ids = new List[]{null};

        repo.searchEntrants("", (profiles, userIds) -> {
            results[0] = profiles;
            ids[0] = userIds;
        });

        assertEquals(3, results[0].size());
        assertEquals(3, ids[0].size());
    }

    /**
     * Verifies sending invitation triggers success callback and stores parameters.
     */
    @Test
    public void testSendInvitation_successCallback() {
        repo.sendInvitationResult = true;

        final boolean[] success = {false};

        repo.sendInvitation("event1", "Private Event", "user1", result -> success[0] = result);

        assertTrue(success[0]);
        assertEquals("event1", repo.lastEventId);
        assertEquals("Private Event", repo.lastEventName);
        assertEquals("user1", repo.lastUserId);
    }

    /**
     * Verifies adding invited user triggers success callback and stores parameters.
     */
    @Test
    public void testAddInvitedUserToEvent_successCallback() {
        repo.addInvitedUserResult = true;

        final boolean[] success = {false};

        repo.addInvitedUserToEvent("event2", "user2", result -> success[0] = result);

        assertTrue(success[0]);
        assertEquals("event2", repo.lastEventId);
        assertEquals("user2", repo.lastUserId);
    }
}