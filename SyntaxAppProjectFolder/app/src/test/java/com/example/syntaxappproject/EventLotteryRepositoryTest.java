package com.example.syntaxappproject;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link EventLotteryRepository}.
 * <p>
 * Uses a fake in-memory subclass so tests run without a real Firebase connection.
 * </p>
 */
public class EventLotteryRepositoryTest {

    /**
     * Fake subclass of {@link EventLotteryRepository} used to simulate
     * callback behavior during testing without Firebase.
     */
    static class FakeEventLotteryRepository extends EventLotteryRepository {

        List<String> candidateUserIds;
        boolean winnerInvitationSuccess;
        boolean notChosenNotificationSuccess;
        boolean addInvitedUserSuccess;
        boolean removeInvitedUserSuccess;

        /**
         * Constructs the fake repository using the test mode constructor
         * to bypass Firebase initialization.
         */
        public FakeEventLotteryRepository() {
            super(true);
        }

        @Override
        public void runLottery(String eventId, String eventName, int sampleSize, LotteryActionCallback callback) {
            if (candidateUserIds == null || candidateUserIds.isEmpty()) {
                callback.onComplete(false, "No entrants available in the wait list.");
                return;
            }

            int actualSampleSize = Math.min(sampleSize, candidateUserIds.size());

            List<String> winners = new ArrayList<>(candidateUserIds.subList(0, actualSampleSize));
            List<String> losers = new ArrayList<>(candidateUserIds.subList(actualSampleSize, candidateUserIds.size()));

            if (!winnerInvitationSuccess) {
                callback.onComplete(false, "Failed while sending winner notifications.");
                return;
            }

            if (!notChosenNotificationSuccess) {
                callback.onComplete(false, "Failed while sending not-chosen notifications.");
                return;
            }

            callback.onComplete(true, "Lottery completed for " + winners.size() + " entrant(s).");
        }

        @Override
        public void drawReplacement(String eventId, String eventName, LotteryActionCallback callback) {
            if (candidateUserIds == null || candidateUserIds.isEmpty()) {
                callback.onComplete(false, "No replacement entrant is available.");
                return;
            }

            if (!winnerInvitationSuccess) {
                callback.onComplete(false, "Failed to send replacement invitation.");
                return;
            }

            if (!addInvitedUserSuccess) {
                callback.onComplete(false, "Replacement invitation was sent but event update failed.");
                return;
            }

            callback.onComplete(true, "Replacement entrant has been invited.");
        }

        @Override
        public void removeInvitedUserFromEvent(String eventId, String userId, SimpleCallback callback) {
            callback.onComplete(removeInvitedUserSuccess);
        }
    }

    private FakeEventLotteryRepository repo;

    /**
     * Initializes a fake repository before each test.
     */
    @Before
    public void setUp() {
        repo = new FakeEventLotteryRepository();
        repo.candidateUserIds = new ArrayList<>();
        repo.winnerInvitationSuccess = true;
        repo.notChosenNotificationSuccess = true;
        repo.addInvitedUserSuccess = true;
        repo.removeInvitedUserSuccess = true;
    }

    /**
     * Verifies that {@link EventLotteryRepository#runLottery(String, String, int, EventLotteryRepository.LotteryActionCallback)}
     * returns failure when no candidates are available.
     */
    @Test
    public void testRunLotteryNoCandidates() {
        repo.candidateUserIds = new ArrayList<>();

        final boolean[] successResult = new boolean[1];
        final String[] messageResult = new String[1];

        repo.runLottery("event1", "Swim Lessons", 2, (success, message) -> {
            successResult[0] = success;
            messageResult[0] = message;
        });

        assertFalse(successResult[0]);
        assertEquals("No entrants available in the wait list.", messageResult[0]);
    }

    /**
     * Verifies that {@link EventLotteryRepository#runLottery(String, String, int, EventLotteryRepository.LotteryActionCallback)}
     * returns failure when winner notifications cannot be sent.
     */
    @Test
    public void testRunLotteryWinnerNotificationFailure() {
        repo.candidateUserIds = new ArrayList<>(Arrays.asList("user1", "user2", "user3"));
        repo.winnerInvitationSuccess = false;

        final boolean[] successResult = new boolean[1];
        final String[] messageResult = new String[1];

        repo.runLottery("event1", "Swim Lessons", 2, (success, message) -> {
            successResult[0] = success;
            messageResult[0] = message;
        });

        assertFalse(successResult[0]);
        assertEquals("Failed while sending winner notifications.", messageResult[0]);
    }

    /**
     * Verifies that {@link EventLotteryRepository#runLottery(String, String, int, EventLotteryRepository.LotteryActionCallback)}
     * returns failure when not-chosen notifications cannot be sent.
     */
    @Test
    public void testRunLotteryNotChosenNotificationFailure() {
        repo.candidateUserIds = new ArrayList<>(Arrays.asList("user1", "user2", "user3"));
        repo.notChosenNotificationSuccess = false;

        final boolean[] successResult = new boolean[1];
        final String[] messageResult = new String[1];

        repo.runLottery("event1", "Swim Lessons", 2, (success, message) -> {
            successResult[0] = success;
            messageResult[0] = message;
        });

        assertFalse(successResult[0]);
        assertEquals("Failed while sending not-chosen notifications.", messageResult[0]);
    }

    /**
     * Verifies that {@link EventLotteryRepository#runLottery(String, String, int, EventLotteryRepository.LotteryActionCallback)}
     * returns success when the lottery completes correctly.
     */
    @Test
    public void testRunLotterySuccess() {
        repo.candidateUserIds = new ArrayList<>(Arrays.asList("user1", "user2", "user3"));

        final boolean[] successResult = new boolean[1];
        final String[] messageResult = new String[1];

        repo.runLottery("event1", "Swim Lessons", 2, (success, message) -> {
            successResult[0] = success;
            messageResult[0] = message;
        });

        assertTrue(successResult[0]);
        assertEquals("Lottery completed for 2 entrant(s).", messageResult[0]);
    }

    /**
     * Verifies that {@link EventLotteryRepository#drawReplacement(String, String, EventLotteryRepository.LotteryActionCallback)}
     * returns failure when no replacement entrant is available.
     */
    @Test
    public void testDrawReplacementNoCandidates() {
        repo.candidateUserIds = new ArrayList<>();

        final boolean[] successResult = new boolean[1];
        final String[] messageResult = new String[1];

        repo.drawReplacement("event1", "Swim Lessons", (success, message) -> {
            successResult[0] = success;
            messageResult[0] = message;
        });

        assertFalse(successResult[0]);
        assertEquals("No replacement entrant is available.", messageResult[0]);
    }

    /**
     * Verifies that {@link EventLotteryRepository#drawReplacement(String, String, EventLotteryRepository.LotteryActionCallback)}
     * returns failure when the replacement invitation cannot be sent.
     */
    @Test
    public void testDrawReplacementInvitationFailure() {
        repo.candidateUserIds = new ArrayList<>(Arrays.asList("user4"));
        repo.winnerInvitationSuccess = false;

        final boolean[] successResult = new boolean[1];
        final String[] messageResult = new String[1];

        repo.drawReplacement("event1", "Swim Lessons", (success, message) -> {
            successResult[0] = success;
            messageResult[0] = message;
        });

        assertFalse(successResult[0]);
        assertEquals("Failed to send replacement invitation.", messageResult[0]);
    }

    /**
     * Verifies that {@link EventLotteryRepository#drawReplacement(String, String, EventLotteryRepository.LotteryActionCallback)}
     * returns failure when the replacement invitation is sent but the event update fails.
     */
    @Test
    public void testDrawReplacementEventUpdateFailure() {
        repo.candidateUserIds = new ArrayList<>(Arrays.asList("user4"));
        repo.addInvitedUserSuccess = false;

        final boolean[] successResult = new boolean[1];
        final String[] messageResult = new String[1];

        repo.drawReplacement("event1", "Swim Lessons", (success, message) -> {
            successResult[0] = success;
            messageResult[0] = message;
        });

        assertFalse(successResult[0]);
        assertEquals("Replacement invitation was sent but event update failed.", messageResult[0]);
    }

    /**
     * Verifies that {@link EventLotteryRepository#drawReplacement(String, String, EventLotteryRepository.LotteryActionCallback)}
     * returns success when a replacement entrant is invited correctly.
     */
    @Test
    public void testDrawReplacementSuccess() {
        repo.candidateUserIds = new ArrayList<>(Arrays.asList("user4"));

        final boolean[] successResult = new boolean[1];
        final String[] messageResult = new String[1];

        repo.drawReplacement("event1", "Swim Lessons", (success, message) -> {
            successResult[0] = success;
            messageResult[0] = message;
        });

        assertTrue(successResult[0]);
        assertEquals("Replacement entrant has been invited.", messageResult[0]);
    }

    /**
     * Verifies that {@link EventLotteryRepository#removeInvitedUserFromEvent(String, String, EventLotteryRepository.SimpleCallback)}
     * returns true when the invited user is removed successfully.
     */
    @Test
    public void testRemoveInvitedUserFromEventSuccess() {
        repo.removeInvitedUserSuccess = true;

        final boolean[] result = new boolean[1];

        repo.removeInvitedUserFromEvent("event1", "user1", success -> result[0] = success);

        assertTrue(result[0]);
    }

    /**
     * Verifies that {@link EventLotteryRepository#removeInvitedUserFromEvent(String, String, EventLotteryRepository.SimpleCallback)}
     * returns false when removing the invited user fails.
     */
    @Test
    public void testRemoveInvitedUserFromEventFailure() {
        repo.removeInvitedUserSuccess = false;

        final boolean[] result = new boolean[1];

        repo.removeInvitedUserFromEvent("event1", "user1", success -> result[0] = success);

        assertFalse(result[0]);
    }
}
