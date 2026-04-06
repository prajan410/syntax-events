package com.example.syntaxappproject;



import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.LinkedHashSet;

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
    public void sendNotification(Notification notification, String eventId, List<String> targetGroups, NotificationCallback callback) {
        Log.d("NotifDebug", "sendNotification called, eventId=" + eventId + " groups=" + targetGroups);
        db.collection("notifications").add(notification);
        // Map each targetGroup to its Firestore subcollection
        if ("ADMINISTRATION".equals(eventId)) {
            db.collection("profiles").get()
                    .addOnSuccessListener(snapshot -> {
                        List<Task<Void>> writeTasks = Collections.synchronizedList(new ArrayList<>());
                        for (DocumentSnapshot userDoc : snapshot.getDocuments()) {
                            boolean wantsAdmin = !Boolean.FALSE.equals(userDoc.getBoolean("adminNotificationEnabled"));
                            if (!wantsAdmin) continue;

                            Task<Void> writeTask = db.collection("users")
                                    .document(userDoc.getId())
                                    .collection("notifications")
                                    .add(notification)
                                    .continueWith(task -> null);
                            writeTasks.add(writeTask);
                        }
                        Tasks.whenAll(writeTasks)
                                .addOnSuccessListener(v -> callback.onComplete(true))
                                .addOnFailureListener(e -> callback.onComplete(false));
                    })
                    .addOnFailureListener(e -> callback.onComplete(false));
            return; // ← don't fall through to the event-based logic
        }

        List<String> subcollections = new ArrayList<>();
        for (String group : targetGroups) {
            switch (group) {
                case "WAITLIST":  subcollections.add("waitlist-entrants"); break;
                case "SELECTED":  subcollections.add("selected-entrants"); break;  // update to your actual name
                case "CANCELLED": subcollections.add("cancelled-entrants"); break; // update to your actual name
                case "ALL":
                    subcollections.add("waitlist-entrants");
                    subcollections.add("selected-entrants");
                    subcollections.add("cancelled-entrants");
                    break;
            }
        }

        // Deduplicate subcollection names (e.g. ALL + WAITLIST would double-add waitlist)
        List<String> uniqueSubcollections = new ArrayList<>(new LinkedHashSet<>(subcollections));

        // Fetch all subcollections, collect all memberIds, then write once per unique member
        Set<String> alreadyWritten = Collections.synchronizedSet(new HashSet<>());
        List<Task<Void>> writeTasks = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger collectionsRemaining = new AtomicInteger(uniqueSubcollections.size());

        for (String subcollection : uniqueSubcollections) {
            db.collection("events").document(eventId)
                    .collection(subcollection)
                    .get()
                    .addOnSuccessListener(memberSnapshot -> {
                        Log.d("NotifDebug", "subcollection=" + subcollection + " members=" + memberSnapshot.size());
                        List<DocumentSnapshot> members = memberSnapshot.getDocuments();

                        if (members.isEmpty()) {
                            if (collectionsRemaining.decrementAndGet() == 0) {
                                Tasks.whenAll(writeTasks)
                                        .addOnSuccessListener(v -> callback.onComplete(true))
                                        .addOnFailureListener(e -> callback.onComplete(false));
                            }
                            return;
                        }

                        AtomicInteger membersRemaining = new AtomicInteger(members.size());

                        for (DocumentSnapshot memberDoc : members) {
                            String memberId = memberDoc.getId();

                            db.collection("profiles").document(memberId).get()
                                    .addOnSuccessListener(userDoc -> {
                                        boolean wantsOrganizer = Boolean.TRUE.equals(userDoc.getBoolean("organizerNotificationEnabled"));
                                        boolean wantsAdmin     = Boolean.TRUE.equals(userDoc.getBoolean("adminNotificationEnabled"));
                                        String senderRole      = notification.getSenderRole();

                                        boolean filteredOut = ("ORGANIZER".equals(senderRole) && !wantsOrganizer)
                                                || ("ADMIN".equals(senderRole)     && !wantsAdmin);

                                        if (!filteredOut && alreadyWritten.add(memberId)) {
                                            Log.d("NotifDebug", "writing notif to users/" + memberId + "/notifications");
                                            Task<Void> writeTask = db.collection("users")
                                                    .document(memberId)
                                                    .collection("notifications")
                                                    .add(notification)
                                                    .continueWith(task -> {
                                                        Log.d("NotifDebug", "write result for " + memberId + ": success=" + task.isSuccessful());
                                                        return null;
                                                    });
                                            writeTasks.add(writeTask);
                                        } else {
                                            Log.d("NotifDebug", "skipped " + memberId + (filteredOut ? " opt-out" : " duplicate"));
                                        }

                                        // When all members of this subcollection are processed
                                        if (membersRemaining.decrementAndGet() == 0) {
                                            // When all subcollections are processed
                                            if (collectionsRemaining.decrementAndGet() == 0) {
                                                Tasks.whenAll(writeTasks)
                                                        .addOnSuccessListener(v -> callback.onComplete(true))
                                                        .addOnFailureListener(e -> callback.onComplete(false));
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("NotifDebug", "failed to fetch profile for " + memberId + ": " + e.getMessage());
                                        if (membersRemaining.decrementAndGet() == 0) {
                                            if (collectionsRemaining.decrementAndGet() == 0) {
                                                Tasks.whenAll(writeTasks)
                                                        .addOnSuccessListener(v -> callback.onComplete(true))
                                                        .addOnFailureListener(err -> callback.onComplete(false));
                                            }
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("NotifDebug", "failed to fetch subcollection " + subcollection + ": " + e.getMessage());
                        if (collectionsRemaining.decrementAndGet() == 0) {
                            Tasks.whenAll(writeTasks)
                                    .addOnSuccessListener(v -> callback.onComplete(true))
                                    .addOnFailureListener(err -> callback.onComplete(false));
                        }
                    });
        }
    }


    public void getNotificationsForUser(String userId, NotificationListCallback callback) {
        db.collection("users")
                .document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Notification> notifications = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Notification n = doc.toObject(Notification.class);
                        if (n != null) {
                            n.setNotificationId(doc.getId()); // ← set the Firestore doc ID
                            notifications.add(n);
                        }
                    }
                    Log.d("NotifDebug", "query success, docs=" + notifications.size());
                    callback.onLoaded(notifications);
                })
                .addOnFailureListener(e -> {
                    Log.e("NotifDebug", "query failed: " + e.getMessage());
                    callback.onLoaded(null);
                });
    }


    public void getAllNotifications(NotificationListCallback callback) {
        db.collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Log.d("NotifDebug", "getAllNotifications docs=" + snapshot.size());
                    callback.onLoaded(snapshot.toObjects(Notification.class));
                })
                .addOnFailureListener(e -> {
                    Log.e("NotifDebug", "getAllNotifications failed: " + e.getMessage());
                    callback.onLoaded(null);
                });
    }
    // ─── Callbacks ─────────────────────────────────────────────────────────

    public interface NotificationCallback {
        void onComplete(boolean success);
    }

    public interface NotificationListCallback {
        void onLoaded(List<Notification> notifications);
    }


}
