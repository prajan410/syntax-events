package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.Profile;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.chip.Chip;

/**
 * Fragment displaying the currently authenticated user's profile summary.
 *
 * <p>Shows name, email, phone, and avatar initial. Provides quick access to
 * profile editing and event history screens via action buttons.</p>
 */
public class UserFragment extends HomeBar {

    /** Repository for loading user profile from Firestore. */
    private ProfileRepository profileRepo = new ProfileRepository();

    /** Service for retrieving current user authentication state. */
    private AuthenticationService authService = new AuthenticationService();

    /** UI elements for displaying profile data. */
    private TextView nameText, emailText, phoneText, emailDetailText, avatarInitial;
    private Chip badgeEntrant, badgeOrganizer, badgeAdmin;

    /**
     * Inflates user profile layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    /**
     * Setter for dependency injection (unit testing).
     */
    public void setProfileRepo(ProfileRepository profileRepo) {
        this.profileRepo = profileRepo;
    }

    /**
     * Setter for dependency injection (unit testing).
     */
    public void setAuthService(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Initializes views, animations, navigation buttons, and loads profile data.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupHotbar(view);

        nameText        = view.findViewById(R.id.profileName);
        emailText       = view.findViewById(R.id.profileEmail);
        phoneText       = view.findViewById(R.id.profilePhone);
        emailDetailText = view.findViewById(R.id.profileEmailDetail);
        avatarInitial   = view.findViewById(R.id.avatarInitial);

        View headerTitle  = view.findViewById(R.id.headerTitle);
        View avatarCard   = view.findViewById(R.id.heroCard);
        View detailsCard  = view.findViewById(R.id.detailsCard);
        View actionsCard  = view.findViewById(R.id.actionsCard);

        badgeEntrant   = view.findViewById(R.id.badgeEntrant);
        badgeOrganizer = view.findViewById(R.id.badgeOrganizer);
        badgeAdmin     = view.findViewById(R.id.badgeAdmin);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        avatarCard.setTranslationY(30f);
        avatarCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(200).start();

        detailsCard.setTranslationY(30f);
        detailsCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(320).start();

        actionsCard.setTranslationY(30f);
        actionsCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(420).start();

        view.findViewById(R.id.personalizationButton).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.editProfileFragment)
        );

        view.findViewById(R.id.eventHistoryButton).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.eventHistoryFragment)
        );

        loadProfile();
    }

    /**
     * Fetches current user's profile from Firestore.
     */
    private void loadProfile() {
        String uid = authService.getCurrentUserId();

        if (uid == null) {
            Toast.makeText(requireContext(), "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        profileRepo.getProfile(uid, this::displayProfile);
    }

    /**
     * Updates UI with profile data, handling null fields gracefully.
     */
    private void displayProfile(Profile profile) {
        if (profile == null || !isAdded()) return;

        requireActivity().runOnUiThread(() -> {
            String name  = profile.getName()  != null ? profile.getName()  : "No name set";
            String email = profile.getEmail() != null ? profile.getEmail() : "No email set";
            String phone = profile.getPhone() != null ? profile.getPhone() : "No phone set";

            badgeEntrant.setVisibility(profile.isEntrant()   ? View.VISIBLE : View.GONE);
            badgeOrganizer.setVisibility(profile.isOrganizer() ? View.VISIBLE : View.GONE);
            badgeAdmin.setVisibility(profile.isAdmin()       ? View.VISIBLE : View.GONE);


            nameText.setText(name);
            emailText.setText(email);
            emailDetailText.setText(email);
            phoneText.setText(phone);

            if (profile.getName() != null && !profile.getName().isEmpty()) {
                avatarInitial.setText(
                        String.valueOf(profile.getName().charAt(0)).toUpperCase()
                );
            }
        });
    }
}
