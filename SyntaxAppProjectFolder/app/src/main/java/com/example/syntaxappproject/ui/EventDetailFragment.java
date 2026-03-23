package com.example.syntaxappproject.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.fragment.NavHostFragment;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.EventDetail;
import com.example.syntaxappproject.EventDetailRepository;
import com.example.syntaxappproject.EventJoinRepository;
import com.example.syntaxappproject.ImageCacheManager;
import com.example.syntaxappproject.ImageItem;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Fragment that displays full details for a single event, including its poster,
 * QR code, metadata, and a context-sensitive action button.
 */
public class EventDetailFragment extends HomeBar {

    private String eventId;

    /** Service for retrieving the current authenticated user's UID. */
    private final AuthenticationService authService = new AuthenticationService();

    /** Repository for join, leave, and membership check operations. */
    private final EventJoinRepository joinRepo = new EventJoinRepository();

    /** When {@code true}, bypasses Firestore and NavController for instrumented tests. */
    public boolean testingMode = false;

    /** UID of the currently authenticated user. */
    private String uid;

    /** Editing the image of an event TODO: extract code here and @CreateEventUploadPosterFragment into a static function */
    private ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), selectedImageUri -> {
                if (selectedImageUri != null) {
                    ContentResolver resolver = requireContext().getContentResolver();
                    try {
                        InputStream imageStream = resolver.openInputStream(selectedImageUri);
                        Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);
                        String imageType = resolver.getType(selectedImageUri);

                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        if (imageType != null && imageType.equals("image/png")) {
                            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        } else {
                            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        }

                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        String base64Image = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
                        ImageCacheManager.put(eventId, imageBitmap);

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference reference = database.getReference("event_posters");

                        HashMap<String, String> imageData = new HashMap<>();
                        imageData.put("image", base64Image);
                        imageData.put("type", imageType);

                        reference.child(eventId).setValue(imageData).addOnSuccessListener(aVoid -> {
                            if (isAdded()) {
                                ImageView eventPoster = getView().findViewById(R.id.eventPoster);
                                if (eventPoster != null) eventPoster.setImageBitmap(imageBitmap);
                                Toast.makeText(getContext(), "Poster updated", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    /**
     * Inflates the event detail layout.
     *
     * @param inflater           the layout inflater
     * @param container          the parent view group
     * @param savedInstanceState previously saved state, or {@code null}
     * @return the inflated root view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_detail, container, false);
    }

    /**
     * Binds views, runs entrance animations, loads event data, and configures
     * the action button based on whether the current user is the organizer or an entrant.
     *
     * @param view               the root view returned by {@link #onCreateView}
     * @param savedInstanceState previously saved state, or {@code null}
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            testingMode = getArguments().getBoolean("testingMode", false);
        }
        if (!testingMode) super.onViewCreated(view, savedInstanceState);
        if (!testingMode) setupHotbar(view);

        ImageView eventPoster     = view.findViewById(R.id.eventPoster);
        ImageView eventQRCode     = view.findViewById(R.id.eventQRCode);
        TextView eventName        = view.findViewById(R.id.eventName);
        TextView description      = view.findViewById(R.id.eventDescription);
        TextView date             = view.findViewById(R.id.eventDate);
        TextView location         = view.findViewById(R.id.eventLocation);
        TextView regiPeriod       = view.findViewById(R.id.eventRegiPeriod);
        TextView capacity         = view.findViewById(R.id.eventCapacity);
        TextView wLCount          = view.findViewById(R.id.eventWLCount);
        TextView lotteryCriteria  = view.findViewById(R.id.eventLotteryCriteria);
        MaterialButton joinButton = view.findViewById(R.id.joinButton);
        MaterialButton doneButton = view.findViewById(R.id.doneButton);
        View headerTitle          = view.findViewById(R.id.headerTitle);
        View posterCard           = view.findViewById(R.id.posterCard);
        View nameCard             = view.findViewById(R.id.nameCard);
        View detailsCard          = view.findViewById(R.id.detailsCard);
        View actionCard           = view.findViewById(R.id.actionCard);
        View btnViewSignups       = view.findViewById(R.id.btnViewSignups);
        View editImageText        = view.findViewById(R.id.editImageText);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start();
        posterCard.setTranslationY(30f);
        posterCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(200).start();
        nameCard.setTranslationY(30f);
        nameCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(300).start();
        detailsCard.setTranslationY(30f);
        detailsCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(380).start();
        actionCard.setTranslationY(30f);
        actionCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(500).start();

        doneButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        uid = authService.getCurrentUserId();
        if (testingMode && uid == null) uid = "test_user";

        if(uid != null) btnViewSignups.setVisibility(View.VISIBLE);

        if (testingMode) {
            joinButton.setOnClickListener(v -> {
                if (uid == null) return;
                String currentText = wLCount.getText().toString();
                int currentCount = Integer.parseInt(currentText.replaceAll("[^0-9]", ""));
                if ("Join".equals(joinButton.getText().toString())) {
                    joinButton.setText("Leave");
                    wLCount.setText("Waitlist: " + (currentCount + 1));
                } else {
                    joinButton.setText("Join");
                    wLCount.setText("Waitlist: " + (currentCount - 1));
                }
            });
            return;
        }


        new EventDetailRepository().getEventDetail(eventId, event -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                eventName.setText(event.getName());
                description.setText(event.getDescription());
                date.setText(event.getStartingEventDate());
                location.setText(event.getLocation());
                regiPeriod.setText(event.getStartingRegistrationPeriod());
                capacity.setText("Capacity: " + event.getCapacity());
                wLCount.setText("Waitlist: " + event.getWaitlistCount());
                lotteryCriteria.setText(event.getLotteryCriteria());

                loadPoster(eventPoster, event);
                loadQRCode(eventQRCode);
                configureActionButton(joinButton, wLCount, event);
            });
        });
    }

    /**
     * Loads the event poster from {@link ImageCacheManager} if available,
     * otherwise fetches and decodes it from Firebase Realtime Database.
     *
     * @param eventPoster the {@link ImageView} to display the poster in
     */
    private void loadPoster(ImageView eventPoster, EventDetail event) {
        if (uid != null && uid.equals(event.getOrganizerUid())) {
            eventPoster.setOnClickListener(v -> {
                imagePickerLauncher.launch("image/*");
            });
        }

        if (ImageCacheManager.has(eventId)) {
            eventPoster.setImageBitmap(ImageCacheManager.get(eventId));
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
                        requireActivity().runOnUiThread(() -> eventPoster.setImageBitmap(bitmap));
                    } catch (Exception ignored) {}
                }).start();
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    /**
     * Fetches and decodes the event QR code from Firebase Realtime Database
     * and displays it in the given {@link ImageView}.
     *
     * @param eventQRCode the {@link ImageView} to display the QR code in
     */
    private void loadQRCode(ImageView eventQRCode) {
        FirebaseDatabase.getInstance()
                .getReference("event_qr_codes")
                .child(eventId)
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
                            requireActivity().runOnUiThread(() -> eventQRCode.setImageBitmap(bitmap));
                        } catch (Exception ignored) {}
                    }).start();
                });
    }

    /**
     * Configures the action button based on the current user's role relative to the event.
     *
     * <p>If the user is the organizer, the button becomes a red "Delete Event" button.
     * If the user is an entrant, the button reflects the registration window state:
     * disabled with an informational label if outside the window, or an active
     * Join/Leave toggle if within it.</p>
     *
     * @param joinButton the action {@link MaterialButton} to configure
     * @param wLCount    the {@link TextView} displaying the current waitlist count
     * @param event      the {@link EventDetail} for the event being displayed
     */
    private void configureActionButton(MaterialButton joinButton, TextView wLCount, EventDetail event) {
        boolean isOrganizer = uid != null && uid.equals(event.getOrganizerUid());

        if (isOrganizer) {
            joinButton.setText("Delete Event");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#E74C3C")));
            joinButton.setOnClickListener(v -> deleteEvent());
            return;
        }

        long now      = System.currentTimeMillis();
        long regStart = parseDateMillis(event.getStartingRegistrationPeriod());
        long regEnd   = parseDateMillis(event.getEndingRegistrationPeriod());
        boolean inWindow = regStart != -1 && regEnd != -1 && now >= regStart && now <= regEnd;

        if (!inWindow) {
            joinButton.setText(now < regStart ? "Registration Not Open" : "Registration Closed");
            joinButton.setAlpha(0.5f);
            joinButton.setEnabled(false);
            return;
        }

        joinRepo.hasJoined(eventId, uid, joined ->
                requireActivity().runOnUiThread(() -> joinButton.setText(joined ? "Leave" : "Join"))
        );
        joinButton.setEnabled(true);
        joinButton.setAlpha(1f);
        joinButton.setOnClickListener(v -> handleJoinLeave(joinButton, wLCount));
    }

    /**
     * Handles the join or leave action when the entrant taps the action button.
     *
     * <p>Re-fetches the latest event data to check capacity before joining.
     * Updates the button state and waitlist count on success.</p>
     *
     * @param joinButton the action button to update after the operation
     * @param wLCount    the waitlist count {@link TextView} to update
     */
    private void handleJoinLeave(MaterialButton joinButton, TextView wLCount) {
        if (uid == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        new EventDetailRepository().getEventDetail(eventId, freshEvent -> {
            if (!isAdded()) return;
            long eventCapacity = freshEvent.getCapacity();
            long waitlistCount = freshEvent.getWaitlistCount();

            joinRepo.hasJoined(eventId, uid, joined -> requireActivity().runOnUiThread(() -> {
                if (joined) {
                    joinRepo.leaveEvent(eventId, uid, success -> requireActivity().runOnUiThread(() -> {
                        if (success) {
                            joinButton.setText("Join");
                            joinButton.setAlpha(1f);
                            joinButton.setEnabled(true);
                            int c = Integer.parseInt(wLCount.getText().toString().replaceAll("[^0-9]", ""));
                            wLCount.setText("Waitlist: " + (c - 1));
                            Toast.makeText(getContext(), "You left the event", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to leave", Toast.LENGTH_SHORT).show();
                        }
                    }));
                } else {
                    if (eventCapacity > 0 && waitlistCount >= eventCapacity) {
                        joinButton.setAlpha(0.5f);
                        joinButton.setText("Waitlist Full");
                        joinButton.setEnabled(false);
                        Toast.makeText(getContext(), "Event is at capacity", Toast.LENGTH_SHORT).show();
                    } else {
                        joinRepo.joinEvent(eventId, uid, success -> requireActivity().runOnUiThread(() -> {
                            if (success) {
                                joinButton.setText("Leave");
                                int c = Integer.parseInt(wLCount.getText().toString().replaceAll("[^0-9]", ""));
                                wLCount.setText("Waitlist: " + (c + 1));
                                Toast.makeText(getContext(), "Successfully joined!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Join failed", Toast.LENGTH_SHORT).show();
                            }
                        }));
                    }
                }
            }));
        });
    }

    /**
     * Deletes the event from Firestore and removes its associated poster and QR code
     * from Firebase Realtime Database and the local image cache.
     *
     * <p>Navigates back to the previous screen on success.</p>
     */
    private void deleteEvent() {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .delete()
                .addOnSuccessListener(a -> {
                    ImageCacheManager.remove(eventId);
                    FirebaseDatabase.getInstance().getReference("event_posters").child(eventId).removeValue();
                    FirebaseDatabase.getInstance().getReference("event_qr_codes").child(eventId).removeValue();
                    if (!isAdded()) return;
                    Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).popBackStack();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to delete event", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Parses a date string into epoch milliseconds.
     * Supports {@code "yyyy-MM-dd"} and {@code "MM/dd/yyyy"} formats.
     *
     * @param dateStr the date string to parse
     * @return epoch milliseconds, or {@code -1} if the string is null, empty, or unparseable
     */
    private long parseDateMillis(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return -1;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            sdf.setLenient(false);
            return sdf.parse(dateStr).getTime();
        } catch (Exception ignored) {}
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault());
            sdf.setLenient(false);
            return sdf.parse(dateStr).getTime();
        } catch (Exception ignored) {}
        return -1;
    }
}
