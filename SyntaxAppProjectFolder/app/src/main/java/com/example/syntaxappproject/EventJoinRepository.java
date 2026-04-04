package com.example.syntaxappproject;

import android.location.Address;
import android.location.Geocoder;
import android.content.Context;

import android.util.Log;

import com.example.syntaxappproject.ui.EventDetailFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Repository class responsible for managing event waitlist membership operations
 * in Firebase Firestore for the SyntaxEvents application.
 *
 * <p>This repository acts as the data access layer for all waitlist-related operations,
 * including joining an event's waitlist, leaving the waitlist, and checking membership
 * status. It also provides geolocation validation for events that require it.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li>Join event waitlist with optional location tracking</li>
 *   <li>Leave event waitlist</li>
 *   <li>Check if a user has already joined an event's waitlist</li>
 *   <li>Geolocation validation for events with geoReq enabled (compares user's city with event city)</li>
 *   <li>Automatic waitlist count increment/decrement using Firestore transactions</li>
 * </ul>
 *
 * <p>All operations are asynchronous and use Firestore transactions to ensure
 * data consistency, particularly for count increments/decrements.</p>
 *
 * @see EventDetail
 * @see EventDetailFragment
 */
public class EventJoinRepository {

    /** Firestore database instance. */
    private FirebaseFirestore db = null;

    /** Android context required for Geocoder operations. */
    private Context context;

    /**
     * Constructs an EventJoinRepository and initializes the Firestore instance.
     */
    public EventJoinRepository() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * Sets the Android context required for geocoding operations.
     * Must be called before any geolocation validation is performed.
     *
     * @param context the Android application context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Protected constructor for unit testing. Allows creating a repository
     * without initializing a live Firestore connection.
     *
     * @param testMode if true, skips Firestore initialization
     */
    protected EventJoinRepository(boolean testMode) {
    }

    /**
     * Checks whether a user has already joined the waitlist for a specific event.
     *
     * @param eventId  the Firestore document ID of the event
     * @param userId   the ID of the user to check
     * @param callback callback invoked with the result (true if joined, false otherwise)
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
                .addOnSuccessListener(doc -> callback.onResult(doc.exists()));
    }

    /**
     * Adds a user to the event's waitlist with optional location validation.
     *
     * <p>If geolocation is required for the event (geoReq = true), this method:
     * <ol>
     *   <li>Validates that the user provided location coordinates</li>
     *   <li>Converts the user's coordinates to a city name using Geocoder</li>
     *   <li>Compares the user's city with the event's city</li>
     *   <li>If cities match, allows the join; otherwise rejects with an error</li>
     * </ol>
     * </p>
     *
     * <p>If geolocation is not required, the user is added to the waitlist regardless
     * of location, but any provided location coordinates are still stored.</p>
     *
     * <p>The operation uses a Firestore transaction to ensure atomicity:
     * <ul>
     *   <li>Checks if the user already joined (throws exception if true)</li>
     *   <li>Adds the user to the waitlist-entrants subcollection</li>
     *   <li>Increments the event's waitlistCount by 1</li>
     * </ul>
     * </p>
     *
     * @param eventId   the Firestore document ID of the event
     * @param userId    the ID of the user joining the waitlist
     * @param latitude  the user's current latitude (may be null if location unavailable)
     * @param longitude the user's current longitude (may be null if location unavailable)
     * @param callback  callback invoked with success/failure and optional message
     */
    public void joinEvent(String eventId, String userId, Double latitude, Double longitude, JoinCallback callback) {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        callback.onComplete(false, "Event not found");
                        return;
                    }

                    Boolean geoReq = eventDoc.getBoolean("geoReq");
                    String eventCity = eventDoc.getString("location");
                    Double eventLat = eventDoc.getDouble("eventLatitude");
                    Double eventLng = eventDoc.getDouble("eventLongitude");

                    if (geoReq != null && geoReq) {
                        if (latitude == null || longitude == null) {
                            callback.onComplete(false, "Geolocation is required for this event. Please enable location services.");
                            return;
                        }

                        if (eventLat == null || eventLng == null) {
                            callback.onComplete(false, "Event location not properly configured.");
                            return;
                        }

                        getUserCityFromLocation(latitude, longitude, new CityCallback() {
                            @Override
                            public void onCityReceived(String userCity) {
                                if (userCity == null || userCity.isEmpty()) {
                                    callback.onComplete(false, "Could not determine your current city.");
                                    return;
                                }

                                if (userCity.equalsIgnoreCase(eventCity)) {
                                    performJoinTransaction(eventId, userId, latitude, longitude, callback);
                                } else {
                                    callback.onComplete(false, String.format(
                                            "You must be in %s to join this event. You appear to be in %s.",
                                            eventCity, userCity
                                    ));
                                }
                            }

                            @Override
                            public void onError(String error) {
                                callback.onComplete(false, error);
                            }
                        });
                    } else {
                        performJoinTransaction(eventId, userId, latitude, longitude, callback);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("EventJoinRepo", "Failed to get event details: " + e.getMessage());
                    callback.onComplete(false, "Failed to validate event requirements");
                });
    }

    /**
     * Retrieves the city name from geographic coordinates using the Geocoder.
     * This operation runs on a background thread to avoid blocking the UI.
     *
     * <p>The method attempts to extract the city name from the following address fields
     * in order of preference:
     * <ol>
     *   <li>Locality (city/town name)</li>
     *   <li>Sub-admin area (county/region)</li>
     *   <li>Admin area (state/province)</li>
     * </ol>
     * </p>
     *
     * @param latitude  the user's latitude coordinate
     * @param longitude the user's longitude coordinate
     * @param callback  callback invoked with the city name or error message
     */
    private void getUserCityFromLocation(double latitude, double longitude, CityCallback callback) {
        if (context == null) {
            callback.onError("Context not set. Cannot determine location.");
            return;
        }

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String city = address.getLocality();
                    if (city == null || city.isEmpty()) {
                        city = address.getSubAdminArea();
                    }
                    if (city == null || city.isEmpty()) {
                        city = address.getAdminArea();
                    }

                    final String userCity = city;
                    if (userCity != null && !userCity.isEmpty()) {
                        callback.onCityReceived(userCity);
                    } else {
                        callback.onError("Could not determine your city from your location.");
                    }
                } else {
                    callback.onError("Could not determine your location.");
                }
            } catch (IOException e) {
                Log.e("EventJoinRepo", "Geocoding error: " + e.getMessage());
                callback.onError("Network error. Please try again.");
            }
        }).start();
    }

    /**
     * Performs the actual Firestore transaction to add a user to the waitlist.
     *
     * <p>This method executes the following operations atomically:</p>
     * <ol>
     *   <li>Checks if the user is already on the waitlist (fails if true)</li>
     *   <li>Creates a document for the user in the waitlist-entrants subcollection</li>
     *   <li>Stores the user's location (latitude/longitude, may be null)</li>
     *   <li>Increments the event's waitlistCount by 1</li>
     * </ol>
     *
     * @param eventId   the Firestore document ID of the event
     * @param userId    the ID of the user joining the waitlist
     * @param latitude  the user's latitude (may be null)
     * @param longitude the user's longitude (may be null)
     * @param callback  callback invoked with success/failure and optional message
     */
    private void performJoinTransaction(String eventId, String userId, Double latitude, Double longitude, JoinCallback callback) {
        Log.d("EventJoinRepo", "Storing location - lat: " + latitude + ", lon: " + longitude);

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

                    data.put("latitude", latitude);
                    data.put("longitude", longitude);

                    if (latitude != null && longitude != null) {
                        Log.d("EventJoinRepo", "Location stored: " + latitude + ", " + longitude);
                    } else {
                        Log.d("EventJoinRepo", "No location available, storing null");
                    }

                    transaction.set(docRef, data);

                    DocumentReference eventRef = db.collection("events").document(eventId);
                    transaction.update(eventRef, "waitlistCount", FieldValue.increment(1));

                    return null;
                })
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventJoinRepo", "Join success - location saved");
                    callback.onComplete(true, "Successfully joined!");
                })
                .addOnFailureListener(e -> {
                    Log.e("EventJoinRepo", "Join failed: " + e.getMessage());
                    callback.onComplete(false, e.getMessage());
                });
    }

    /**
     * Removes a user from the event's waitlist.
     *
     * <p>This operation uses a Firestore transaction to ensure atomicity:</p>
     * <ol>
     *   <li>Checks if the user is on the waitlist (fails if false)</li>
     *   <li>Deletes the user's document from the waitlist-entrants subcollection</li>
     *   <li>Decrements the event's waitlistCount by 1</li>
     * </ol>
     *
     * @param eventId  the Firestore document ID of the event
     * @param userId   the ID of the user leaving the waitlist
     * @param callback callback invoked with success/failure and optional message
     */
    public void leaveEvent(String eventId, String userId, JoinCallback callback) {
        db.runTransaction(transaction -> {
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
                })
                .addOnSuccessListener(aVoid -> callback.onComplete(true, "Successfully left the event."))
                .addOnFailureListener(e -> {
                    Log.e("EventJoinRepo", "Leave transaction failed: " + e.getMessage());
                    callback.onComplete(false, "Failed to leave. Please try again.");
                });
    }

    /**
     * Callback interface for join and leave operations.
     *
     * <p>Provides both a simple success callback and an overload that includes
     * a descriptive message for better user feedback.</p>
     */
    public interface JoinCallback {
        /**
         * Called when the join or leave operation completes.
         *
         * @param success true if the operation succeeded, false otherwise
         */
        void onComplete(boolean success);

        /**
         * Called when the join or leave operation completes with a message.
         * This default implementation calls the simple onComplete method.
         *
         * @param success true if the operation succeeded, false otherwise
         * @param message a descriptive message about the operation result
         */
        default void onComplete(boolean success, String message) {
            onComplete(success);
        }
    }

    /**
     * Callback interface for checking waitlist membership status.
     */
    public interface JoinCheckCallback {
        /**
         * Called with the result of a waitlist membership check.
         *
         * @param joined true if the user is on the waitlist, false otherwise
         */
        void onResult(boolean joined);
    }

    /**
     * Internal callback interface for geocoding operations.
     * Converts geographic coordinates to a city name.
     */
    private interface CityCallback {
        /**
         * Called when the city name has been successfully determined.
         *
         * @param city the city name derived from the coordinates
         */
        void onCityReceived(String city);

        /**
         * Called when an error occurs during geocoding.
         *
         * @param error a descriptive error message
         */
        void onError(String error);
    }
}