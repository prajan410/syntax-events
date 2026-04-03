package com.example.syntaxappproject;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
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
