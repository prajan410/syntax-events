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
 * Allows the user to select roles (Entrant, Organizer, or both), enter personal
 * information, and create or update their profile in Firestore.
 */
public class ProfileSetupFragment extends Fragment {

    private boolean isEntrant = true;
    private boolean isOrganizer = false;
    private MaterialButton entrantButton;
    private MaterialButton organizerButton;
    private ProfileRepository profileRepo;
    private AuthenticationService authService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile_setup, container, false);
    }

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
     * Checks for at least one role, non-empty name/email, and valid email format.
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

        if (firstNameVal.isEmpty()) {
            Toast.makeText(requireContext(), "First name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (emailVal.isEmpty()) {
            Toast.makeText(requireContext(), "Email is required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailVal).matches()) {
            Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show();
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

            String uid      = authService.getCurrentUserId();
            String fullName = firstNameVal + (lastNameVal.isEmpty() ? "" : " " + lastNameVal);

            Profile profile = new Profile(
                    fullName,
                    emailVal,
                    phoneVal.isEmpty() ? null : phoneVal,
                    isEntrant,
                    isOrganizer,
                    false,
                    true,
                    true,
                    uid
            );

            profileRepo.getProfile(uid, existing -> {
                if (existing != null) {
                    profileRepo.updateProfile(uid, profile,
                            saved -> handleSaveResult(saved, R.id.userFragment));
                } else {
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("profiles")
                            .whereEqualTo("email", emailVal)
                            .get()
                            .addOnSuccessListener(query -> {
                                if (!query.isEmpty()) {
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(requireContext(),
                                                    "An account with this email already exists",
                                                    Toast.LENGTH_SHORT).show());
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
                            })
                            .addOnFailureListener(e ->
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(requireContext(),
                                                    "Could not verify email, please try again",
                                                    Toast.LENGTH_SHORT).show()));
                }
            });
        });
    }

    /**
     * Handles the result of a profile save operation.
     * Navigates to the specified destination on success, shows error toast on failure.
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
     * Updates the visual state of the role selection buttons.
     * Green background when selected, light gray when deselected.
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