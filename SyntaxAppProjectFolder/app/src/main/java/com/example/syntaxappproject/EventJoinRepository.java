package com.example.syntaxappproject;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.util.HashMap;
import java.util.Map;

/**
 * Repository class responsible for managing event waitlist membership
 * in Firebase Firestore for the SyntaxEvents application.
 * <p>
 * Handles joining, leaving, and checking membership status for an event's
 * waitlist. All operations are asynchronous and return results via callbacks.
 * </p>
 *
 * <p>Outstanding issues: no handling for capacity limits when joining,
 * and no error distinction between network failures and missing documents.</p>
 */
public class EventJoinRepository {
    private FirebaseFirestore db = null;

    /**
     * Constructs an EventJoinRepository and initializes the Firestore instance.
     */
    public EventJoinRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Protected no-arg constructor for unit testing — skips Firebase initialization.
     */
    protected EventJoinRepository(boolean testMode) {
    }

    /**
     * Checks whether a user has already joined an event's waitlist.
     *
     * @param eventId  the ID of the event to check
     * @param userId   the ID of the user to check
     * @param callback the {@link JoinCheckCallback} invoked with {@code true}
     *                 if the user is on the waitlist, {@code false} otherwise
     */
    public void hasJoined(String eventId, String userId, JoinCheckCallback callback) {
        if (eventId == null || userId == null) {
            callback.onResult(false);
            return;
        }
        db.collection("events")
                .document(eventId)
                .collection("waitlist-entrants")
                .document(userId)
                .get()
                .addOnSuccessListener(doc ->
                        callback.onResult(doc.exists()));
    }


    /**
     * Adds a user to the waitlist of the specified event.
     * <p>
     * Records the join timestamp under the {@code joinedAt} field.
     * </p>
     *
     * @param eventId  the ID of the event to join
     * @param userId   the ID of the user joining
     * @param latitude the latitude of the user joining (can be null)
     * @param longitude the longitude of the user joining (can be null)
     * @param callback the {@link JoinCallback} invoked with the result
     */
    public void joinEvent(String eventId, String userId, Double latitude, Double longitude, JoinCallback callback) {
        db.runTransaction(transaction -> {
                    DocumentReference docRef = db.collection("events")
                            .document(eventId)
                            .collection("waitlist-entrants")
                            .document(userId);

                    DocumentSnapshot snapshot = transaction.get(docRef);

                    if (snapshot.exists()) {
                        throw new RuntimeException("Already joined");
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put("joinedAt", Timestamp.now());
                    if (latitude != null && longitude != null) {
                        data.put("latitude", latitude);
                        data.put("longitude", longitude);
                    }
                    transaction.set(docRef, data);

                    DocumentReference eventRef = db.collection("events").document(eventId);
                    transaction.update(eventRef, "waitlistCount", FieldValue.increment(1));

                    return null;
                })
                .addOnSuccessListener(aVoid -> callback.onComplete(true))
                .addOnFailureListener(e -> {
                    Log.e("EventJoinRepo", "Join transaction failed: " + e.getMessage());
                    callback.onComplete(false);
                });
    }

    /**
     * Alias for {@link #joinEvent(String, String, Double, Double, JoinCallback)}
     * with {@code latitude} and {@code longitude} set to {@code null}.
     */
    @Deprecated
    public void joinEvent(String eventId, String userId, JoinCallback callback) {
        joinEvent(eventId, userId, null, null, callback);
    }



    /**
     * Removes a user from the waitlist of the specified event.
     *
     * @param eventId  the ID of the event to leave
     * @param userId   the ID of the user leaving
     * @param callback the {@link JoinCallback} invoked with the result
     */
    public void leaveEvent(String eventId, String userId, JoinCallback callback) {
        db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                        DocumentReference docRef = db.collection("events")
                                .document(eventId)
                                .collection("waitlist-entrants")
                                .document(userId);
                        DocumentSnapshot snapshot = transaction.get(docRef);

                        if (!snapshot.exists()) {
                            throw new RuntimeException("Not joined");
                        }
                        transaction.delete(docRef);
                        DocumentReference eventRef = db.collection("events").document(eventId);
                        transaction.update(eventRef, "waitlistCount", FieldValue.increment(-1));

                        return null;
                    }
                })
                .addOnSuccessListener(aVoid -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }


    /**
     * Callback interface for waitlist membership check results.
     */
    public interface JoinCheckCallback {
        /**
         * Called with the result of a waitlist membership check.
         *
         * @param joined {@code true} if the user is on the waitlist
         */
        void onResult(boolean joined);
    }

    /**
     * Callback interface for join and leave operation results.
     */
    public interface JoinCallback {
        /**
         * Called when the join or leave operation completes.
         *
         * @param success {@code true} if the operation succeeded
         */
        void onComplete(boolean success);
    }
}
