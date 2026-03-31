package com.example.syntaxappproject;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link InvitationRepository}.
 * <p>
 * Uses a fake in-memory subclass so tests run without a real Firebase connection.
 * </p>
 */
public class InvitationRepositoryTest {

    /**
     * Fake subclass of {@link InvitationRepository} used to simulate
     * Firebase callback behavior during testing.
     */
    static class FakeInvitationRepository extends InvitationRepository {

        private Invitation invitationToReturn;
        private boolean acceptResult;
        private boolean declineResult;

        /**
         * Constructs the fake repository using the test-mode constructor
         * to bypass Firebase initialization.
         */
        public FakeInvitationRepository() {
            super(true);
        }

        @Override
        public void getPendingInvitationForUser(String userId, InvitationCallback callback) {
            callback.onResult(invitationToReturn);
        }

        @Override
        public void acceptInvitation(String invitationId, ActionCallback callback) {
            callback.onComplete(acceptResult);
        }

        @Override
        public void declineInvitation(String invitationId, ActionCallback callback) {
            callback.onComplete(declineResult);
        }
    }

    private FakeInvitationRepository repo;
    private Invitation invitation;

    /**
     * Initializes a fake repository and sample invitation before each test.
     */
    @Before
    public void setUp() {
        repo = new FakeInvitationRepository();

        invitation = new Invitation();
        invitation.setInvitationId("inv1");
        invitation.setEventId("event1");
        invitation.setEventName("Swim Lessons");
        invitation.setUserId("user1");
        invitation.setStatus("pending");
    }

    /**
     * Verifies that {@link InvitationRepository#getPendingInvitationForUser(String, InvitationRepository.InvitationCallback)}
     * returns an invitation when one exists.
     */
    @Test
    public void testGetPendingInvitationForUserReturnsInvitation() {
        repo.invitationToReturn = invitation;

        final Invitation[] result = new Invitation[1];

        repo.getPendingInvitationForUser("user1", returnedInvitation -> result[0] = returnedInvitation);

        assertEquals("inv1", result[0].getInvitationId());
        assertEquals("event1", result[0].getEventId());
        assertEquals("Swim Lessons", result[0].getEventName());
        assertEquals("user1", result[0].getUserId());
        assertEquals("pending", result[0].getStatus());
    }

    /**
     * Verifies that {@link InvitationRepository#getPendingInvitationForUser(String, InvitationRepository.InvitationCallback)}
     * returns {@code null} when no invitation exists.
     */
    @Test
    public void testGetPendingInvitationForUserReturnsNull() {
        repo.invitationToReturn = null;

        final Invitation[] result = new Invitation[1];

        repo.getPendingInvitationForUser("user1", returnedInvitation -> result[0] = returnedInvitation);

        assertNull(result[0]);
    }

    /**
     * Verifies that {@link InvitationRepository#acceptInvitation(String, InvitationRepository.ActionCallback)}
     * returns {@code true} when the accept operation succeeds.
     */
    @Test
    public void testAcceptInvitationSuccess() {
        repo.acceptResult = true;

        final boolean[] result = new boolean[1];

        repo.acceptInvitation("inv1", success -> result[0] = success);

        assertTrue(result[0]);
    }

    /**
     * Verifies that {@link InvitationRepository#acceptInvitation(String, InvitationRepository.ActionCallback)}
     * returns {@code false} when the accept operation fails.
     */
    @Test
    public void testAcceptInvitationFailure() {
        repo.acceptResult = false;

        final boolean[] result = new boolean[1];

        repo.acceptInvitation("inv1", success -> result[0] = success);

        assertFalse(result[0]);
    }

    /**
     * Verifies that {@link InvitationRepository#declineInvitation(String, InvitationRepository.ActionCallback)}
     * returns {@code true} when the decline operation succeeds.
     */
    @Test
    public void testDeclineInvitationSuccess() {
        repo.declineResult = true;

        final boolean[] result = new boolean[1];

        repo.declineInvitation("inv1", success -> result[0] = success);

        assertTrue(result[0]);
    }

    /**
     * Verifies that {@link InvitationRepository#declineInvitation(String, InvitationRepository.ActionCallback)}
     * returns {@code false} when the decline operation fails.
     */
    @Test
    public void testDeclineInvitationFailure() {
        repo.declineResult = false;

        final boolean[] result = new boolean[1];

        repo.declineInvitation("inv1", success -> result[0] = success);

        assertFalse(result[0]);
    }
}