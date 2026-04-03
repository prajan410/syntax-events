package com.example.syntaxappproject.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.Invitation;
import com.example.syntaxappproject.InvitationRepository;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.materialswitch.MaterialSwitch;

/**
 * Fragment that displays entrant invitation notifications and lets the user
 * accept or decline a pending invitation.
 * <p>
 * This screen now supports both winner notifications and not-chosen notifications.
 * It also supports organizer and admin notification preference toggles,
 * a RecyclerView area for notification display, and a badge for new items.
 * </p>
 *
 * <p>Outstanding issues: currently shows only one latest relevant notification
 * at a time for the entrant, the RecyclerView adapter is not yet attached,
 * and the new badge count is currently limited to a single pending invitation.</p>
 */
public class NotificationFragment extends HomeBar {

    private final AuthenticationService authService = new AuthenticationService();
    private final InvitationRepository invitationRepository = new InvitationRepository();

    private Invitation currentInvitation = null;

    private TextView currentUserText;
    private TextView eventNameText;
    private TextView messageText;
    private TextView statusText;
    private TextView emptyText;
    private View invitationCard;
    private Button acceptButton;
    private Button declineButton;

    private MaterialSwitch organizerToggle;
    private MaterialSwitch adminToggle;
    private RecyclerView notificationsRecyclerView;
    private TextView newBadge;
    private View headerTitle;
    private View toggleCard;
    private View notificationsCard;

    private ProfileRepository profileRepository = new ProfileRepository();

    public NotificationFragment() {
    }

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
        // TODO: set your adapter here once built
        // notificationsRecyclerView.setAdapter(new NotificationAdapter(...));

        String userId = authService.getCurrentUserId();

        if (userId == null) {
            currentUserText.setText("Current user id: no signed-in user");
            showEmptyState("No signed-in user found.");
            return;
        } else {
            currentUserText.setText("Current user id: " + userId);
        }

        loadLatestNotification(userId);
        loadToggleStates(userId);
        loadNotifications(userId);

        acceptButton.setOnClickListener(v -> {
            if (currentInvitation == null) {
                Toast.makeText(getContext(), "No invitation to accept", Toast.LENGTH_SHORT).show();
                return;
            }

            invitationRepository.acceptInvitation(currentInvitation.getInvitationId(), success ->
                    requireActivity().runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(getContext(), "Invitation accepted", Toast.LENGTH_SHORT).show();
                            loadLatestNotification(userId);
                            loadNotifications(userId);
                        } else {
                            Toast.makeText(getContext(), "Failed to accept invitation", Toast.LENGTH_SHORT).show();
                        }
                    })
            );
        });

        declineButton.setOnClickListener(v -> {
            if (currentInvitation == null) {
                Toast.makeText(getContext(), "No invitation to decline", Toast.LENGTH_SHORT).show();
                return;
            }

            invitationRepository.declineInvitation(
                    currentInvitation.getInvitationId(),
                    currentInvitation.getEventId(),
                    userId,
                    success -> requireActivity().runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                            loadLatestNotification(userId);
                            loadNotifications(userId);
                        } else {
                            Toast.makeText(getContext(), "Failed to decline invitation", Toast.LENGTH_SHORT).show();
                        }
                    })
            );
        });

        animateIn();
    }

    /**
     * Loads one latest relevant notification for the current user.
     *
     * @param userId the signed-in user id
     */
    private void loadLatestNotification(String userId) {
        invitationRepository.getLatestRelevantInvitationForUser(userId, invitation ->
                requireActivity().runOnUiThread(() -> {
                    currentInvitation = invitation;

                    if (invitation == null) {
                        showEmptyState("No notifications right now.");
                        return;
                    }

                    invitationCard.setVisibility(View.VISIBLE);
                    emptyText.setVisibility(View.GONE);

                    eventNameText.setText(invitation.getEventName());

                    if ("pending".equals(invitation.getStatus())) {
                        messageText.setText("Congratulations! You have been selected to participate.");
                        statusText.setText("Status: pending");
                        acceptButton.setVisibility(View.VISIBLE);
                        declineButton.setVisibility(View.VISIBLE);
                    } else if ("not_chosen".equals(invitation.getStatus())) {
                        messageText.setText("You were not chosen this time. If another selected entrant declines, you may still get another chance from the waiting list.");
                        statusText.setText("Status: not chosen");
                        acceptButton.setVisibility(View.GONE);
                        declineButton.setVisibility(View.GONE);
                    } else {
                        showEmptyState("No notifications right now.");
                    }
                })
        );
    }

    /**
     * Shows the empty state and hides the invitation card.
     *
     * @param message the empty state message to display
     */
    private void showEmptyState(String message) {
        invitationCard.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(message);
    }

    /**
     * Loads organizer and admin notification preference toggle states
     * from the user's profile in Firestore.
     *
     * @param userId the signed-in user id
     */
    private void loadToggleStates(String userId) {
        profileRepository.getProfile(userId, profile -> {
            if (profile != null) {
                requireActivity().runOnUiThread(() -> {
                    organizerToggle.setChecked(profile.isOrganizerNotificationEnabled());
                    adminToggle.setChecked(profile.isAdminNotificationEnabled());
                    setupToggleListeners(userId);
                });
            }
        });
    }

    /**
     * Attaches listeners to organizer and admin notification toggles
     * and writes updated preference values back to Firestore.
     *
     * @param userId the signed-in user id
     */
    private void setupToggleListeners(String userId){
        organizerToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (userId != null) {
                profileRepository.getProfile(userId, profile -> {
                    if (profile != null) {
                        profile.setOrganizerNotificationEnabled(isChecked);
                        profileRepository.updateProfile(userId, profile, success -> {
                            requireActivity().runOnUiThread(() -> {
                                if (!success) {
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

    /**
     * Loads notification badge state for the current user by checking
     * for a pending invitation.
     *
     * @param userId the signed-in user id
     */
    private void loadNotifications(String userId) {
        invitationRepository.getPendingInvitationForUser(userId, invitation ->
                requireActivity().runOnUiThread(() -> {
                    if (invitation == null) {
                        newBadge.setVisibility(View.GONE);
                        return;
                    }
                    newBadge.setText("1 new");
                    newBadge.setVisibility(View.VISIBLE);
                })
        );
    }

    /**
     * Function to animate fading in of cards.
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