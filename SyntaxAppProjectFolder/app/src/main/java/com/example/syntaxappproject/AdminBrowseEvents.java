package com.example.syntaxappproject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.ui.AdminFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for administrators to browse all events in the system.
 *
 * <p>This fragment displays a comprehensive list of all events from the Firestore
 * {@code events} collection, allowing administrators to:</p>
 * <ul>
 *   <li>View all events with their names, organizers, locations, and capacities</li>
 *   <li>Search events in real-time by name, description, or location</li>
 *   <li>Click on any event to view detailed information and manage/delete it</li>
 *   <li>See the total count of events in the current view</li>
 * </ul>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Real-time search updates as the user types (case-insensitive substring matching)</li>
 *   <li>Loading spinner while fetching data from Firestore</li>
 *   <li>Empty state message when no events match the search criteria</li>
 *   <li>Entrance animations for visual appeal</li>
 * </ul>
 *
 * <p>Navigation: Clicking Done returns to {@link AdminFragment}.</p>
 *
 * @see AdminEventAdapter
 * @see AdminEventDetails
 * @see AdminFragment
 */
public class AdminBrowseEvents extends Fragment {

    /** Log tag for debugging. */
    private static final String TAG = "AdminBrowseEvents";

    /** Complete list of all events fetched from Firestore. */
    private ArrayList<EventDetail> allEvents;

    /** Firestore document IDs corresponding to each event. */
    private ArrayList<String> allEventIds;

    /** Adapter that binds event data to the RecyclerView. */
    private AdminEventAdapter adapter;

    /** RecyclerView displaying the list of events. */
    private RecyclerView recyclerView;

    /** Spinner shown while events are loading. */
    private View loadingSpinner;

    /** Text shown when no events match the current filter. */
    private TextView emptyText;

    /** Search input for filtering events by name, description, or location. */
    private EditText searchInput;

    /** Badge showing the number of events in the current view. */
    private TextView countBadge;

    /** Default constructor required for fragment instantiation. */
    public AdminBrowseEvents() {}

    /**
     * Inflates the layout, initializes views, sets up animations,
     * configures the search bar, and loads events from Firestore.
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
        searchInput    = view.findViewById(R.id.searchInput);
        countBadge     = view.findViewById(R.id.eventCountBadge);

        View headerTitle    = view.findViewById(R.id.headerTitle);
        View mainCard       = view.findViewById(R.id.mainCard);
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

        allEvents = new ArrayList<>();
        allEventIds = new ArrayList<>();
        adapter = new AdminEventAdapter(new ArrayList<>(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadingSpinner.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "Search text changed: '" + s.toString() + "'");
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        FirebaseFirestore.getInstance()
                .collection("events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d(TAG, "Events loaded: " + queryDocumentSnapshots.size());
                    allEvents.clear();
                    allEventIds.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        EventDetail event = doc.toObject(EventDetail.class);
                        if (event != null) {
                            allEvents.add(event);
                            allEventIds.add(doc.getId());
                        }
                    }

                    loadingSpinner.setVisibility(View.GONE);

                    int count = allEvents.size();
                    countBadge.setText(count + (count == 1 ? " event" : " events"));
                    applyFilter();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load events", e);
                    loadingSpinner.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    emptyText.setText("Failed to load events");
                });

        return view;
    }

    /**
     * Applies the search filter to the event list.
     *
     * <p>Filters by event name, description, and location using case-insensitive
     * substring matching. Always filters from the full list ({@code allEvents})
     * rather than the previously filtered list to ensure consistent results when
     * backspacing.</p>
     *
     * <p>Updates the adapter with filtered results and updates the count badge
     * and empty state accordingly.</p>
     */
    private void applyFilter() {
        String query = searchInput.getText().toString().toLowerCase();
        Log.d(TAG, "Applying filter with query: '" + query + "', total events: " + allEvents.size());

        List<EventDetail> filteredEvents = new ArrayList<>();
        List<String> filteredIds = new ArrayList<>();

        for (int i = 0; i < allEvents.size(); i++) {
            EventDetail event = allEvents.get(i);
            String name = event.getName() != null ? event.getName().toLowerCase() : "";
            String description = event.getDescription() != null ? event.getDescription().toLowerCase() : "";
            String location = event.getLocation() != null ? event.getLocation().toLowerCase() : "";

            boolean matches = query.isEmpty() || name.contains(query) ||
                    description.contains(query) || location.contains(query);

            if (matches) {
                filteredEvents.add(event);
                filteredIds.add(allEventIds.get(i));
            }
        }

        Log.d(TAG, "Filtered events count: " + filteredEvents.size());
        adapter.updateData(filteredEvents, filteredIds);
        countBadge.setText(filteredEvents.size() + (filteredEvents.size() == 1 ? " event" : " events"));

        if (filteredEvents.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(query.isEmpty() ? "No events found" : "No matching events");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
        }
    }
}