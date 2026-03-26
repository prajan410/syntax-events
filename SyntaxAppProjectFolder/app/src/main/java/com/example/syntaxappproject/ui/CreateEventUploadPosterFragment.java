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
import com.example.syntaxappproject.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

        // --- Entrance Animations ---
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

        // --- Tap to pick image ---
        view.findViewById(R.id.posterTapArea).setOnClickListener(v -> openGallery());

        // --- Continue button ---
        view.findViewById(R.id.continueToQRButton).setOnClickListener(v -> {
            if (selectedImageUri == null) {
                Toast.makeText(getContext(), "Please select a poster", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.setImageUri(selectedImageUri);
            saveEventToFirebase();
        });

        // --- Skip button ---
        view.findViewById(R.id.skipPosterButton).setOnClickListener(v -> saveEventToFirebase());
    }


    /**
     * Writes all event data collected in {@link EventViewModel} to Firestore
     * as a new document in the {@code events} collection.
     * <p>
     * On success, navigates to the QR code step passing the new event ID.
     * On failure, displays a short toast to the user.
     * </p>
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

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

        db.collection("events")
                .add(eventData)
                .addOnSuccessListener(documentReference -> {
                    String eventId = documentReference.getId();
                    viewModel.setEventId(eventId);

                    // If we have an image, upload it to Realtime Database
                    if (selectedImageUri != null) {
                        uploadPosterToRealtimeDatabase(eventId);
                    } else {
                        navigateToQRFragment(eventId);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show()
                );
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
     * Handles the result from the gallery picker.
     * <p>
     * If the user selected an image, stores the URI in {@link #selectedImageUri},
     * displays a preview in {@link #posterPreview}, and hides the upload hint.
     * </p>
     */
    private void uploadPosterToRealtimeDatabase(String eventId) {
        ContentResolver resolver = requireContext().getContentResolver();
        try {
            InputStream imageStream = resolver.openInputStream(selectedImageUri);
            Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);
            String imageType = resolver.getType(selectedImageUri);

            // Check for image type
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            if (imageType != null && imageType.equals("image/png")) {
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            } else {
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            }

            // Setting up data to push to store in json
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64Image = android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference reference = database.getReference("event_posters");

            HashMap<String, String> imageData = new HashMap<>();
            imageData.put("image", base64Image);
            imageData.put("type", imageType);

            reference.child(eventId).setValue(imageData)
                    .addOnSuccessListener(aVoid -> navigateToQRFragment(eventId))
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to upload poster", Toast.LENGTH_SHORT).show();
                        navigateToQRFragment(eventId); // Navigate anyway
                    });

        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
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
