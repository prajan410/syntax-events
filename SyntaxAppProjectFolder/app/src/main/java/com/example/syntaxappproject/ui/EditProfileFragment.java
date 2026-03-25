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
 * <p>Extends {@link HomeBar} to inherit bottom navigation bar functionality.</p>
 */
public class EditProfileFragment extends HomeBar {

    private final ProfileRepository profileRepo = new ProfileRepository();
    private final AuthenticationService authService = new AuthenticationService();

    private TextInputEditText editFirstName;
    private TextInputEditText editLastName;
    private TextInputEditText editEmail;
    private TextInputEditText editPhone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try { setupHotbar(view); } catch (Exception ignored) {} // Safe hotbar setup for testing (no NavHostFragment in unit tests)

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
     * Loads the authenticated user's profile from Firestore and populates the form fields.
     * Splits the full name on the first space to separate first and last name.
     */
    private void loadProfileToEdit() {
        String uid = authService.getCurrentUserId();
        if (uid == null) {
            Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigateUp();
            return;
        }

        profileRepo.getProfile(uid, profile -> {
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    String[] names = profile.getName() != null
                            ? profile.getName().split(" ", 2)
                            : new String[]{"", ""};
                    editFirstName.setText(names.length > 0 ? names[0] : "");
                    editLastName.setText(names.length > 1 ? names[1] : "");
                    editEmail.setText(profile.getEmail());
                    editPhone.setText(profile.getPhone() != null ? profile.getPhone() : "");
                });
            }
        });
    }

    /**
     * Validates inputs and saves the updated profile to Firestore.
     * Fetches existing profile first to preserve role and notification settings.
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

    private void showDeleteDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure? This permanently deletes your profile and logs you out.")
                .setPositiveButton("Delete", (d, w) -> deleteProfile())
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Permanently deletes the user's profile, archives it to deleted-profiles collection,
     * clears local preferences, signs out, and navigates to splash screen.
     */
    private void deleteProfile() {
        String uid = authService.getCurrentUserId();

        profileRepo.getProfile(uid, profile -> {
            if (!isAdded()) return;

            java.util.Map<String, Object> archived = new java.util.HashMap<>();
            if (profile != null) {
                archived.put("name",                  profile.getName());
                archived.put("email",                 profile.getEmail());
                archived.put("phone",                 profile.getPhone());
                archived.put("isEntrant",             profile.isEntrant());
                archived.put("isOrganizer",           profile.isOrganizer());
                archived.put("isAdmin",               profile.isAdmin());
                archived.put("notificationsEnabled",  profile.isNotificationsEnabled());
                archived.put("deviceId",              profile.getDeviceId());
            }
            archived.put("uid",       uid);
            archived.put("deletedAt", com.google.firebase.Timestamp.now());

            com.google.firebase.firestore.FirebaseFirestore db =
                    com.google.firebase.firestore.FirebaseFirestore.getInstance();

            db.collection("deleted-profiles")
                    .add(archived)
                    .addOnSuccessListener(unused ->
                            profileRepo.deleteProfile(uid, success -> {
                                if (!isAdded()) return;
                                requireActivity().runOnUiThread(() -> {
                                    if (success) {
                                        requireActivity().getSharedPreferences("UserPrefs", 0)
                                                .edit().clear().apply();
                                        authService.signOut();
                                        Navigation.findNavController(requireView())
                                                .navigate(R.id.splashFragment);
                                        Toast.makeText(requireContext(),
                                                "Account deleted", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(requireContext(),
                                                "Delete failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            })
                    )
                    .addOnFailureListener(e -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(),
                                        "Failed to archive profile", Toast.LENGTH_SHORT).show()
                        );
                    });
        });
    }
}