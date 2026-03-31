package com.example.syntaxappproject.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
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
import com.example.syntaxappproject.EventJoinRepository;
import com.example.syntaxappproject.Invitation;
import com.example.syntaxappproject.InvitationRepository;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.materialswitch.MaterialSwitch;

public class NotificationFragment extends HomeBar {

    private final AuthenticationService authService = new AuthenticationService();
    private final InvitationRepository invitationRepository = new InvitationRepository();
    private final ProfileRepository profileRepository = new ProfileRepository();

    private MaterialSwitch organizerToggle;
    private MaterialSwitch adminToggle;
    private RecyclerView notificationsRecyclerView;
    private TextView newBadge;
    private View headerTitle;
    private View toggleCard;
    private View notificationsCard;

    private Invitation currentInvitation = null;

    public NotificationFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupHotbar(view);

        organizerToggle = view.findViewById(R.id.organizerToggle);
        adminToggle = view.findViewById(R.id.adminToggle);
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);
        newBadge = view.findViewById(R.id.newBadge);
        headerTitle = view.findViewById(R.id.headerTitle);
        toggleCard = view.findViewById(R.id.toggleCard);
        notificationsCard = view.findViewById(R.id.notificationsCard);

        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        String userId = authService.getCurrentUserId();
        if (userId != null) {
            loadToggleStates(userId);
            loadNotifications(userId);
        } else {
            newBadge.setVisibility(View.GONE);
        }

        notificationsCard.setOnClickListener(v -> {
            if (currentInvitation == null) {
                Toast.makeText(getContext(), "No pending invitations right now.", Toast.LENGTH_SHORT).show();
                return;
            }
            showInvitationDialog(userId);
        });

        animateIn();
    }

    private void loadToggleStates(String userId) {
        profileRepository.getProfile(userId, profile -> {
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    organizerToggle.setChecked(profile.isOrganizerNotificationEnabled());
                    adminToggle.setChecked(profile.isAdminNotificationEnabled());
                    setupToggleListeners(userId);
                });
            }
        });
    }

    private void setupToggleListeners(String userId) {
        organizerToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            profileRepository.getProfile(userId, profile -> {
                if (profile != null) {
                    profile.setOrganizerNotificationEnabled(isChecked);
                    profileRepository.updateProfile(userId, profile, success -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (!success) {
                                Toast.makeText(getContext(), "Failed to save preference", Toast.LENGTH_SHORT).show();
                                organizerToggle.setChecked(!isChecked);
                            }
                        });
                    });
                }
            });
        });

        adminToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            profileRepository.getProfile(userId, profile -> {
                if (profile != null) {
                    profile.setAdminNotificationEnabled(isChecked);
                    profileRepository.updateProfile(userId, profile, success -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (!success) {
                                Toast.makeText(getContext(), "Failed to save preference", Toast.LENGTH_SHORT).show();
                                adminToggle.setChecked(!isChecked);
                            }
                        });
                    });
                }
            });
        });
    }

    private void loadNotifications(String userId) {
        invitationRepository.getPendingInvitationForUser(userId, invitation -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                currentInvitation = invitation;
                if (invitation == null) {
                    newBadge.setVisibility(View.GONE);
                } else {
                    newBadge.setText("1 new");
                    newBadge.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private void showInvitationDialog(String userId) {
        if (currentInvitation == null || getContext() == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle(currentInvitation.getEventName())
                .setMessage("You have been invited to join the waiting list for this private event.")
                .setNegativeButton("Decline", (dialog, which) -> declineInvitation(userId))
                .setPositiveButton("Accept", (dialog, which) -> acceptInvitation(userId))
                .show();
    }

    private void acceptInvitation(String userId) {
        if (currentInvitation == null) return;

        EventJoinRepository joinRepo = new EventJoinRepository();
        String eventId = currentInvitation.getEventId();

        joinRepo.joinEvent(eventId, userId, joinSuccess -> {
            if (!isAdded()) return;

            if (!joinSuccess) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to join waitlist", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            invitationRepository.acceptInvitation(currentInvitation.getInvitationId(), success -> {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(), "Invitation accepted", Toast.LENGTH_SHORT).show();
                        loadNotifications(userId);
                    } else {
                        Toast.makeText(getContext(), "Joined waitlist but failed to update invitation", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    private void declineInvitation(String userId) {
        if (currentInvitation == null) return;

        invitationRepository.declineInvitation(currentInvitation.getInvitationId(), success -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                if (success) {
                    Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                    loadNotifications(userId);
                } else {
                    Toast.makeText(getContext(), "Failed to decline invitation", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

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