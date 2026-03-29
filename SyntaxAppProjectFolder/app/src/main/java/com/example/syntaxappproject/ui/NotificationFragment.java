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


        String userId = authService.getCurrentUserId();

        if (userId == null) {
            currentUserText.setText("Current user id: no signed-in user");
            showEmptyState("No signed-in user found.");
            return;
        } else {
            currentUserText.setText("Current user id: " + userId);
        }

        loadPendingInvitation(userId);

        acceptButton.setOnClickListener(v -> {
            if (currentInvitation == null) {
                Toast.makeText(getContext(), "No invitation to accept", Toast.LENGTH_SHORT).show();
                return;
            }

            invitationRepository.acceptInvitation(currentInvitation.getInvitationId(), success ->
                    requireActivity().runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(getContext(), "Invitation accepted", Toast.LENGTH_SHORT).show();
                            loadPendingInvitation(userId);
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

                    invitationCard.setVisibility(View.VISIBLE);
                    emptyText.setVisibility(View.GONE);

                    eventNameText.setText(invitation.getEventName());
                    messageText.setText("Congratulations! You have been selected to participate.");
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