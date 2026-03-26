package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.EventJoinRepository;
import com.example.syntaxappproject.Invitation;
import com.example.syntaxappproject.InvitationRepository;
import com.example.syntaxappproject.R;

/**
 * Fragment that displays entrant invitations and lets the user
 * accept or decline them.
 * Outstanding issues:
 * Currently shows one pending invitation for demo simplicity
 * Invitation creation is handled separately
 **/
public class NotificationFragment extends HomeBar {

    // Auth gives us the current Firebase user id
    private final AuthenticationService authService = new AuthenticationService();

    // Repository handles Firestore read/update logic
    private final InvitationRepository invitationRepository = new InvitationRepository();

    // Keep the currently displayed invitation in memory
    private Invitation currentInvitation = null;

    // UI references
    private TextView currentUserText;
    private TextView eventNameText;
    private TextView messageText;
    private TextView statusText;
    private TextView emptyText;
    private View invitationCard;
    private Button acceptButton;
    private Button declineButton;

    public NotificationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Keep the hotbar working exactly like the other entrant screens
        setupHotbar(view);

        // Find all views
        currentUserText = view.findViewById(R.id.currentUserText);
        eventNameText = view.findViewById(R.id.invitationEventName);
        messageText = view.findViewById(R.id.invitationMessage);
        statusText = view.findViewById(R.id.invitationStatusText);
        emptyText = view.findViewById(R.id.emptyNotificationText);
        invitationCard = view.findViewById(R.id.invitationCard);
        acceptButton = view.findViewById(R.id.acceptInvitationButton);
        declineButton = view.findViewById(R.id.declineInvitationButton);

        String userId = authService.getCurrentUserId();

        // Show the current signed-in user id so your teammate can easily
        // create a matching invitation document in Firebase for demo purposes
        if (userId == null) {
            currentUserText.setText("Current user id: no signed-in user");
            showEmptyState("No signed-in user found.");
            return;
        } else {
            currentUserText.setText("Current user id: " + userId);
        }

        // Load one pending invitation for this user
        loadPendingInvitation(userId);

        // Accept button logic
        acceptButton.setOnClickListener(v -> {
            if (currentInvitation == null) {
                Toast.makeText(getContext(), "No invitation to accept", Toast.LENGTH_SHORT).show();
                return;
            }

            String eventId = currentInvitation.getEventId();
            EventJoinRepository joinRepo = new EventJoinRepository();

            joinRepo.joinEvent(eventId, userId, joinSuccess -> {
                if (!isAdded()) return;

                if (!joinSuccess) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Failed to join waitlist", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                invitationRepository.acceptInvitation(currentInvitation.getInvitationId(), success ->
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(getContext(), "Invitation accepted", Toast.LENGTH_SHORT).show();
                                loadPendingInvitation(userId);
                            } else {
                                Toast.makeText(getContext(), "Joined waitlist but failed to update invitation", Toast.LENGTH_SHORT).show();
                            }
                        })
                );
            });
        });

        // Decline button logic
        declineButton.setOnClickListener(v -> {
            if (currentInvitation == null) {
                Toast.makeText(getContext(), "No invitation to decline", Toast.LENGTH_SHORT).show();
                return;
            }

            invitationRepository.declineInvitation(currentInvitation.getInvitationId(), success ->
                    requireActivity().runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                            loadPendingInvitation(userId);
                        } else {
                            Toast.makeText(getContext(), "Failed to decline invitation", Toast.LENGTH_SHORT).show();
                        }
                    })
            );
        });
    }

    /**
     * Loads one pending invitation for the current user.
     **/
    private void loadPendingInvitation(String userId) {
        invitationRepository.getPendingInvitationForUser(userId, invitation ->
                requireActivity().runOnUiThread(() -> {
                    currentInvitation = invitation;

                    if (invitation == null) {
                        showEmptyState("No pending invitations right now.");
                        return;
                    }

                    // Show the invitation details
                    invitationCard.setVisibility(View.VISIBLE);
                    emptyText.setVisibility(View.GONE);

                    eventNameText.setText(invitation.getEventName());
                    messageText.setText("You have been invited to join the waiting list for this private event.");
                    statusText.setText("Status: " + invitation.getStatus());
                })
        );
    }

    /**
     * Shows the empty state and hides the invitation card.
     **/
    private void showEmptyState(String message) {
        invitationCard.setVisibility(View.GONE);
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(message);
    }
}