package com.example.syntaxappproject;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link EventJoinRepository}.
 * <p>
 * Uses an in-memory fake subclass to avoid any Firebase dependency.
 * </p>
 */
public class EventJoinRepositoryTest {

    /**
     * In-memory fake subclass of {@link EventJoinRepository} that simulates
     * the Firestore waitlist-entrants subcollection using a {@link HashSet}.
     * Stores entries as {@code "eventId::userId"} composite keys.
     */
    static class FakeEventJoinRepository extends EventJoinRepository {

        private final Set<String> waitlist = new HashSet<>();

        /**
         * Constructs the fake repository using the test-mode constructor
         * to bypass Firebase initialization.
         */
        public FakeEventJoinRepository() {
            super(true);
        }

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
        public void hasJoined(String eventId, String userId, JoinCheckCallback callback) {
            callback.onResult(waitlist.contains(key(eventId, userId)));
        }

        /**
         * Overrides the 5-parameter joinEvent method (latitude and longitude can be null).
         */
        @Override
        public void joinEvent(String eventId, String userId, Double latitude, Double longitude, JoinCallback callback) {
            waitlist.add(key(eventId, userId));
            callback.onComplete(true);
        }

        /**
         * Convenience method for tests that don't care about location.
         * Calls the main joinEvent method with null latitude/longitude.
         */
        public void joinEvent(String eventId, String userId, JoinCallback callback) {
            joinEvent(eventId, userId, null, null, callback);
        }

        @Override
        public void leaveEvent(String eventId, String userId, JoinCallback callback) {
            waitlist.remove(key(eventId, userId));
            callback.onComplete(true);
        }
    }

    private FakeEventJoinRepository repo;

    /**
     * Initializes a fresh fake repository before each test.
     */
    @Before
    public void setUp() {
        repo = new FakeEventJoinRepository();
    }

    /**
     * Verifies that {@link EventJoinRepository#hasJoined} returns {@code false}
     * when the user has not joined the event.
     */
    @Test
    public void testHasJoined_returnsFalse_whenUserNotOnWaitlist() {
        boolean[] result = {true};
        repo.hasJoined("event-001", "user-001", joined -> result[0] = joined);
        assertFalse(result[0]);
    }

    /**
     * Verifies that {@link EventJoinRepository#hasJoined} returns {@code true}
     * after the user has joined the event.
     */
    @Test
    public void testHasJoined_returnsTrue_afterUserJoins() {
        repo.joinEvent("event-001", "user-001", success -> {});
        boolean[] result = {false};
        repo.hasJoined("event-001", "user-001", joined -> result[0] = joined);
        assertTrue(result[0]);
    }

    /**
     * Verifies that {@link EventJoinRepository#joinEvent} invokes the callback
     * with {@code true} on success.
     */
    @Test
    public void testJoinEvent_callsCallbackTrue() {
        boolean[] result = {false};
        repo.joinEvent("event-001", "user-001", success -> result[0] = success);
        assertTrue(result[0]);
    }

    /**
     * Verifies that joining an event for one user does not affect
     * the waitlist status of a different user on the same event.
     */
    @Test
    public void testJoinEvent_differentUsersAreIndependent() {
        repo.joinEvent("event-001", "user-001", success -> {});
        boolean[] user2Result = {true};
        repo.hasJoined("event-001", "user-002", joined -> user2Result[0] = joined);
        assertFalse(user2Result[0]);
    }

    /**
     * Verifies that joining one event does not affect the same user's
     * waitlist status on a different event.
     */
    @Test
    public void testJoinEvent_sameUserDifferentEventsAreIndependent() {
        repo.joinEvent("event-001", "user-001", success -> {});
        boolean[] result = {true};
        repo.hasJoined("event-002", "user-001", joined -> result[0] = joined);
        assertFalse(result[0]);
    }

    /**
     * Verifies that {@link EventJoinRepository#leaveEvent} invokes the callback
     * with {@code true} on success.
     */
    @Test
    public void testLeaveEvent_callsCallbackTrue() {
        repo.joinEvent("event-001", "user-001", success -> {});
        boolean[] result = {false};
        repo.leaveEvent("event-001", "user-001", success -> result[0] = success);
        assertTrue(result[0]);
    }

    /**
     * Verifies that a user is no longer on the waitlist after leaving the event.
     */
    @Test
    public void testLeaveEvent_userIsNoLongerOnWaitlist() {
        repo.joinEvent("event-001", "user-001", success -> {});
        repo.leaveEvent("event-001", "user-001", success -> {});
        boolean[] result = {true};
        repo.hasJoined("event-001", "user-001", joined -> result[0] = joined);
        assertFalse(result[0]);
    }

    /**
     * Verifies that leaving an event does not remove other users
     * from the same event's waitlist.
     */
    @Test
    public void testLeaveEvent_doesNotAffectOtherUsers() {
        repo.joinEvent("event-001", "user-001", success -> {});
        repo.joinEvent("event-001", "user-002", success -> {});
        repo.leaveEvent("event-001", "user-001", success -> {});
        boolean[] result = {false};
        repo.hasJoined("event-001", "user-002", joined -> result[0] = joined);
        assertTrue(result[0]);
    }
}