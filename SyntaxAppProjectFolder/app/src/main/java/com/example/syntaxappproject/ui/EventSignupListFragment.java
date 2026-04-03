package com.example.syntaxappproject.ui;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.CSVService;
import com.example.syntaxappproject.EntrantNameAdapter;
import com.example.syntaxappproject.Profile;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventSignupListFragment extends HomeBar {
    private String eventId;
    private FirebaseFirestore db;
    private ProfileRepository profileRepo;

    private EntrantNameAdapter finalAdapter;
    private EntrantNameAdapter invitedAdapter;
    private EntrantNameAdapter waitingAdapter;
    private EntrantNameAdapter cancelledAdapter;

    private boolean displayFinalEntrants = false;
    private boolean displayInvitedEntrants = false;
    private boolean displayWaitingList = false;
    private boolean displayCancelledEntrants = false;
    private List<Pair<String, String>> finalEntrants = new ArrayList<>(); // name, email

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        profileRepo = new ProfileRepository();
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_signup_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupHotbar(view);

        finalAdapter = new EntrantNameAdapter();
        invitedAdapter = new EntrantNameAdapter();
        waitingAdapter = new EntrantNameAdapter();
        cancelledAdapter = new EntrantNameAdapter();

        setupRecyclerView(view.findViewById(R.id.finalEntrants), finalAdapter);
        setupRecyclerView(view.findViewById(R.id.invitedEntrants), invitedAdapter);
        setupRecyclerView(view.findViewById(R.id.waitingList), waitingAdapter);
        setupRecyclerView(view.findViewById(R.id.cancelledEntrants), cancelledAdapter);

        view.findViewById(R.id.finalEntrantsTitle).setOnClickListener(v -> {
            displayFinalEntrants = !displayFinalEntrants;
            view.findViewById(R.id.finalEntrants).setVisibility(displayFinalEntrants ? View.VISIBLE : View.GONE);
        });
        view.findViewById(R.id.invitedEntrantsTitle).setOnClickListener(v -> {
            displayInvitedEntrants = !displayInvitedEntrants;
            view.findViewById(R.id.invitedEntrants).setVisibility(displayInvitedEntrants ? View.VISIBLE : View.GONE);
        });
        view.findViewById(R.id.waitingListTitle).setOnClickListener(v -> {
            displayWaitingList = !displayWaitingList;
            view.findViewById(R.id.waitingList).setVisibility(displayWaitingList ? View.VISIBLE : View.GONE);
        });
        view.findViewById(R.id.cancelledEntrantsTitle).setOnClickListener(v -> {
            displayCancelledEntrants = !displayCancelledEntrants;
            view.findViewById(R.id.cancelledEntrants).setVisibility(displayCancelledEntrants ? View.VISIBLE : View.GONE);
        });

        view.findViewById(R.id.downloadFinalEntrantsBtn).setOnClickListener(v -> {
            if (finalEntrants.isEmpty()) {
                Toast.makeText(getContext(), "No final entrants to download", Toast.LENGTH_SHORT).show();
            } else {
                CSVService.storeCSV(getContext(), finalEntrants);
                Toast.makeText(getContext(), "Downloading final entrants CSV...", Toast.LENGTH_SHORT).show();
            }
        });

        if (eventId != null) {
            loadEntrants();
        }
    }

    private void setupRecyclerView(RecyclerView recyclerView, EntrantNameAdapter adapter) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void loadEntrants() {
        // Load Waiting List
        loadCategory("waitlist-entrants", waitingAdapter);
        // Load Invited
        loadCategory("invited-entrants", invitedAdapter);
        // Load Final
        loadCategory("final-entrants", finalAdapter);
        // Load Cancelled
        loadCategory("cancelled-entrants", cancelledAdapter);
    }

    private void loadCategory(String collectionName, EntrantNameAdapter adapter) {
        if ("final-entrants".equals(collectionName)) {
            finalEntrants.clear();
        }
        db.collection("events")
                .document(eventId)
                .collection(collectionName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Profile> profiles = new ArrayList<>();
                    int total = queryDocumentSnapshots.size();
                    if (total == 0) {
                        adapter.setProfiles(profiles);
                        return;
                    }
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getId();
                        profileRepo.getProfile(userId, profile -> {
                            if (profile != null) {
                                profiles.add(profile);
                                if ("final-entrants".equals(collectionName)) {
                                    finalEntrants.add(new Pair<>(profile.getName(), profile.getEmail()));
                                }
                            }
                            if (profiles.size() == total || (profiles.size() + getFailedCount(queryDocumentSnapshots, profiles) == total)) {
                                if (isAdded()) {
                                    requireActivity().runOnUiThread(() -> adapter.setProfiles(profiles));
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> Log.e("EventSignupList", "Error loading " + collectionName, e));
    }



    private int getFailedCount(Object snapshots, List<Profile> profiles) {
        // Simple heuristic for concurrent loading
        return 0; 
    }
}
