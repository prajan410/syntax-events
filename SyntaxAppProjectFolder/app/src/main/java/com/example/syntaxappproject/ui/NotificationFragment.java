package com.example.syntaxappproject.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.InvitationRepository;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class NotificationFragment extends HomeBar {

    // --- Unchanged ---
    private final AuthenticationService authService = new AuthenticationService();
    private final InvitationRepository invitationRepository = new InvitationRepository();

    // --- Removed (no longer in XML) ---
    // private TextView currentUserText;
    // private TextView eventNameText;
    // private TextView messageText;
    // private TextView statusText;
    // private View invitationCard;
    // private Button acceptButton;
    // private Button declineButton;
    // private TextView emptyText;

    // --- New views from XML ---
    private MaterialSwitch organizerToggle;
    private MaterialSwitch adminToggle;
    private RecyclerView notificationsRecyclerView;
    private TextView newBadge;
    private View headerTitle;
    private View toggleCard;
    private View notificationsCard;

    public NotificationFragment() {}
    private ProfileRepository profileRepository = new ProfileRepository();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // unchanged
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupHotbar(view); // unchanged

        // --- New: bind views ---
        organizerToggle = view.findViewById(R.id.organizerToggle);
        adminToggle = view.findViewById(R.id.adminToggle);
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);
        newBadge = view.findViewById(R.id.newBadge);
        headerTitle = view.findViewById(R.id.headerTitle);
        toggleCard = view.findViewById(R.id.toggleCard);
        notificationsCard = view.findViewById(R.id.notificationsCard);

        // --- New: RecyclerView setup ---
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // TODO: set your adapter here once built
        // notificationsRecyclerView.setAdapter(new NotificationAdapter(...));

        // --- New: load and apply toggle states from Firestore ---
        String userId = authService.getCurrentUserId();
        if (userId != null) {
            loadToggleStates(userId);
            loadNotifications(userId);
        }

        // --- New: save toggle state back to Firestore on change ---


        // --- New: fade-in animation matching profile fragment style ---
        animateIn();
    }

    // --- New ---
    private void loadToggleStates(String userId) {
        profileRepository.getProfile(userId, profile -> {
            if (profile != null) {
                requireActivity().runOnUiThread(() -> {
                    organizerToggle.setChecked(profile.isOrganizerNotificationEnabled());
                    adminToggle.setChecked(profile.isAdminNotificationEnabled());
                    setupToggleListeners(userId); // attach listeners only after state is set
                });
            }
        });
    }
    private void setupToggleListeners(String userId){
        organizerToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (userId != null) {
                profileRepository.getProfile(userId, profile -> {
                    if (profile != null) {
                        profile.setOrganizerNotificationEnabled(isChecked); //updates profile object
                        profileRepository.updateProfile(userId, profile, success -> { //updates profile in firebase.
                            requireActivity().runOnUiThread(() -> {
                                if (!success) { //Warns when failed
                                    Toast.makeText(getContext(), "Failed to save preference", Toast.LENGTH_SHORT).show();
                                    organizerToggle.setChecked(!isChecked);
                                }
                            });
                        });
                    }
                });
            }
        });

        adminToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (userId != null) {
                profileRepository.getProfile(userId, profile -> {
                    if (profile != null) {
                        profile.setAdminNotificationEnabled(isChecked);
                        profileRepository.updateProfile(userId, profile, success -> {
                            requireActivity().runOnUiThread(() -> {
                                if (!success) {
                                    Toast.makeText(getContext(), "Failed to save preference", Toast.LENGTH_SHORT).show();
                                    adminToggle.setChecked(!isChecked);
                                }
                            });
                        });
                    }
                });
            }
        });
    }

    // --- New: replaces loadPendingInvitation — now loads into RecyclerView ---
    private void loadNotifications(String userId) {
        invitationRepository.getPendingInvitationForUser(userId, invitation ->
                requireActivity().runOnUiThread(() -> {
                    if (invitation == null) {
                        newBadge.setVisibility(View.GONE);
                        return;
                    }
                    // TODO: pass list to adapter and update newBadge count
                    newBadge.setText("1 new");
                    newBadge.setVisibility(View.VISIBLE);
                })
        );
    }

    /**
     * Function to animate fading in of cards.
     *
     */
    private void animateIn() {
        View[] views = {headerTitle, toggleCard, notificationsCard};
        AnimatorSet set = new AnimatorSet();
        android.animation.Animator[] animators = new android.animation.Animator[views.length];
        for (int i = 0; i < views.length; i++) {
            ObjectAnimator fade = ObjectAnimator.ofFloat(views[i], "alpha", 0f, 1f);
            fade.setStartDelay(i * 80L);
            fade.setDuration(300);
            animators[i] = fade;
        }
        set.playTogether(animators);
        set.start();
    }
}