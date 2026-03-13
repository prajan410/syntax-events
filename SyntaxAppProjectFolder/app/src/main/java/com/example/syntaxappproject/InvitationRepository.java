package com.example.syntaxappproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository class responsible for reading and updating entrant invitations
 * Stored in Firebase Firestore.
 * Outstanding issues:
 * Currently fetches only one pending invitation for demo simplicity
 * Organizer side invitation creation is handled separately
 **/
public class InvitationRepository {

    private FirebaseFirestore db = null;

    /**
     * Constructs an InvitationRepository and initializes Firestore.
     **/
    public InvitationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Protected constructor for unit testing.
     **/
    protected InvitationRepository(boolean testMode) {
        // intentionally empty for test subclassing
    }

    /**
     * Reads one pending invitation for the given user.
     *
     * @param userId the signed-in user id
     * @param callback callback that returns an Invitation or null
     **/
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
     * Marks an invitation as accepted.
     *
     * @param invitationId the Firestore document id of the invitation
     * @param callback callback reporting success/failure
     **/
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
     **/
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
     * Callback for reading one invitation.
     **/
    public interface InvitationCallback {
        void onResult(Invitation invitation);
    }

    /**
     * Callback for accept/decline actions.
     **/
    public interface ActionCallback {
        void onComplete(boolean success);
    }
}