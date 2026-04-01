package com.example.syntaxappproject.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.example.syntaxappproject.Notification;
import com.example.syntaxappproject.NotificationAdapter;
import com.example.syntaxappproject.NotificationRepository;
import com.example.syntaxappproject.EventJoinRepository;
import com.example.syntaxappproject.Invitation;
import com.example.syntaxappproject.InvitationRepository;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.materialswitch.MaterialSwitch;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final ProfileRepository profileRepository = new ProfileRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final InvitationRepository invitationRepository = new InvitationRepository();


    private MaterialSwitch organizerToggle;
    private MaterialSwitch adminToggle;
    private RecyclerView notificationsRecyclerView;
    private TextView newBadge;
    private View headerTitle;
    private View toggleCard;
    private View notificationsCard;

    private Invitation currentInvitation = null;
    private NotificationAdapter notificationAdapter;
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
        notificationAdapter = new NotificationAdapter();
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationsRecyclerView.setAdapter(notificationAdapter);

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

    /**
     * Reads toggle states from Firestore and applies them to the switches.
     * Only attaches listeners after state is set to avoid triggering
     * unwanted Firestore writes on load.
     */
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

    /**
     * Loads notifications for the current user from Firestore.
     * Filters by the user's opt-out preferences before displaying.
     * Updates the newBadge count based on how many were loaded.
     */
    /**
     * Loads notifications for the current user.
     * Filters by sender role opt-out preferences, then by
     * targetGroup membership (WAITLIST / SELECTED / CANCELLED / ALL).
     */
    private void loadNotifications(String userId) {
        Log.d("NotifDebug", "loadNotifications called for userId=" + userId);

        invitationRepository.getPendingInvitationForUser(userId, invitation -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                currentInvitation = invitation;
                Log.d("NotifDebug", "invitation=" + (invitation == null ? "null" : invitation.getInvitationId()));
                if (invitation != null) {
                    newBadge.setText("1 new");
                    newBadge.setVisibility(View.VISIBLE);
                }
            });
        });

        profileRepository.getProfile(userId, profile -> {
            if (!isAdded()) return;
            if (profile == null) {
                Log.d("NotifDebug", "profile is null, aborting");
                return;
            }

            boolean wantsOrganizer = profile.isOrganizerNotificationEnabled();
            boolean wantsAdmin     = profile.isAdminNotificationEnabled();
            Log.d("NotifDebug", "wantsOrganizer=" + wantsOrganizer + " wantsAdmin=" + wantsAdmin);

            notificationRepository.getNotificationsForUser("AEIggcqIZzAqNgfG7H46", notifications -> {
                if (!isAdded()) return;
                Log.d("NotifDebug", "notifications fetched: " + (notifications == null ? "null" : notifications.size()));

                if (notifications == null || notifications.isEmpty()) return;

                List<Notification> roleFiltered = new ArrayList<>();
                for (Notification n : notifications) {
                    Log.d("NotifDebug", "checking notif senderRole=" + n.getSenderRole() + " targetGroup=" + n.getTargetGroup());
                    if ("ORGANIZER".equals(n.getSenderRole()) && !wantsOrganizer) {
                        Log.d("NotifDebug", "filtered out by organizer toggle");
                        continue;
                    }
                    if ("ADMIN".equals(n.getSenderRole()) && !wantsAdmin) {
                        Log.d("NotifDebug", "filtered out by admin toggle");
                        continue;
                    }
                    roleFiltered.add(n);
                }

                Log.d("NotifDebug", "roleFiltered size=" + roleFiltered.size());
                filterAndDisplay(roleFiltered, userId);
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

    /**
     * Loads notifications for the current user.
     * Filters by sender role opt-out preferences, then by
     * targetGroup membership (WAITLIST / SELECTED / CANCELLED / ALL).
     */

    /**
     * Checks each notification against the user's actual group membership
     * before displaying. Uses AtomicInteger to wait for all async checks
     * to complete before rendering.
     */
    private void filterAndDisplay(List<Notification> notifications, String userId) {
        if (notifications.isEmpty()) {
            requireActivity().runOnUiThread(() -> {
                newBadge.setVisibility(View.GONE);
                notificationsCard.setVisibility(View.GONE);
            });
            return;
        }

        List<Notification> filtered = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger pending = new AtomicInteger(notifications.size());

        for (Notification notif : notifications) {
            checkMembership(notif, userId, belongs -> {
                if (belongs) filtered.add(notif);
                if (pending.decrementAndGet() == 0) {
                    requireActivity().runOnUiThread(() -> {
                        if (filtered.isEmpty()) {
                            newBadge.setVisibility(View.GONE);
                            notificationsCard.setVisibility(View.GONE);
                        } else {
                            newBadge.setText(filtered.size() + " new");
                            newBadge.setVisibility(View.VISIBLE);
                            notificationAdapter.setNotifications(filtered);
                            notificationsCard.setVisibility(View.VISIBLE);
                        }
                    });
                }
            });
        }
    }

    /**
     * Checks whether the current user belongs to the targetGroup
     * of the given notification by querying Firestore membership.
     *
     * WAITLIST  — in waitlist-entrants and not in invitedUserIds
     * SELECTED  — has a lottery_win invitation document
     * CANCELLED — has a lottery_loss invitation document
     * ALL       — always true
     */
    private void checkMembership(Notification notif, String uid, BooleanCallback callback) {
        String group   = notif.getTargetGroup();
        String eventId = notif.getEventId();
        Log.d("NotifDebug", "checkMembership group=" + group + " eventId=" + eventId + " uid=" + uid);

        switch (group) {
            case "ALL":
                Log.d("NotifDebug", "ALL — returning true");
                callback.onResult(true);
                break;

            case "WAITLIST":
                new EventJoinRepository().hasJoined(eventId, uid, joined -> {
                    Log.d("NotifDebug", "WAITLIST hasJoined=" + joined);
                    if (!joined) { callback.onResult(false); return; }
                    FirebaseFirestore.getInstance()
                            .collection("events").document(eventId)
                            .get()
                            .addOnSuccessListener(doc -> {
                                List<String> invited = (List<String>) doc.get("invitedUserIds");
                                boolean result = invited == null || !invited.contains(uid);
                                Log.d("NotifDebug", "WAITLIST not in invitedUserIds=" + result);
                                callback.onResult(result);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("NotifDebug", "WAITLIST event fetch failed: " + e.getMessage());
                                callback.onResult(false);
                            });
                });
                break;

            case "SELECTED":
                FirebaseFirestore.getInstance()
                        .collection("invitations")
                        .whereEqualTo("eventId", eventId)
                        .whereEqualTo("userId", uid)
                        .whereEqualTo("type", "lottery_win")
                        .get()
                        .addOnSuccessListener(snap -> {
                            Log.d("NotifDebug", "SELECTED found " + snap.size() + " docs");
                            callback.onResult(!snap.isEmpty());
                        })
                        .addOnFailureListener(e -> {
                            Log.e("NotifDebug", "SELECTED query failed: " + e.getMessage());
                            callback.onResult(false);
                        });
                break;

            case "CANCELLED":
                FirebaseFirestore.getInstance()
                        .collection("invitations")
                        .whereEqualTo("eventId", eventId)
                        .whereEqualTo("userId", uid)
                        .whereEqualTo("type", "lottery_loss")
                        .get()
                        .addOnSuccessListener(snap -> {
                            Log.d("NotifDebug", "CANCELLED found " + snap.size() + " docs");
                            callback.onResult(!snap.isEmpty());
                        })
                        .addOnFailureListener(e -> {
                            Log.e("NotifDebug", "CANCELLED query failed: " + e.getMessage());
                            callback.onResult(false);
                        });
                break;

            default:
                Log.d("NotifDebug", "unknown group=" + group + " returning false");
                callback.onResult(false);
        }
    }

    /**
     * Animates fade in of cards
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
    private interface BooleanCallback {
        void onResult(boolean result);
    }
}