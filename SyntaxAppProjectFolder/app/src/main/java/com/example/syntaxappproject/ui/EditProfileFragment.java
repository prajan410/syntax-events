package com.example.syntaxappproject.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.Profile;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Fragment that allows the currently authenticated user to view and edit
 * their profile information, including first name, last name, email, and
 * phone number.
 *
 * <p>Also provides the option to permanently delete the user's account,
 * which clears local preferences, signs the user out, and navigates to
 * the splash screen.</p>
 *
 * <p>Navigation is performed via {@link Navigation#findNavController(View)}
 * using the root view, which allows the NavController to be mocked in
 * instrumented tests via {@link Navigation#setViewNavController(View,
 * androidx.navigation.NavController)}.</p>
 *
 * <p>Extends {@link HomeBar} to inherit bottom navigation bar functionality.</p>
 */
public class EditProfileFragment extends HomeBar {

    /**
     * Repository for reading and writing {@link Profile} data to Firestore.
     * Package-private to allow injection in instrumented tests.
     */
    private final ProfileRepository profileRepo = new ProfileRepository();

    /**
     * Service for retrieving and managing the current user's authentication state.
     * Package-private to allow injection in instrumented tests.
     */
    private final AuthenticationService authService = new AuthenticationService();

    /** Input field for the user's first name. */
    private TextInputEditText editFirstName;

    /** Input field for the user's last name. */
    private TextInputEditText editLastName;

    /** Input field for the user's email address. */
    private TextInputEditText editEmail;

    /** Input field for the user's phone number. */
    private TextInputEditText editPhone;

    /**
     * Inflates the edit profile layout.
     *
     * @param inflater           the layout inflater used to inflate the view
     * @param container          the parent view group the fragment UI attaches to
     * @param savedInstanceState previously saved instance state, if any
     * @return the inflated root view for this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_edit, container, false);
    }

    /**
     * Initializes views, entrance animations, and button click listeners.
     * Also loads the current user's profile data into the form fields.
     *
     * <p>The bottom navigation hotbar setup is wrapped in a try-catch so that
     * tests running the fragment in isolation (without a NavHostFragment) do not
     * crash on hotbar initialization.</p>
     *
     * @param view               the root view returned by {@link #onCreateView}
     * @param savedInstanceState previously saved instance state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Safe hotbar setup for testing (no NavHostFragment in unit tests)
        try { setupHotbar(view); } catch (Exception ignored) {}

        // Cache view references for form fields
        editFirstName = view.findViewById(R.id.editFirstName);
        editLastName  = view.findViewById(R.id.editLastName);
        editEmail     = view.findViewById(R.id.editEmail);
        editPhone     = view.findViewById(R.id.editPhone);

        View headerTitle = view.findViewById(R.id.headerTitle);
        View nameCard    = view.findViewById(R.id.nameCard);
        View contactCard = view.findViewById(R.id.contactCard);
        View actionsCard = view.findViewById(R.id.actionsCard);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start();

        nameCard.setTranslationY(30f);
        nameCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(200).start();

        contactCard.setTranslationY(30f);
        contactCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(320).start();

        actionsCard.setTranslationY(30f);
        actionsCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(420).start();

        view.findViewById(R.id.saveEdit).setOnClickListener(v -> saveEdit());
        view.findViewById(R.id.deleteProfile).setOnClickListener(v -> showDeleteDialog());

        loadProfileToEdit();
    }

    /**
     * Loads the authenticated user's profile from Firestore and populates
     * the editable fields with their current name, email, and phone number.
     *
     * <p>If no authenticated user is found, displays a toast and navigates up.</p>
     * <p>The full name stored in Firestore is split on the first space into
     * first and last name fields.</p>
     */
    private void loadProfileToEdit() {
        String uid = authService.getCurrentUserId();
        if (uid == null) { // No active session - redirect to login
            Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
            return;
        }

        profileRepo.getProfile(uid, profile -> { // callback runs on background thread
            if (profile != null && isAdded()) { // Only update UI if fragment is still attached to activity
                requireActivity().runOnUiThread(() -> { // Switch to UI thread for view updates
                    String[] names = profile.getName() != null
                            ? profile.getName().split(" ", 2)
                            : new String[]{"", ""}; // Split full name on first space (handles "First Last" or just "First")
                    editFirstName.setText(names.length > 0 ? names[0] : "");
                    editLastName.setText(names.length > 1 ? names[1] : "");
                    editEmail.setText(profile.getEmail());
                    editPhone.setText(profile.getPhone() != null ? profile.getPhone() : "");
                });
            }
        });
    }

    /**
     * Validates the input fields and saves the updated profile to Firestore.
     *
     * <p>First name and email are required fields. If either is empty, a toast
     * is shown and the save is aborted. On a successful save, navigates back to
     * the previous screen. On failure, shows an error toast.</p>
     *
     * <p>The existing profile is fetched first to preserve role and notification
     * settings that are not editable on this screen.</p>
     */
    private void saveEdit() {
        String firstName = editFirstName.getText() != null ? editFirstName.getText().toString().trim() : "";
        String lastName  = editLastName.getText()  != null ? editLastName.getText().toString().trim()  : "";
        String email     = editEmail.getText()     != null ? editEmail.getText().toString().trim()     : "";
        String phone     = editPhone.getText()     != null ? editPhone.getText().toString().trim()     : "";

        if (firstName.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Name and Email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = authService.getCurrentUserId();
        profileRepo.getProfile(uid, existing -> {
            boolean isEntrant   = existing != null && existing.isEntrant();
            boolean isOrganizer = existing != null && existing.isOrganizer();
            boolean isAdmin     = existing != null && existing.isAdmin();
            boolean notifs      = existing != null && existing.isNotificationsEnabled();

            Profile updated = new Profile(
                    firstName + (lastName.isEmpty() ? "" : " " + lastName),
                    email,
                    phone.isEmpty() ? null : phone,
                    isEntrant,
                    isOrganizer,
                    isAdmin,
                    notifs,
                    uid
            );
            profileRepo.updateProfile(uid, updated, success -> {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(requireContext(), "Profile saved!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).navigateUp();
                    } else {
                        Toast.makeText(requireContext(), "Save failed", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }


    /**
     * Displays a confirmation {@link AlertDialog} warning the user that
     * deleting their account is permanent and cannot be undone.
     *
     * <p>Confirming the dialog triggers {@link #deleteProfile()}.
     * Cancelling dismisses the dialog with no action.</p>
     */
    private void showDeleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure? This permanently deletes your profile and logs you out.")
                .setPositiveButton("Delete", (d, w) -> deleteProfile())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Permanently deletes the user's profile from Firestore, clears all
     * locally stored shared preferences, signs the user out via
     * {@link AuthenticationService#signOut()}, and navigates to the splash screen.
     *
     * <p>On failure, displays an error toast and leaves the user on the current screen.</p>
     */
    private void deleteProfile() {
        String uid = authService.getCurrentUserId();
        profileRepo.deleteProfile(uid, success -> {
            if (!isAdded()) return; // Safety check - fragment might be destroyed during async operation
            requireActivity().runOnUiThread(() -> {
                if (success) {
                    // Clear all local user preferences
                    requireActivity().getSharedPreferences("UserPrefs", 0).edit().clear().apply();
                    authService.signOut();
                    Navigation.findNavController(requireView()).navigate(R.id.splashFragment);
                    Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
