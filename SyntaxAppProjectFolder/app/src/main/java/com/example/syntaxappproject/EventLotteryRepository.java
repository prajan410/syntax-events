package com.example.syntaxappproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository class responsible for organizer side lottery actions
 * in Firebase Firestore for the SyntaxEvents application.
 * Lottery repository helper
 * <p>
 * Handles sampling entrants from the wait list, sending winner notifications,
 * sending not chosen notifications, and drawing replacement entrants.
 * All operations are asynchronous and return results via callbacks.
 * </p>
 *
 * <p>Outstanding issues: this implementation keeps users on the wait list
 * after sending notifications, and sends one notification document per action.</p>
 */
public class EventLotteryRepository {

    private FirebaseFirestore db = null;

    /**
     * Constructs an EventLotteryRepository and initializes the Firestore instance.
     */
    public EventLotteryRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Protected constructor for unit testing.
     */
    protected EventLotteryRepository(boolean testMode) {
    }

    /**
     * Runs the lottery for an event by sampling a specified number of entrants
     * from the wait list.
     *
     * @param eventId      the id of the event
     * @param eventName    the name of the event
     * @param sampleSize   the number of entrants to sample
     * @param callback     callback reporting success/failure and a message
     */
    public void runLottery(String eventId, String eventName, int sampleSize, LotteryActionCallback callback) {
        getCandidateUserIds(eventId, candidateUserIds -> {
            if (candidateUserIds == null || candidateUserIds.isEmpty()) {
                callback.onComplete(false, "No entrants available in the wait list.");
                return;
            }

            Collections.shuffle(candidateUserIds);

            int actualSampleSize = Math.min(sampleSize, candidateUserIds.size());

            List<String> winners = new ArrayList<>(candidateUserIds.subList(0, actualSampleSize));
            List<String> losers = new ArrayList<>(candidateUserIds.subList(actualSampleSize, candidateUserIds.size()));

            processWinnerInvitations(eventId, eventName, winners, 0, success -> {
                if (!success) {
                    callback.onComplete(false, "Failed while sending winner notifications.");
                    return;
                }

                processNotChosenNotifications(eventId, eventName, losers, 0, loserSuccess -> {
                    if (!loserSuccess) {
                        callback.onComplete(false, "Failed while sending not-chosen notifications.");
                        return;
                    }

                    callback.onComplete(true, "Lottery completed for " + winners.size() + " entrant(s).");
                });
            });
        });
    }

    /**
     * Draws one replacement entrant from the wait list for the event.
     *
     * @param eventId      the id of the event
     * @param eventName    the name of the event
     * @param callback     callback reporting success/failure and a message
     */
    public void drawReplacement(String eventId, String eventName, LotteryActionCallback callback) {
        getCandidateUserIds(eventId, candidateUserIds -> {
            if (candidateUserIds == null || candidateUserIds.isEmpty()) {
                callback.onComplete(false, "No replacement entrant is available.");
                return;
            }

            Collections.shuffle(candidateUserIds);
            String replacementUserId = candidateUserIds.get(0);

            sendWinnerInvitation(eventId, eventName, replacementUserId, success -> {
                if (!success) {
                    callback.onComplete(false, "Failed to send replacement invitation.");
                    return;
                }

                addInvitedUserToEvent(eventId, replacementUserId, invitedSuccess -> {
                    if (!invitedSuccess) {
                        callback.onComplete(false, "Replacement invitation was sent but event update failed.");
                        return;
                    }

                    callback.onComplete(true, "Replacement entrant has been invited.");
                });
            });
        });
    }

    /**
     * Removes an invited user from the event document after they decline.
     *
     * @param eventId      the id of the event
     * @param userId       the user to remove from invited users
     * @param callback     callback reporting success/failure
     */
    public void removeInvitedUserFromEvent(String eventId, String userId, SimpleCallback callback) {
        db.collection("events")
                .document(eventId)
                .update("invitedUserIds", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(unused -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    /**
     * Reads wait list entrants and filters out users who are already invited.
     *
     * @param eventId      the id of the event
     * @param callback     callback returning the remaining candidate user ids
     */
    private void getCandidateUserIds(String eventId, CandidateCallback callback) {
        db.collection("events")
                .document(eventId)
                .get()
                .addOnSuccessListener(eventDoc -> {
                    List<String> invitedUserIds = (List<String>) eventDoc.get("invitedUserIds");
                    if (invitedUserIds == null) {
                        invitedUserIds = new ArrayList<>();
                    }

                    List<String> finalInvitedUserIds = invitedUserIds;

                    db.collection("events")
                            .document(eventId)
                            .collection("waitlist-entrants")
                            .get()
                            .addOnSuccessListener(waitlistSnapshots -> {
                                List<String> candidateUserIds = new ArrayList<>();

                                for (com.google.firebase.firestore.DocumentSnapshot doc : waitlistSnapshots.getDocuments()) {
                                    String userId = doc.getId();
                                    if (!finalInvitedUserIds.contains(userId)) {
                                        candidateUserIds.add(userId);
                                    }
                                }

                                callback.onResult(candidateUserIds);
                            })
                            .addOnFailureListener(e -> callback.onResult(null));
                })
                .addOnFailureListener(e -> callback.onResult(null));
    }

    /**
     * Sends winner invitations for all selected entrants.
     *
     * @param eventId      the id of the event
     * @param eventName    the name of the event
     * @param winners      selected winner user ids
     * @param index        current recursive index
     * @param callback     callback reporting success/failure
     */
    private void processWinnerInvitations(String eventId, String eventName, List<String> winners, int index, SimpleCallback callback) {
        if (index >= winners.size()) {
            callback.onComplete(true);
            return;
        }

        String userId = winners.get(index);

        sendWinnerInvitation(eventId, eventName, userId, success -> {
            if (!success) {
                callback.onComplete(false);
                return;
            }

            addInvitedUserToEvent(eventId, userId, invitedSuccess -> {
                if (!invitedSuccess) {
                    callback.onComplete(false);
                    return;
                }

                processWinnerInvitations(eventId, eventName, winners, index + 1, callback);
            });
        });
    }

    /**
     * Sends not-chosen notifications to all remaining entrants.
     *
     * @param eventId      the id of the event
     * @param eventName    the name of the event
     * @param losers       non-selected user ids
     * @param index        current recursive index
     * @param callback     callback reporting success/failure
     */
    private void processNotChosenNotifications(String eventId, String eventName, List<String> losers, int index, SimpleCallback callback) {
        if (index >= losers.size()) {
            callback.onComplete(true);
            return;
        }

        String userId = losers.get(index);

        sendNotChosenNotification(eventId, eventName, userId, success -> {
            if (!success) {
                callback.onComplete(false);
                return;
            }

            processNotChosenNotifications(eventId, eventName, losers, index + 1, callback);
        });
    }

    /**
     * Creates one winner invitation document.
     *
     * @param eventId      the id of the event
     * @param eventName    the name of the event
     * @param userId       the invited entrant
     * @param callback     callback reporting success/failure
     */
    private void sendWinnerInvitation(String eventId, String eventName, String userId, SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        data.put("eventName", eventName);
        data.put("userId", userId);
        data.put("status", "pending");
        data.put("invitedAt", Timestamp.now());
        data.put("responseAt", null);
        data.put("type", "lottery_win");

        db.collection("invitations")
                .add(data)
                .addOnSuccessListener(documentReference -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    /**
     * Creates one not-chosen notification document.
     *
     * @param eventId      the id of the event
     * @param eventName    the name of the event
     * @param userId       the entrant who was not chosen
     * @param callback     callback reporting success/failure
     */
    private void sendNotChosenNotification(String eventId, String eventName, String userId, SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        data.put("eventName", eventName);
        data.put("userId", userId);
        data.put("status", "not_chosen");
        data.put("invitedAt", Timestamp.now());
        data.put("responseAt", null);
        data.put("type", "lottery_loss");

        db.collection("invitations")
                .add(data)
                .addOnSuccessListener(documentReference -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    /**
     * Adds one invited user id to the event document.
     *
     * @param eventId      the id of the event
     * @param userId       the invited user id
     * @param callback     callback reporting success/failure
     */
    private void addInvitedUserToEvent(String eventId, String userId, SimpleCallback callback) {
        db.collection("events")
                .document(eventId)
                .update("invitedUserIds", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(unused -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    /**
     * Callback interface for reading candidate user ids.
     */
    private interface CandidateCallback {
        void onResult(List<String> candidateUserIds);
    }

    /**
     * Callback interface for simple success/failure operations.
     */
    public interface SimpleCallback {
        void onComplete(boolean success);
    }

    /**
     * Callback interface for lottery actions that return a status message.
     */
    public interface LotteryActionCallback {
        void onComplete(boolean success, String message);
    }
}