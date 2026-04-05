package com.example.syntaxappproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrivateEventInvitationRepository {

    private FirebaseFirestore db;

    /**
     * Creates a new repository instance and initializes the Firestore database reference.
     */
    public PrivateEventInvitationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Callback interface used to return matching entrant profiles
     * and their corresponding user IDs after a search.
     */
    public interface SearchCallback {

        /**
         * Called when the search operation completes.
         *
         * @param results the list of matching profiles
         * @param userIds the list of matching user IDs
         */
        void onResult(List<Profile> results, List<String> userIds);
    }

    /**
     * Callback interface used to return the success status
     * of a repository operation.
     */
    public interface SimpleCallback {

        /**
         * Called when the operation completes.
         *
         * @param success true if the operation succeeded, false otherwise
         */
        void onComplete(boolean success);
    }

    /**
     * Searches all entrant profiles in Firestore and returns profiles
     * whose name, email, or phone number contains the given query text.
     *
     * @param query the search text entered by the user
     * @param callback the callback that receives the matching profiles and user IDs
     */
    public void searchEntrants(String query, SearchCallback callback) {
        db.collection("profiles")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Profile> results = new ArrayList<>();
                    List<String> userIds = new ArrayList<>();

                    String q = query == null ? "" : query.trim().toLowerCase();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Profile profile = doc.toObject(Profile.class);
                        if (profile == null) continue;

                        String name = profile.getName() == null ? "" : profile.getName().toLowerCase();
                        String email = profile.getEmail() == null ? "" : profile.getEmail().toLowerCase();
                        String phone = profile.getPhone() == null ? "" : profile.getPhone().toLowerCase();

                        boolean match = name.contains(q) || email.contains(q) || phone.contains(q);

                        if (match) {
                            results.add(profile);
                            userIds.add(doc.getId());
                        }
                    }

                    callback.onResult(results, userIds);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>(), new ArrayList<>()));
    }

    /**
     * Sends a private waitlist invitation by creating a new invitation
     * document in the Firestore invitations collection.
     *
     * @param eventId the ID of the event
     * @param eventName the name of the event
     * @param userId the ID of the invited user
     * @param callback the callback that receives the success status
     */
    public void sendInvitation(String eventId, String eventName, String userId, SimpleCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        data.put("eventName", eventName);
        data.put("userId", userId);
        data.put("status", "pending");
        data.put("invitedAt", Timestamp.now());
        data.put("responseAt", null);
        data.put("type", "private_waitlist");

        db.collection("invitations")
                .add(data)
                .addOnSuccessListener(documentReference -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    /**
     * Adds the specified user ID to the invited users list
     * of the given event document in Firestore.
     *
     * @param eventId the ID of the event to update
     * @param userId the ID of the user to add to the invited list
     * @param callback the callback that receives the success status
     */
    public void addInvitedUserToEvent(String eventId, String userId, SimpleCallback callback) {
        db.collection("events")
                .document(eventId)
                .update("invitedUserIds", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(unused -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }
}