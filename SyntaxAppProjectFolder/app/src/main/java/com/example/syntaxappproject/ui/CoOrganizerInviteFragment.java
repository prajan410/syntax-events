package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.CoOrganizerRepository;
import com.example.syntaxappproject.EventDetailRepository;
import com.example.syntaxappproject.InviteEntrantAdapter;
import com.example.syntaxappproject.Notification;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

/**
 * Fragment displaying of invite entrant to be co-organizer.
 *
 * <p>a page for invite selected entrant to be co-organizer
 * organizer can select entrant in notify entrants page.
 * </p>
 */
public class CoOrganizerInviteFragment extends Fragment {

    private TextInputEditText searchInput;
    private MaterialButton searchButton;
    private MaterialButton doneButton;
    private TextView resultText;
    private RecyclerView inviteRecyclerView;

    private InviteEntrantAdapter adapter;
    private CoOrganizerRepository repo;

    private String eventId;
    private String eventName;
    private boolean isCoOrganizer;

    public CoOrganizerInviteFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_co_organizer_invite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchInput = view.findViewById(R.id.searchInput);
        searchButton = view.findViewById(R.id.searchButton);
        doneButton = view.findViewById(R.id.doneButton);
        resultText = view.findViewById(R.id.resultText);
        inviteRecyclerView = view.findViewById(R.id.inviteRecyclerView);

        if (repo == null) {
            repo = new CoOrganizerRepository();
        }

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }

        adapter = new InviteEntrantAdapter(
                new ArrayList<>(),
                new ArrayList<>(),
                (profile, userId) -> inviteUser(userId)
        );

        inviteRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        inviteRecyclerView.setAdapter(adapter);

        searchButton.setOnClickListener(v -> doSearch());

        doneButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.notifyEntrantsFragment)
        );
    }

    /**
     * Search for the entrant matched to the search text.
     */
    private void doSearch() {
        String query = "";
        if (searchInput.getText() != null) {
            query = searchInput.getText().toString().trim();
        }

        if (TextUtils.isEmpty(query)) {
            Toast.makeText(getContext(), "Enter a name, phone, or email", Toast.LENGTH_SHORT).show();
            return;
        }

        resultText.setText("Searching...");

        repo.searchEntrants(query, (profiles, userIds) -> {
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                adapter.updateData(profiles, userIds);

                if (profiles.isEmpty()) {
                    resultText.setText("No entrants found");
                } else {
                    resultText.setText("Found " + profiles.size() + " result(s)");
                }
            });
        });
    }

    /**
     * Send a notification and invitation to selected entrant.
     * @param userId the id of entrant
     */
    private void inviteUser(String userId) {

        new EventDetailRepository().getEventDetail(eventId, event -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                eventName = event.getName();
                if (eventId == null || eventName == null) {
                    Toast.makeText(getContext(), "Missing event info", Toast.LENGTH_SHORT).show();
                    return;
                }
                Notification notify = new Notification();
                notify.setEventId(eventId);
                notify.setSenderId(userId);
                notify.setEventName(eventName);
                notify.setTitle("Invite Co-organizer");
                notify.setSenderRole("ORGANIZER");
                notify.setTargetGroup("CO_ORGANIZER");
                notify.setBody("Invite you to be co-organizer of " + eventName);
                notify.setTimestamp(System.currentTimeMillis());
                notify.setStatus("SENT");

                repo.isCoOrganizer(eventId, userId, coOrganizer -> {
                    isCoOrganizer = coOrganizer;

                    if (isCoOrganizer) {
                        Toast.makeText(getContext(), "Already invite this entrant", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        repo.sendInvitation(eventId, eventName, userId, notify, success -> {
                            if (!isAdded()) return;

                            requireActivity().runOnUiThread(() -> {
                                if (!success) {
                                    Toast.makeText(getContext(), "Failed to send invitation", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                repo.addCoOrganizer(eventId, userId, added -> {
                                    if (!isAdded()) return;

                                    requireActivity().runOnUiThread(() -> {
                                        if (added) {
                                            Toast.makeText(getContext(), "Invitation sent", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getContext(), "Invitation sent but failed to update event", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                });
                            });
                        });
                    }
                });
            });
        });
    }
}

