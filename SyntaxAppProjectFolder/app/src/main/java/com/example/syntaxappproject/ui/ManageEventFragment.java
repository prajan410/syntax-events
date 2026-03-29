package com.example.syntaxappproject.ui;

import android.app.Activity;
import android.app.AlertDialog;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.EventViewModel;
import com.example.syntaxappproject.ImageCacheManager;
import com.example.syntaxappproject.R;
import com.example.syntaxappproject.BulletPointHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that allows organizers to view and edit their event details,
 * change the event poster, and delete the event.
 *
 * <p>The fragment loads event data by eventId passed as an argument.
 * The action button starts as "Cancel" but changes to "Save" when any
 * changes are detected. Clicking "Save" saves changes and navigates back.
 * Clicking "Cancel" navigates back without saving. Clicking "Delete"
 * shows a confirmation dialog before permanently deleting the event.</p>
 *
 * <p>Extends {@link HomeBar} to inherit bottom navigation bar functionality.</p>
 */
public class ManageEventFragment extends HomeBar {

    private TextInputEditText eventNameInput, descriptionInput, locationInput, capacityInput;
    private TextInputEditText eventStartDateInput, eventEndDateInput;
    private TextInputEditText regisStartDateInput, regisEndDateInput;
    private TextInputEditText lotteryCriteriaInput;
    private SwitchMaterial geoSwitch;
    private ImageView posterPreview;
    private View uploadHint;
    private MaterialButton actionButton;

    /** Firestore document ID of the event being edited. */
    private String eventId;

    /** URI of the selected poster image for upload. */
    private Uri selectedImageUri;

    /** Flag indicating whether the user has made any changes to the form. */
    private boolean hasChanges = false;

    /** Test flag to bypass Firestore calls during instrumented tests. */
    public boolean disableFirestoreForTest = false;

    private final AuthenticationService authService = new AuthenticationService();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_event, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (disableFirestoreForTest) {
            initializeViews(view);
            setupAnimations(view);
            setupDatePickers();
            setupBulletPointBehavior();
            setupClickListeners(view);
            setupChangeListeners();
            return;
        }
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        if (eventId == null) {
            Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        initializeViews(view);
        setupAnimations(view);
        setupDatePickers();
        setupBulletPointBehavior();
        setupClickListeners(view);
        loadEventData();
        setupChangeListeners();
    }

    /**
     * Initializes all view references.
     *
     * @param view the root view of the fragment
     */
    private void initializeViews(View view) {
        eventNameInput = view.findViewById(R.id.eventNameInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        locationInput = view.findViewById(R.id.locationInput);
        capacityInput = view.findViewById(R.id.capacityInput);
        eventStartDateInput = view.findViewById(R.id.eventStartDateInput);
        eventEndDateInput = view.findViewById(R.id.eventEndDateInput);
        regisStartDateInput = view.findViewById(R.id.regisStartDateInput);
        regisEndDateInput = view.findViewById(R.id.regisEndDateInput);
        geoSwitch = view.findViewById(R.id.geolocationSwitch);
        posterPreview = view.findViewById(R.id.posterPreview);
        uploadHint = view.findViewById(R.id.uploadHint);
        actionButton = view.findViewById(R.id.actionButton);
        lotteryCriteriaInput = view.findViewById(R.id.lotteryCriteriaInput);
    }

    /**
     * Applies entrance animations with staggered delays for visual appeal.
     *
     * @param view the root view of the fragment
     */
    private void setupAnimations(View view) {
        View headerTitle = view.findViewById(R.id.headerTitle);
        View posterCard = view.findViewById(R.id.posterCard);
        View detailsCard = view.findViewById(R.id.detailsCard);
        View capacityCard = view.findViewById(R.id.capacityCard);
        View datesCard = view.findViewById(R.id.datesCard);
        View geolocationCard = view.findViewById(R.id.geolocationCard);
        View lotteryCriteriaCard = view.findViewById(R.id.lotteryCriteriaCard);
        View actionButtonsContainer = view.findViewById(R.id.actionButtonsContainer);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start();

        posterCard.setTranslationY(30f);
        posterCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(200).start();

        detailsCard.setTranslationY(30f);
        detailsCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(280).start();

        capacityCard.setTranslationY(30f);
        capacityCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(340).start();

        datesCard.setTranslationY(30f);
        datesCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(400).start();

        geolocationCard.setTranslationY(30f);
        geolocationCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(460).start();

        lotteryCriteriaCard.setTranslationY(30f);
        lotteryCriteriaCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(520).start();

        actionButtonsContainer.setTranslationY(30f);
        actionButtonsContainer.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(580).start();
    }

    /**
     * Sets up date picker dialogs for all date input fields.
     */
    private void setupDatePickers() {
        eventStartDateInput.setOnClickListener(v -> showDatePicker(eventStartDateInput));
        eventEndDateInput.setOnClickListener(v -> showDatePicker(eventEndDateInput));
        regisStartDateInput.setOnClickListener(v -> showDatePicker(regisStartDateInput));
        regisEndDateInput.setOnClickListener(v -> showDatePicker(regisEndDateInput));
    }

    /**
     * Displays a DatePickerDialog and sets the selected date in the target field.
     *
     * @param target the TextInputEditText to populate with the selected date
     */
    private void showDatePicker(TextInputEditText target) {
        android.app.DatePickerDialog picker = new android.app.DatePickerDialog(
                requireContext(),
                (datePicker, year, month, day) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, day);
                    target.setText(date);
                },
                java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
                java.util.Calendar.getInstance().get(java.util.Calendar.MONTH),
                java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }

    /**
     * Sets up bullet point behavior for the lottery criteria input field.
     */
    private void setupBulletPointBehavior() {
        BulletPointHelper.setupBulletPointField(lotteryCriteriaInput);
    }

    /**
     * Configures click listeners for interactive UI elements.
     *
     * @param view the root view of the fragment
     */
    private void setupClickListeners(View view) {
        view.findViewById(R.id.posterTapArea).setOnClickListener(v -> openGallery());

        actionButton.setOnClickListener(v -> {
            if (hasChanges) {
                saveChanges();
            } else {
                Navigation.findNavController(view).navigateUp();
            }
        });

        view.findViewById(R.id.deleteButton).setOnClickListener(v -> showDeleteDialog());
    }

    /**
     * Sets up text change listeners to detect user modifications.
     */
    private void setupChangeListeners() {
        android.text.TextWatcher watcher = new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                markChanges();
            }
        };

        eventNameInput.addTextChangedListener(watcher);
        descriptionInput.addTextChangedListener(watcher);
        locationInput.addTextChangedListener(watcher);
        capacityInput.addTextChangedListener(watcher);
        eventStartDateInput.addTextChangedListener(watcher);
        eventEndDateInput.addTextChangedListener(watcher);
        regisStartDateInput.addTextChangedListener(watcher);
        regisEndDateInput.addTextChangedListener(watcher);
        lotteryCriteriaInput.addTextChangedListener(watcher);
        geoSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> markChanges());
    }

    /**
     * Marks that changes have been made and updates the action button to "Save".
     */
    private void markChanges() {
        if (!hasChanges) {
            hasChanges = true;
            actionButton.setText("Save");
            actionButton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            getResources().getColor(R.color.green, requireContext().getTheme())
                    )
            );
        }
    }

    /**
     * Loads event data from Firestore and populates the form fields.
     * Skips loading if disableFirestoreForTest is true.
     */
    private void loadEventData() {
        if (disableFirestoreForTest) return;

        db.collection("events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        populateFields(documentSnapshot);
                        loadPoster();
                    } else {
                        Toast.makeText(requireContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigateUp();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                });
    }

    /**
     * Populates form fields with data from the Firestore document.
     *
     * @param doc the Firestore document containing event data
     */
    private void populateFields(DocumentSnapshot doc) {
        String name = doc.getString("name");
        String description = doc.getString("description");
        String location = doc.getString("location");
        Long capacity = doc.getLong("capacity");
        String startDate = doc.getString("startingEventDate");
        String endDate = doc.getString("endingEventDate");
        String regisStart = doc.getString("startingRegistrationPeriod");
        String regisEnd = doc.getString("endingRegistrationPeriod");
        Boolean geoRequired = doc.getBoolean("geolocationRequired");
        String lotteryCriteria = doc.getString("lotteryCriteria");

        eventNameInput.setText(name != null ? name : "");
        descriptionInput.setText(description != null ? description : "");
        locationInput.setText(location != null ? location : "");
        capacityInput.setText(capacity != null ? String.valueOf(capacity) : "");
        eventStartDateInput.setText(startDate != null ? startDate : "");
        eventEndDateInput.setText(endDate != null ? endDate : "");
        regisStartDateInput.setText(regisStart != null ? regisStart : "");
        regisEndDateInput.setText(regisEnd != null ? regisEnd : "");
        geoSwitch.setChecked(geoRequired != null && geoRequired);

        if (lotteryCriteria != null && !lotteryCriteria.isEmpty()) {
            String formattedCriteria = BulletPointHelper.formatWithBullets(lotteryCriteria);
            lotteryCriteriaInput.setText(formattedCriteria);
        }
    }

    /**
     * Loads the event poster from Firebase Realtime Database and displays it.
     */
    private void loadPoster() {
        FirebaseDatabase.getInstance().getReference("event_posters")
                .child(eventId)
                .get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        String base64Image = dataSnapshot.child("image").getValue(String.class);
                        if (base64Image != null && !base64Image.isEmpty()) {
                            byte[] decodedString = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            posterPreview.setImageBitmap(bitmap);
                        }
                    }
                })
                .addOnFailureListener(e -> {});
    }

    /**
     * Launches the device gallery to allow the user to select a new poster image.
     */
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    /**
     * Validates input and saves the updated event data to Firestore.
     * Navigates back on success, shows error toast on failure.
     */
    private void saveChanges() {
        String name = getText(eventNameInput);
        String description = getText(descriptionInput);
        String location = getText(locationInput);
        String capacityStr = getText(capacityInput);
        String startDate = getText(eventStartDateInput);
        String endDate = getText(eventEndDateInput);
        String regisStart = getText(regisStartDateInput);
        String regisEnd = getText(regisEndDateInput);
        String lotteryCriteria = BulletPointHelper.getPlainText(getText(lotteryCriteriaInput));

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Event name is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Description is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (location.isEmpty()) {
            Toast.makeText(requireContext(), "Location is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isInteger(capacityStr) || Integer.parseInt(capacityStr) < 1) {
            Toast.makeText(requireContext(), "Valid capacity is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("description", description);
        updates.put("location", location);
        updates.put("capacity", Integer.parseInt(capacityStr));
        updates.put("startingEventDate", startDate);
        updates.put("endingEventDate", endDate);
        updates.put("startingRegistrationPeriod", regisStart);
        updates.put("endingRegistrationPeriod", regisEnd);
        updates.put("geolocationRequired", geoSwitch.isChecked());
        updates.put("lotteryCriteria", lotteryCriteria);

        db.collection("events").document(eventId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (selectedImageUri != null) {
                        uploadPoster();
                    } else {
                        Toast.makeText(requireContext(), "Event updated", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigateUp();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to update event", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Uploads the selected poster image to Firebase Realtime Database.
     * Resizes and compresses the image before Base64 encoding.
     */
    private void uploadPoster() {
        new Thread(() -> {
            try {
                ContentResolver resolver = requireContext().getContentResolver();
                InputStream imageStream = resolver.openInputStream(selectedImageUri);
                Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);


                int maxDim = 512; // Resize to max 512px to reduce storage
                int width = imageBitmap.getWidth();
                int height = imageBitmap.getHeight();
                if (width > maxDim || height > maxDim) {
                    float scale = Math.min((float) maxDim / width, (float) maxDim / height);
                    imageBitmap = Bitmap.createScaledBitmap(
                            imageBitmap,
                            Math.round(width * scale),
                            Math.round(height * scale),
                            true
                    );
                }


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos); // Compress to JPEG 60% quality
                String base64Image = android.util.Base64.encodeToString(
                        baos.toByteArray(), android.util.Base64.DEFAULT);

                ImageCacheManager.put(eventId, imageBitmap);

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference reference = database.getReference("event_posters");

                HashMap<String, String> imageData = new HashMap<>();
                imageData.put("image", base64Image);
                imageData.put("type", "image/jpeg");

                reference.child(eventId).setValue(imageData)
                        .addOnSuccessListener(aVoid -> {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Event updated", Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(requireView()).navigateUp();
                            });
                        })
                        .addOnFailureListener(e -> {
                            if (!isAdded()) return;
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Event updated but poster upload failed", Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(requireView()).navigateUp();
                            });
                        });

            } catch (Exception e) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "Failed to process image", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigateUp();
                });
            }
        }).start();
    }

    /**
     * Displays a confirmation dialog before deleting the event.
     */
    private void showDeleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event? This action cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> deleteEvent())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Permanently deletes the event from Firestore and removes associated poster and QR code.
     * Navigates back on success.
     */
    private void deleteEvent() {
        if (disableFirestoreForTest) {
            Navigation.findNavController(requireView()).navigateUp();
            return;
        }

        FirebaseDatabase.getInstance().getReference("event_posters")
                .child(eventId)
                .removeValue()
                .addOnSuccessListener(aVoid -> {
                    db.collection("events").document(eventId)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(requireView()).navigateUp();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(requireContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    db.collection("events").document(eventId)
                            .delete()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                                Navigation.findNavController(requireView()).navigateUp();
                            })
                            .addOnFailureListener(err -> {
                                Toast.makeText(requireContext(), "Failed to delete event", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    /**
     * Safely extracts and trims text from a TextInputEditText.
     *
     * @param field the input field to read
     * @return trimmed text, or empty string if field is null
     */
    private String getText(TextInputEditText field) {
        return field.getText() != null ? field.getText().toString().trim() : "";
    }

    /**
     * Validates if a string can be parsed as an integer.
     *
     * @param str the string to validate
     * @return true if the string is a valid integer, false otherwise
     */
    private boolean isInteger(String str) {
        if (str == null || str.isBlank()) return false;
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Launcher for gallery image selection.
     * Updates the preview image and marks changes when an image is selected.
     */
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK
                                && result.getData() != null) {
                            selectedImageUri = result.getData().getData();
                            posterPreview.setImageURI(selectedImageUri);
                            if (uploadHint != null) uploadHint.setVisibility(View.GONE);
                            markChanges();
                        }
                    });
}