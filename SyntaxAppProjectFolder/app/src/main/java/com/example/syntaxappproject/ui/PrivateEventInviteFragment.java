package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.syntaxappproject.InviteEntrantAdapter;
import com.example.syntaxappproject.PrivateEventInvitationRepository;
import com.example.syntaxappproject.Profile;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class PrivateEventInviteFragment extends Fragment {

    private TextInputEditText searchInput;
    private MaterialButton searchButton;
    private MaterialButton doneButton;
    private TextView resultText;
    private RecyclerView inviteRecyclerView;

    private InviteEntrantAdapter adapter;
    private PrivateEventInvitationRepository repository;

    private String eventId;
    private String eventName;

    public PrivateEventInviteFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_private_event_invite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchInput = view.findViewById(R.id.searchInput);
        searchButton = view.findViewById(R.id.searchButton);
        doneButton = view.findViewById(R.id.doneButton);
        resultText = view.findViewById(R.id.resultText);
        inviteRecyclerView = view.findViewById(R.id.inviteRecyclerView);

        repository = new PrivateEventInvitationRepository();

        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            eventName = getArguments().getString("eventName");
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
                NavHostFragment.findNavController(this).navigate(R.id.organizerEventsFragment)
        );
    }

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

        repository.searchEntrants(query, (profiles, userIds) -> {
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

    private void inviteUser(String userId) {
        if (eventId == null || eventName == null) {
            Toast.makeText(getContext(), "Missing event info", Toast.LENGTH_SHORT).show();
            return;
        }

        repository.sendInvitation(eventId, eventName, userId, success -> {
            if (!isAdded()) return;

            requireActivity().runOnUiThread(() -> {
                if (!success) {
                    Toast.makeText(getContext(), "Failed to send invitation", Toast.LENGTH_SHORT).show();
                    return;
                }

                repository.addInvitedUserToEvent(eventId, userId, added -> {
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
}