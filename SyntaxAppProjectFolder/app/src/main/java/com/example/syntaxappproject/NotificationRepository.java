package com.example.syntaxappproject;



import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

/**
 * Repository class responsible for managing {@link Notification} data
 * in Firebase Firestore for the SyntaxEvents application.
 *
 * <p>
 * Handles writing notifications and notification logs to Firestore.
 * Does not contain any role-based or UI logic — that belongs in the fragment.
 * </p>
 */
public class NotificationRepository {

    private FirebaseFirestore db;

    private static final String COLLECTION_NOTIFICATIONS = "notifications";
    private static final String COLLECTION_LOGS = "notificationLogs";

    public NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    protected NotificationRepository(boolean testMode) {}

    /**
     * Writes a {@link Notification} document to Firestore and stores
     * its generated ID back onto the object.
     *
     * @param notification the notification to store
     * @param callback     success or failure
     */
    public void sendNotification(Notification notification, NotificationCallback callback) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    String notificationId = documentReference.getId();
                    notification.setNotificationId(notificationId);
                    documentReference.update("notificationId", notificationId)
                            .addOnSuccessListener(unused -> callback.onComplete(true))
                            .addOnFailureListener(e -> callback.onComplete(false));
                })
                .addOnFailureListener(e -> callback.onComplete(false));
    }

    /**
     * Writes one {@link NotificationLog} record to Firestore.
     * Call this once per recipient after a notification is sent.
     * Fire-and-forget — failures are silent and don't block the UI.
     *
     * @param log the log record to store
     */
    public void writeLog(NotificationLog log) {
        db.collection(COLLECTION_LOGS).add(log);
    }

    /**
     * Retrieves all notifications sent for a given event,
     * ordered by timestamp descending.
     *
     * @param eventId  the event to query
     * @param callback list of notifications or null on failure
     */
    public void getNotificationsForEvent(String eventId, NotificationListCallback callback) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("eventId", eventId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot ->
                        callback.onLoaded(snapshot.toObjects(Notification.class)))
                .addOnFailureListener(e -> callback.onLoaded(null));
    }
    public void getNotificationsForUser(String eventId, NotificationListCallback callback) {
        db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("eventId", eventId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot ->
                        callback.onLoaded(snapshot.toObjects(Notification.class)))
                .addOnFailureListener(e -> callback.onLoaded(null));
    }

    /**
     * Retrieves all {@link NotificationLog} records for a given event.
     * Used by the admin log fragment to audit sent notifications.
     *
     * @param eventId  the event to query logs for
     * @param callback list of logs or null on failure
     */
    public void getLogsForEvent(String eventId, NotificationLogListCallback callback) {
        db.collection(COLLECTION_LOGS)
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(snapshot ->
                        callback.onLoaded(snapshot.toObjects(NotificationLog.class)))
                .addOnFailureListener(e -> callback.onLoaded(null));
    }

    // ─── Callbacks ─────────────────────────────────────────────────────────

    public interface NotificationCallback {
        void onComplete(boolean success);
    }

    public interface NotificationListCallback {
        void onLoaded(List<Notification> notifications);
    }

    public interface NotificationLogListCallback {
        void onLoaded(List<NotificationLog> logs);
    }
}
