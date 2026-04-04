package com.example.syntaxappproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository class responsible for reading and updating entrant invitations
 * stored in Firebase Firestore.
 * <p>
 * Handles loading relevant invitation notifications for an entrant,
 * accepting invitations, and declining invitations.
 * </p>
 *
 * <p>Outstanding issues: currently this repository still returns one
 * relevant notification at a time for the UI.</p>
 */
public class InvitationRepository {

    private FirebaseFirestore db = null;

    /**
     * Constructs an InvitationRepository and initializes Firestore.
     */
    public InvitationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Protected constructor for unit testing.
     */
    protected InvitationRepository(boolean testMode) {
    }

    /**
     * Reads one pending invitation for the given user.
     *
     * @param userId the signed in user id
     * @param callback callback that returns an Invitation or null
     */
    public void getPendingInvitationForUser(String userId, InvitationCallback callback) {
        db.collection("invitations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "pending")
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    if (querySnapshots.isEmpty()) {
                        callback.onResult(null);
                        return;
                    }

                    Invitation invitation = querySnapshots.getDocuments().get(0).toObject(Invitation.class);
                    if (invitation != null) {
                        invitation.setInvitationId(querySnapshots.getDocuments().get(0).getId());
                    }

                    callback.onResult(invitation);
                })
                .addOnFailureListener(e -> callback.onResult(null));
    }

    /**
     * Reads the latest relevant invitation notification for the given user.
     * Relevant statuses are pending and not chosen.
     *
     * @param userId the signed-in user id
     * @param callback callback that returns an Invitation or null
     */
    public void getLatestRelevantInvitationForUser(String userId, InvitationCallback callback) {
        db.collection("invitations")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    Invitation latestInvitation = null;

                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshots.getDocuments()) {
                        Invitation invitation = doc.toObject(Invitation.class);
                        if (invitation == null) {
                            continue;
                        }

                        String status = invitation.getStatus();
                        if (!"pending".equals(status) && !"not_chosen".equals(status)) {
                            continue;
                        }

                        invitation.setInvitationId(doc.getId());

                        if (latestInvitation == null) {
                            latestInvitation = invitation;
                        } else if (isNewer(invitation, latestInvitation)) {
                            latestInvitation = invitation;
                        }
                    }

                    callback.onResult(latestInvitation);
                })
                .addOnFailureListener(e -> callback.onResult(null));
    }

    /**
     * Marks an invitation as accepted.
     *
     * @param invitationId the Firestore document id of the invitation
     * @param callback callback reporting success/failure
     */
    public void acceptInvitation(String invitationId, ActionCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "accepted");
        data.put("responseAt", Timestamp.now());

        db.collection("invitations")
                .document(invitationId)
                .update(data)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    /**
     * Marks an invitation as declined.
     *
     * @param invitationId the Firestore document id of the invitation
     * @param callback callback reporting success/failure
     */
    public void declineInvitation(String invitationId, ActionCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "declined");
        data.put("responseAt", Timestamp.now());

        db.collection("invitations")
                .document(invitationId)
                .update(data)
                .addOnCompleteListener(task -> callback.onComplete(task.isSuccessful()));
    }

    /**
     * Marks an invitation as declined and removes the entrant
     * from the event's invited user list.
     *
     * @param invitationId the Firestore document id of the invitation
     * @param eventId the event id of the invitation
     * @param userId the user who declined
     * @param callback callback reporting success/failure
     */
    public void declineInvitation(String invitationId, String eventId, String userId, ActionCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "declined");
        data.put("responseAt", Timestamp.now());

        db.collection("invitations")
                .document(invitationId)
                .update(data)
                .addOnSuccessListener(unused ->
                        db.collection("events")
                                .document(eventId)
                                .update("invitedUserIds", FieldValue.arrayRemove(userId))
                                .addOnSuccessListener(unused2 -> callback.onComplete(true))
                                .addOnFailureListener(e -> callback.onComplete(false))
                )
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    /**
     * Returns true if the first invitation is newer than the second one.
     *
     * @param current the current invitation being checked
     * @param latest the current latest invitation
     * @return true if current is newer
     */
    private boolean isNewer(Invitation current, Invitation latest) {
        if (current.getInvitedAt() == null) {
            return false;
        }
        if (latest.getInvitedAt() == null) {
            return true;
        }
        return current.getInvitedAt().compareTo(latest.getInvitedAt()) > 0;
    }

    /**
     * Callback for reading one invitation.
     */
    public interface InvitationCallback {
        void onResult(Invitation invitation);
    }

    /**
     * Callback for accept/decline actions.
     */
    public interface ActionCallback {
        void onComplete(boolean success);
    }
}