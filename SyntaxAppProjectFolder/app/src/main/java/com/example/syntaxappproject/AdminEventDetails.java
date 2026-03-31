package com.example.syntaxappproject;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.example.syntaxappproject.ui.AdminCommentDetailsFragment;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Fragment that displays detailed information about a specific event for admin users.
 *
 * <p>This fragment shows:</p>
 * <ul>
 *   <li>Event poster image (loaded from cache or Realtime Database)</li>
 *   <li>Event QR code (loaded from Realtime Database)</li>
 *   <li>Event details: name, description, organizer, location, dates, capacity</li>
 *   <li>Option to delete the event (with confirmation dialog)</li>
 * </ul>
 *
 * <p>Event data is passed via Bundle arguments from the previous fragment.
 * The fragment also fetches additional event data from Firestore if needed.</p>
 *
 * <p>Navigation: Clicking Done returns to the previous screen via {@code popBackStack()}.
 * Clicking Delete permanently removes the event and its associated media.</p>
 *
 * @see AdminBrowseEvents
 * @see AdminCommentDetailsFragment
 * @see ImageCacheManager
 */
public class AdminEventDetails extends Fragment {

    /** Default constructor required for fragment instantiation. */
    public AdminEventDetails() {}

    /**
     * Inflates the layout, initializes views, loads event data, and sets up click listeners.
     *
     * @param inflater           the layout inflater used to inflate the fragment's view
     * @param container          the parent view group that the fragment's UI attaches to
     * @param savedInstanceState previously saved instance state, if any
     * @return the inflated view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_event_details, container, false);

        AppCompatButton removeButton = view.findViewById(R.id.btn_remove_event);
        AppCompatButton doneButton = view.findViewById(R.id.doneButton);
        ImageView posterImage = view.findViewById(R.id.iv_event_poster);
        ImageView qrImage = view.findViewById(R.id.iv_event_qr_code);
        TextView titleText = view.findViewById(R.id.tv_detail_event_title);
        TextView descriptionText = view.findViewById(R.id.tv_detail_event_description);
        TextView organizerText = view.findViewById(R.id.tv_detail_event_organizer);
        TextView locationText = view.findViewById(R.id.tv_detail_event_location);
        TextView datesText = view.findViewById(R.id.tv_detail_event_dates);
        TextView regText = view.findViewById(R.id.tv_detail_event_reg);
        TextView capacityText = view.findViewById(R.id.tv_detail_event_capacity);

        animateHeaderAndCard(view);

        Bundle args = getArguments();
        if (args == null) return view;

        String eventId = args.getString("eventId");

        populateEventDetails(args, titleText, descriptionText, organizerText,
                locationText, datesText, regText, capacityText);
        loadPoster(eventId, posterImage);
        loadQRCode(eventId, qrImage);
        removeButton.setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this event?")
                        .setPositiveButton("Delete", (dialog, which) -> deleteEvent(eventId))
                        .setNegativeButton("Cancel", null)
                        .show()
        );
        doneButton.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).popBackStack();
        });

        return view;
    }

    /**
     * Applies slide-up and fade-in animations to the header and poster card.
     *
     * @param root the root view containing the elements to animate
     */
    private void animateHeaderAndCard(View root) {
        View header = root.findViewById(R.id.tv_event_details_title);
        View posterCard = root.findViewById(R.id.posterCard);

        header.setTranslationY(-20f);
        header.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        posterCard.setTranslationY(30f);
        posterCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(200).start();
    }

    /**
     * Populates the event detail text views with data from the Bundle.
     * Uses default values for missing fields.
     *
     * @param args            the Bundle containing event data
     * @param titleText       TextView for event name
     * @param descriptionText TextView for event description
     * @param organizerText   TextView for organizer UID
     * @param locationText    TextView for event location
     * @param datesText       TextView for event date range
     * @param regText         TextView for registration period range
     * @param capacityText    TextView for event capacity
     */
    private void populateEventDetails(Bundle args, TextView titleText, TextView descriptionText,
                                      TextView organizerText, TextView locationText,
                                      TextView datesText, TextView regText, TextView capacityText) {

        titleText.setText(args.getString("name", "Unknown event"));
        descriptionText.setText(args.getString("description", "No description"));
        organizerText.setText("Organizer UID: " + args.getString("organizerUid", "—"));
        locationText.setText(args.getString("location", "No location set"));
        String startEvent = args.getString("startingEventDate");
        String endEvent = args.getString("endingEventDate");
        datesText.setText((startEvent != null && endEvent != null) ? startEvent + " → " + endEvent : "—");
        String startReg = args.getString("startingRegistrationPeriod");
        String endReg = args.getString("endingRegistrationPeriod");
        regText.setText((startReg != null && endReg != null) ? startReg + " → " + endReg : "—");
        long capacity = args.getLong("capacity", 0);
        capacityText.setText(capacity > 0 ? String.valueOf(capacity) : "Unlimited");
    }

    /**
     * Permanently deletes the event from Firestore and removes its associated media
     * (poster and QR code) from Realtime Database. Navigates back on success.
     *
     * @param eventId the Firestore document ID of the event to delete
     */
    private void deleteEvent(String eventId) {
        FirebaseFirestore.getInstance().collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(unused -> {
                    FirebaseDatabase.getInstance().getReference("event_posters").child(eventId).removeValue();
                    FirebaseDatabase.getInstance().getReference("event_qr_codes").child(eventId).removeValue();
                    if (isAdded()) NavHostFragment.findNavController(this).navigateUp();
                });
    }

    /**
     * Loads the event poster from cache or fetches and decodes it from Realtime Database.
     * Uses background thread for decoding to avoid blocking the UI.
     *
     * @param eventId     the ID of the event
     * @param posterImage the ImageView to display the poster in
     */
    private void loadPoster(String eventId, ImageView posterImage) {
        if (ImageCacheManager.has(eventId)) {
            posterImage.setImageBitmap(ImageCacheManager.get(eventId));
            return;
        }

        ImageItem.fetchByEventId(eventId, new ImageItem.ImageCallback() {
            @Override
            public void onImageLoaded(ImageItem imageItem) {
                if (imageItem == null || imageItem.imageUrl == null) return;
                new Thread(() -> {
                    try {
                        byte[] decoded = Base64.decode(imageItem.imageUrl, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        if (bitmap == null || !isAdded()) return;
                        ImageCacheManager.put(eventId, bitmap);
                        requireActivity().runOnUiThread(() -> posterImage.setImageBitmap(bitmap));
                    } catch (Exception ignored) {}
                }).start();
            }

            @Override
            public void onError(Exception e) {}
        });
    }

    /**
     * Fetches and decodes the event QR code from Firebase Realtime Database.
     * Uses background thread for decoding to avoid blocking the UI.
     *
     * @param eventId the ID of the event
     * @param qrImage the ImageView to display the QR code in
     */
    private void loadQRCode(String eventId, ImageView qrImage) {
        FirebaseDatabase.getInstance().getReference("event_qr_codes").child(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || !snapshot.exists() || !isAdded()) return;
                    String base64 = snapshot.child("image").getValue(String.class);
                    if (base64 == null) return;
                    new Thread(() -> {
                        try {
                            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            if (bitmap == null || !isAdded()) return;
                            requireActivity().runOnUiThread(() -> qrImage.setImageBitmap(bitmap));
                        } catch (Exception ignored) {}
                    }).start();
                });
    }
}