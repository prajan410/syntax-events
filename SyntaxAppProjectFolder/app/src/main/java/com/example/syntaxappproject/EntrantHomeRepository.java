package com.example.syntaxappproject;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.List;
/**
 * Repository class responsible for check and add co-organizer
 * <p>
 * This repository acts as the data access layer between the application
 * and the Firestore backend. It provides methods to check and add co-organizer
 * using an event ID.
 * </p>
 */
public class EntrantHomeRepository {
    private FirebaseFirestore db;
    private CollectionReference eventsRef;
    /**
     * Constructs a EventDetailRepository and initializes the Firestore instance.
     */
    public EntrantHomeRepository(){
        db = FirebaseFirestore.getInstance();
    }
    /**
     * Callback interface for operations that success return a list of events
     */
    public interface EventCallback {
        /**
         * Called when the event retrieval completes.
         *
         * @param events the retrieved a list of {@link EventDetail}
         */
        void onSuccess(List<EventDetail> events);
    }

    /**
     * Get the events from the firebase
     * @param callback the {@link EntrantHomeRepository.EventCallback} invoked with the result,
     */
    public void getEvents(EventCallback callback) {

        eventsRef = db.collection("events");
        eventsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            List<EventDetail> events = new ArrayList<>();

            for (DocumentSnapshot doc : queryDocumentSnapshots) {

                EventDetail event = doc.toObject(EventDetail.class);

                if (event != null) {
                    event.setEventId(doc.getId());
                    events.add(event);
                }
            }

            callback.onSuccess(events);
        });
    }
}
