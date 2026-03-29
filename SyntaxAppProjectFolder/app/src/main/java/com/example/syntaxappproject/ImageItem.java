package com.example.syntaxappproject;
/**
 * model class that stores the data for a single image
 * it keeps information related to an uploaded image
 * each object represents one image item in the system
 */

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ImageItem {

    public String imageUrl;
    public String uploadedBy;

    /**
     * empty constructor required for firestore deserialization
     */
    public ImageItem() {
    }
    /**
     * creates an image item with its basic information
     *
     * @param imageUrl url of the uploaded image
     * @param uploadedBy id of the user who uploaded the image
     */
    public ImageItem(String imageUrl, String uploadedBy) {
        this.imageUrl = imageUrl;
        this.uploadedBy = uploadedBy;
    }

    /**
     * Callback interface for asynchronous database retrieval
     */
    public interface ImageCallback {
        void onImageLoaded(ImageItem imageItem);
        void onError(Exception e);
    }

    /**
     * Fetches an image from the Realtime Database using the eventID.
     *
     * @param eventId  The ID of the event whose poster to fetch.
     * @param callback The callback to handle the result or error.
     */
    public static void fetchByEventId(String eventId, ImageCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("event_posters")
                .child(eventId);

        ref.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String base64Image = dataSnapshot.child("image").getValue(String.class);
                ImageItem item = new ImageItem(base64Image, null);
                callback.onImageLoaded(item);
            } else {
                callback.onImageLoaded(null);
            }
        }).addOnFailureListener(callback::onError);
    }
}