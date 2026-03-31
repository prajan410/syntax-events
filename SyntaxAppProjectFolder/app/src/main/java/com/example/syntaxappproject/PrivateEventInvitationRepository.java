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

    public PrivateEventInvitationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public interface SearchCallback {
        void onResult(List<Profile> results, List<String> userIds);
    }

    public interface SimpleCallback {
        void onComplete(boolean success);
    }

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

    public void addInvitedUserToEvent(String eventId, String userId, SimpleCallback callback) {
        db.collection("events")
                .document(eventId)
                .update("invitedUserIds", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(unused -> callback.onComplete(true))
                .addOnFailureListener(e -> callback.onComplete(false));
    }
}