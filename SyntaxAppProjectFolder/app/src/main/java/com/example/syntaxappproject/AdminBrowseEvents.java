package com.example.syntaxappproject;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

/**
 * Fragment for administrators to browse all events in the system.
 * Fetches events from Firestore and displays them in a RecyclerView.
 */
public class AdminBrowseEvents extends Fragment {

    private ArrayList<EventDetail> eventList;
    private ArrayList<String> eventIds;
    private AdminEventAdapter adapter;
    private RecyclerView recyclerView;
    private View loadingSpinner;
    private View emptyText;

    /**
     * Default constructor required for fragment instantiation.
     */
    public AdminBrowseEvents() {}

    /**
     * Inflates the layout, initializes views, sets up animations,
     * and loads events from Firestore.
     *
     * @param inflater           the layout inflater used to inflate the fragment's view
     * @param container          the parent view group that the fragment's UI attaches to
     * @param savedInstanceState previously saved instance state, if any
     * @return the inflated view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_events, container, false);

        recyclerView   = view.findViewById(R.id.recycler_events);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);
        emptyText      = view.findViewById(R.id.emptyText);

        View headerTitle    = view.findViewById(R.id.headerTitle);
        View mainCard       = view.findViewById(R.id.mainCard);
        TextView countBadge = view.findViewById(R.id.eventCountBadge);
        Button doneButton   = view.findViewById(R.id.doneButton);

        doneButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                    .navigate(R.id.adminFragment);
        });

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        countBadge.animate().alpha(1f)
                .setDuration(300).setStartDelay(200).start();

        mainCard.setTranslationY(30f);
        mainCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(250).start();

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        eventList = new ArrayList<>();
        eventIds  = new ArrayList<>();
        adapter   = new AdminEventAdapter(eventList, eventIds);
        recyclerView.setAdapter(adapter);

        loadingSpinner.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        // Fetch all events from Firestore
        FirebaseFirestore.getInstance()
                .collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    eventIds.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        EventDetail event = doc.toObject(EventDetail.class);
                        if (event != null) {
                            eventList.add(event);
                            eventIds.add(doc.getId()); // Store ID separately for later navigation
                        }
                    }

                    adapter.notifyDataSetChanged();
                    loadingSpinner.setVisibility(View.GONE);

                    int count = eventList.size();
                    countBadge.setText(count + (count == 1 ? " event" : " events"));

                    if (eventList.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyText.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyText.setVisibility(View.GONE);
                    }
                });

        return view;
    }
}