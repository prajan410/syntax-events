package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.Profile;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Fragment for initial user profile setup during onboarding.
 * <p>
 * Allows the user to select roles (Entrant, Organizer, or both), enter personal
 * information, and create or update their profile in Firestore. On successful save,
 * the fragment navigates to either the home screen or the user profile fragment,
 * depending on whether this is a new profile or an update.
 * </p>
 */
public class ProfileSetupFragment extends Fragment {

    /** Whether the user has selected the entrant role. */
    private boolean isEntrant = true;

    /** Whether the user has selected the organizer role. */
    private boolean isOrganizer = false;

    /** Button to select or deselect the entrant role. */
    private MaterialButton entrantButton;

    /** Button to select or deselect the organizer role. */
    private MaterialButton organizerButton;

    /** Repository for profile creation, update, and retrieval. */
    private ProfileRepository profileRepo;

    /** Service for user authentication during setup. */
    private AuthenticationService authService;

    /**
     * Inflates the profile setup layout.
     *
     * @param inflater  the layout inflater
     * @param container the parent view group
     * @param savedInstanceState previously saved state, or {@code null}
     * @return the inflated view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_setup, container, false);
    }

    /**
     * Binds views, runs entrance animations, sets up role toggles,
     * and configures the confirm button handler.
     *
     * @param view               the root view returned by {@link #onCreateView}
     * @param savedInstanceState previously saved state, or {@code null}
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        entrantButton   = view.findViewById(R.id.entrantButton);
        organizerButton = view.findViewById(R.id.organizerButton);

        TextInputEditText firstName = view.findViewById(R.id.firstNameInput);
        TextInputEditText lastName  = view.findViewById(R.id.lastNameInput);
        TextInputEditText email     = view.findViewById(R.id.emailInput);
        TextInputEditText phone     = view.findViewById(R.id.phoneInput);

        View headerTitle   = view.findViewById(R.id.headerTitle);
        View headerSub     = view.findViewById(R.id.headerSubtitle);
        View roleCard      = view.findViewById(R.id.roleCard);
        View nameCard      = view.findViewById(R.id.nameCard);
        View contactCard   = view.findViewById(R.id.contactCard);
        View confirmCard   = view.findViewById(R.id.confirmCard);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        headerSub.animate().alpha(1f)
                .setDuration(300).setStartDelay(200).start();

        roleCard.setTranslationY(30f);
        roleCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(250).start();

        nameCard.setTranslationY(30f);
        nameCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(350).start();

        contactCard.setTranslationY(30f);
        contactCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(440).start();

        confirmCard.setTranslationY(30f);
        confirmCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(520).start();

        updateRoleButtons();

        entrantButton.setOnClickListener(v -> {
            isEntrant = !isEntrant;
            updateRoleButtons();
        });

        organizerButton.setOnClickListener(v -> {
            isOrganizer = !isOrganizer;
            updateRoleButtons();
        });

        view.findViewById(R.id.confirmButton).setOnClickListener(
                v -> confirmProfile(firstName, lastName, email, phone));
    }

    /**
     * Validates user input and initiates the profile save workflow.
     *
     * <p>Requires at least one role (entrant or organizer) and non‑empty name and email.
     * If the profile already exists, it is updated; otherwise a new profile is created.</p>
     *
     * @param firstName the first name input field
     * @param lastName  the last name input field
     * @param email     the email input field
     * @param phone     the phone number input field
     */
    private void confirmProfile(TextInputEditText firstName, TextInputEditText lastName,
                                TextInputEditText email, TextInputEditText phone) {

        if (!isEntrant && !isOrganizer) {
            Toast.makeText(requireContext(), "Please select at least one role", Toast.LENGTH_SHORT).show();
            return;
        }

        String firstNameVal = firstName.getText() != null ? firstName.getText().toString().trim() : "";
        String lastNameVal  = lastName.getText()  != null ? lastName.getText().toString().trim()  : "";
        String emailVal     = email.getText()     != null ? email.getText().toString().trim()     : "";
        String phoneVal     = phone.getText()     != null ? phone.getText().toString().trim()     : "";

        if (firstNameVal.isEmpty() || emailVal.isEmpty()) {
            Toast.makeText(requireContext(), "Name and Email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (authService == null) authService = new AuthenticationService();
        if (profileRepo == null) profileRepo = new ProfileRepository();

        authService.signInAnonymously(success -> {
            if (!success) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show());
                return;
            }

            String uid = authService.getCurrentUserId();
            String fullName = firstNameVal + (lastNameVal.isEmpty() ? "" : " " + lastNameVal);

            Profile profile = new Profile(
                    fullName,
                    emailVal,
                    phoneVal.isEmpty() ? null : phoneVal,
                    isEntrant,
                    isOrganizer,
                    false,
                    true,
                    uid
            );

            profileRepo.getProfile(uid, existing -> {
                if (existing != null) {
                    profileRepo.updateProfile(uid, profile, saved -> handleSaveResult(saved, R.id.userFragment));
                } else {
                    profileRepo.createProfile(uid, profile, saved -> {
                        if (saved) {
                            requireActivity()
                                    .getSharedPreferences("UserPrefs", 0)
                                    .edit()
                                    .putBoolean("isLoggedIn", true)
                                    .apply();
                        }
                        handleSaveResult(saved, R.id.action_profile_to_home);
                    });
                }
            });
        });
    }

    /**
     * Handles the result of a profile save operation.
     *
     * <p>On success, navigates to the destination specified by {@code navAction} and
     * shows a confirmation toast. On failure, shows an error toast without navigation.</p>
     *
     * @param saved     whether the save operation was successful
     * @param navAction the navigation action ID to use on success
     */
    private void handleSaveResult(boolean saved, int navAction) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            if (saved) {
                NavHostFragment.findNavController(this).navigate(navAction);
                Toast.makeText(requireContext(), "Profile saved!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the visual state of the role selection buttons according to
     * the current role flags ({@link #isEntrant} and {@link #isOrganizer}).
     */
    private void updateRoleButtons() {
        entrantButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor(isEntrant ? "#2ECC71" : "#F0F0F0")));
        entrantButton.setTextColor(
                android.graphics.Color.parseColor(isEntrant ? "#FFFFFF" : "#1A1A1A"));

        organizerButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor(isOrganizer ? "#2ECC71" : "#F0F0F0")));
        organizerButton.setTextColor(
                android.graphics.Color.parseColor(isOrganizer ? "#FFFFFF" : "#1A1A1A"));
    }
}
