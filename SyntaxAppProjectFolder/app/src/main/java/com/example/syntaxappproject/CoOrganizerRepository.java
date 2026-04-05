package com.example.syntaxappproject;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Repository class responsible for check and add co-organizer
 * <p>
 * This repository acts as the data access layer between the application
 * and the Firestore backend. It provides methods to check and add co-organizer
 * using an event ID.
 * </p>
 */
public class CoOrganizerRepository {
    private FirebaseFirestore db;
    /**
     * Constructs an EventJoinRepository and initializes the Firestore instance.
     */
    public CoOrganizerRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Protected constructor for unit testing.
     **/
    protected CoOrganizerRepository(boolean testMode) {
    }

    /**
     * Check if the user is co-organizer of the event
     * @param eventId the Id of event to check
     * @param userId  the Id of user to check
     * @param callback the {@link checkCallback} invoked with the result,
     */
    public void isCoOrganizer(String eventId, String userId, CoOrganizerRepository.checkCallback callback) {
        if (eventId == null || userId == null) {
            callback.onResult(false);
            return;
        }
        db.collection("events")
                .document(eventId)
                .collection("co-organizers")
                .document(userId)
                .get()
                .addOnSuccessListener(doc ->
                        callback.onResult(doc.exists()));
    }

    /**
     * Add the user id to a event to mark it as co-organizer
     * @param eventId the Id of a event for add
     * @param uid   the Id of a user to add
     * @param callback the {@link addCallback} invoked with the result,
     */
    public void addCoOrganizer(String eventId, String uid, addCallback callback) {
        DocumentReference docRef = db.collection("events")
                .document(eventId)
                .collection("co-organizers")
                .document(uid);

        Map<String, Object> data = new HashMap<>();
        data.put("uid", uid);

        docRef.set(data)
                .addOnSuccessListener(unused -> callback.onComplete(true))
                .addOnFailureListener(e -> {
                    Log.e("CoOrganizerRepo", "Add failed: " + e.getMessage());
                    callback.onComplete(false);
                });
    }

    /**
     * Search the entrants contain the string in search bar in their name
     * @param query The string of search
     * @param callback the {@link searchCallback} invoked with the result,
     */
    public void searchEntrants(String query, CoOrganizerRepository.searchCallback callback) {
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
     * Send a invitation of co-organizer to database
     * @param eventId the id of the event
     * @param eventName the name of the event
     * @param userId the id of invited user
     * @param callback the {@link addCallback} invoked with the result,
     */
    public void sendInvitation(String eventId, String eventName, String userId, Notification notify, CoOrganizerRepository.addCallback callback) {
        Map<String, Object> data = new HashMap<>();
        data.put("eventId", eventId);
        data.put("eventName", eventName);
        data.put("userId", userId);
        data.put("status", "pending");
        data.put("invitedAt", Timestamp.now());
        data.put("responseAt", null);
        data.put("type", "co_organizer");

        db.collection("profiles").document(userId).get()
                .addOnSuccessListener(userDoc -> {
                            boolean wantsOrganizer = Boolean.TRUE.equals(userDoc.getBoolean("organizerNotificationEnabled"));

                            if (wantsOrganizer){
                                db.collection("users")
                                        .document(userId)
                                        .collection("notifications")
                                        .add(notify);
                            }
                });
        new EventJoinRepository().leaveEvent(eventId, userId, remove -> {});

        db.collection("invitations")
                .add(data)
                .addOnSuccessListener(documentReference -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }
    /**
     * Callback interface for search entrants results.
     */
    public interface searchCallback {
        /**
         * Called with the result of search entrant
         *
         * @param results return a list of profile
         * @param userIds return a list of uid
         */
        void onResult(List<Profile> results, List<String> userIds);
    }

    /**
     * Callback interface for co-organizer check results.
     */
    public interface checkCallback {
        /**
         * Called with the result of a co-organizer check.
         *
         * @param coOrganizer {@code true} if the user is on the co-organizer
         */
        void onResult(boolean coOrganizer);
    }
    /**
     * Callback interface for add operation results.
     */
    public interface addCallback {
        /**
         * Called when the add operation completes.
         *
         * @param success {@code true} if the operation succeeded
         */
        void onComplete(boolean success);
    }
}
