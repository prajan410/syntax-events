package com.example.syntaxappproject.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.EventViewModel;
import com.example.syntaxappproject.ImageCacheManager;
import com.example.syntaxappproject.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that allows the organizer to upload an event poster image.
 * <p>
 * The user can select an image from the gallery, preview it, or skip the poster.
 * The event data from {@link EventViewModel} is saved to Firestore; if an image
 * is selected, it is also uploaded to Firebase Realtime Database and cached with
 * {@link ImageCacheManager}. On success, the fragment navigates to the QR code step.
 * </p>
 */
public class CreateEventUploadPosterFragment extends Fragment {

    private ImageView posterPreview;
    private View uploadHint;
    private Uri selectedImageUri;
    private EventViewModel viewModel;

    private final AuthenticationService authService = new AuthenticationService();

    /**
     * Inflates the upload poster layout.
     *
     * @param inflater  the layout inflater
     * @param container the parent view group
     * @param savedInstanceState previously saved state, or {@code null}
     * @return the inflated view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_event_upload_poster, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView}. Binds views, applies
     * entrance animations, and attaches click handlers for poster selection,
     * continue, and skip.
     *
     * @param view               the view returned by {@link #onCreateView}
     * @param savedInstanceState previously saved state, or {@code null}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel     = new ViewModelProvider(requireActivity()).get(EventViewModel.class);
        posterPreview = view.findViewById(R.id.posterPreview);
        uploadHint    = view.findViewById(R.id.uploadHint);

        View headerTitle  = view.findViewById(R.id.headerTitle);
        View stepIndicator = view.findViewById(R.id.stepIndicator);
        View posterCard   = view.findViewById(R.id.posterCard);
        View actionCard   = view.findViewById(R.id.actionCard);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        stepIndicator.animate().alpha(1f)
                .setDuration(300).setStartDelay(200).start();

        posterCard.setTranslationY(30f);
        posterCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(250).start();

        actionCard.setTranslationY(30f);
        actionCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(370).start();

        view.findViewById(R.id.posterTapArea).setOnClickListener(v -> openGallery());

        view.findViewById(R.id.continueToQRButton).setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(getContext(), "Please select a poster", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.setImageUri(selectedImageUri);
            saveEventToFirebase();
        });

        view.findViewById(R.id.skipPosterButton).setOnClickListener(v -> saveEventToFirebase());
    }

    /**
     * Launches the device gallery via an {@link Intent} to allow the
     * organizer to pick an image for the event poster.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    /**
     * Writes all event data collected in {@link EventViewModel} to Firestore
     * as a new document in the {@code events} collection.
     * <p>
     * On success, navigates to the QR code step passing the new event ID.
     * On failure, displays a short toast to the user.
     * </p>
     */
    private void saveEventToFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String organizerUid = authService.getCurrentUserId();

        Map<String, Object> eventData = new HashMap<>();
        eventData.put("name",         viewModel.getName().getValue());
        eventData.put("description",  viewModel.getDescription().getValue());
        eventData.put("location",     viewModel.getLocation().getValue());
        eventData.put("capacity",     viewModel.getCapacity().getValue());
        eventData.put("startingEventDate",     viewModel.getStartingEventDate().getValue());
        eventData.put("endingEventDate",     viewModel.getEndingEventDate().getValue());
        eventData.put("startingRegistrationPeriod",     viewModel.getStartingRegistrationPeriod().getValue());
        eventData.put("endingRegistrationPeriod",     viewModel.getEndingRegistrationPeriod().getValue());
        eventData.put("organizerUid", organizerUid);
        eventData.put("privateEvent", Boolean.TRUE.equals(viewModel.getPrivateEvent().getValue()));
        eventData.put("invitedUserIds", viewModel.getInvitedUserIds().getValue());
        eventData.put("waitlistCount", 0);
        eventData.put("geoReq", Boolean.TRUE.equals(viewModel.getGeoReq().getValue()));

        db.collection("events")
                .add(eventData)
                .addOnSuccessListener(documentReference -> {
                    String eventId = documentReference.getId();
                    viewModel.setEventId(eventId);

                    boolean isPrivate = Boolean.TRUE.equals(viewModel.getPrivateEvent().getValue());

                    if (selectedImageUri != null) {
                        uploadPosterToRealtimeDatabase(eventId, isPrivate);
                    } else {
                        afterEventSaved(eventId, isPrivate);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show()
                );
    }

    /**
     * Uploads the selected poster image to Firebase Realtime Database.
     * <p>
     * Reads the image from {@link #selectedImageUri}, compresses it as PNG or JPEG,
     * Base64‑encodes it, and stores it under {@code event_posters/{eventId}}.
     * The bitmap is also cached in {@link ImageCacheManager}. On success or failure,
     * the fragment navigates to the QR code step.
     * </p>
     *
     * @param eventId the ID of the event whose poster is being uploaded
     */
    private void uploadPosterToRealtimeDatabase(String eventId, boolean isPrivate) {
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

            reference.child(eventId).setValue(imageData)
                    .addOnSuccessListener(aVoid -> afterEventSaved(eventId, isPrivate))
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to upload poster", Toast.LENGTH_SHORT).show();
                        afterEventSaved(eventId, isPrivate);
                    });
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
            afterEventSaved(eventId, isPrivate);
        }
    }
    private void afterEventSaved(String eventId, boolean isPrivate) {
        if (isPrivate) {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);
            bundle.putString("eventName", viewModel.getName().getValue());

            NavHostFragment.findNavController(this)
                    .navigate(R.id.privateEventInviteFragment, bundle);
        } else {
            navigateToQRFragment(eventId);
        }
    }

    private void navigateToQRFragment(String eventId) {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", eventId);
        NavHostFragment.findNavController(this)
                .navigate(R.id.toCreateEventQRFragment, bundle);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            posterPreview.setImageURI(selectedImageUri);
                            if (uploadHint != null) uploadHint.setVisibility(View.GONE);
                        }
                    });
}
