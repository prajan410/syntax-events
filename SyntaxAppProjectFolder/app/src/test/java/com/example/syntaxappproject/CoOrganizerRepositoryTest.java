package com.example.syntaxappproject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
/**
 * Unit tests for the {@link CoOrganizerRepository}.
 */
public class CoOrganizerRepositoryTest {

    /**
     * In-memory fake subclass of {@link CoOrganizerRepository} that simulates
     * the Firestore waitlist-entrants subcollection using a {@link HashSet}.
     * Stores entries as {@code "eventId::userId"} composite keys.
     */
    static class FakeCoOrganizerRepository extends CoOrganizerRepository {

        private final Set<String> waitlist = new HashSet<>();

        /**
         * Constructs the fake repository using the test-mode constructor
         * to bypass Firebase initialization.
         */
        public FakeCoOrganizerRepository(){super(true);}

        /**
         * Returns a composite key combining eventId and userId.
         *
         * @param eventId the event ID
         * @param userId  the user ID
         * @return a unique string key for the waitlist entry
         */
        private String key(String eventId, String userId) {
            return eventId + "::" + userId;
        }

        @Override
        public void isCoOrganizer(String eventId, String userId, checkCallback callback) {
            callback.onResult(waitlist.contains(key(eventId, userId)));
        }

        @Override
        public void addCoOrganizer(String eventId, String userId, addCallback callback) {
            waitlist.add(key(eventId, userId));
            callback.onComplete(true);
        }

    }

    private FakeCoOrganizerRepository repo;
    /**
     * Initializes a fake repository.
     */
    @Before
    public void setUp() {
        repo = new FakeCoOrganizerRepository();
    }

    /**
     * Test when the user is not in co-organizer
     */
    @Test
    public void testisCoOrganizerReturnFalse(){
        boolean[] result = {true};
        repo.isCoOrganizer("event","user",coOrganizer -> result[0] = coOrganizer);
        assertFalse(result[0]);
    }
    /**
     * Test when the user is in co-organizer
     */
    @Test
    public void testisCoOrganizerReturnTrue(){
        repo.addCoOrganizer("event","user", success -> {});
        boolean[] result = {false};
        repo.isCoOrganizer("event","user",coOrganizer -> result[0] = coOrganizer);
        assertTrue(result[0]);
    }
    /**
     * Test add a user to co-organizer
     */
    @Test
    public void testAddCoOrganizerReturnTrue(){
        boolean[] result = {false};
        repo.addCoOrganizer("event","user", success -> result[0] = success);
        assertTrue(result[0]);
    }
}
