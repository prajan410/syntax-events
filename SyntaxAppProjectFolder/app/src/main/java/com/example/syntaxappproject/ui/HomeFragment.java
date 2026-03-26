package com.example.syntaxappproject.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.EntrantHomeRepository;
import com.example.syntaxappproject.EventAdapter;
import com.example.syntaxappproject.EventDetail;
import com.example.syntaxappproject.EventJoinRepository;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HomeFragment extends HomeBar {

    private EventAdapter adapter;
    private RecyclerView recyclerView;
    private View rootView;

    private final AuthenticationService authService = new AuthenticationService();
    private final EntrantHomeRepository entrantHomeRepo = new EntrantHomeRepository();
    private final EventJoinRepository joinRepo = new EventJoinRepository();
    private final ProfileRepository profileRepo = new ProfileRepository();
    private List<EventDetail> allEvents = new ArrayList<>();

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    /**
     * Initializes views, animations, role-based button visibility, event list,
     * search bar, and admin FAB. The admin FAB is only shown if the current
     * user has admin privileges.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupHotbar(view);
        this.rootView = view;

        TextInputEditText searchBar = view.findViewById(R.id.searchInput);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterEvents(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        recyclerView = view.findViewById(R.id.eventList);
        Button eventsButton = view.findViewById(R.id.eventsButton);
        Button createEventButton = view.findViewById(R.id.createEventButton);
        View fabAdmin  = view.findViewById(R.id.fabAdmin);
        View titleText = view.findViewById(R.id.textView);
        View roleButtonRow = view.findViewById(R.id.roleButtonRow);
        View mainCard = view.findViewById(R.id.mainCard);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(new ArrayList<>(), this::openEventDetail);
        recyclerView.setAdapter(adapter);

        titleText.setTranslationY(-20f);
        titleText.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start();
        roleButtonRow.setTranslationY(-10f);
        roleButtonRow.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(200).start();
        mainCard.setTranslationY(30f);
        mainCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(300).start();

        String uid = authService.getCurrentUserId();
        profileRepo.getProfile(uid, profile -> {
            if (profile == null || !isAdded()) return;

            boolean isEntrant  = profile.isEntrant();
            boolean isOrganizer = profile.isOrganizer();

            requireActivity().runOnUiThread(() -> {
                eventsButton.setVisibility(isEntrant ? View.VISIBLE : View.GONE);
                createEventButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);

                eventsButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2ECC71")));
                eventsButton.setTextColor(Color.WHITE);
                createEventButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#D3D3D3")));
                createEventButton.setTextColor(Color.BLACK);

                if (isEntrant) {
                    if (!allEvents.isEmpty()) {
                        adapter.updateList(allEvents);
                    } else {
                        setEventsList();
                    }
                } else {
                    NavHostFragment.findNavController(this).navigate(R.id.organizerEventsFragment);
                }

                eventsButton.setOnClickListener(v -> {
                    if (!allEvents.isEmpty()) {
                        adapter.updateList(allEvents);
                    } else {
                        setEventsList();
                    }
                });
                createEventButton.setOnClickListener(v ->
                        NavHostFragment.findNavController(this).navigate(R.id.organizerEventsFragment));

                if (profile.isAdmin()) {
                    fabAdmin.setVisibility(View.VISIBLE);
                    fabAdmin.setScaleX(0f);
                    fabAdmin.setScaleY(0f);
                    fabAdmin.animate().alpha(1f).scaleX(1f).scaleY(1f)
                            .setDuration(400).setStartDelay(600).start();
                    fabAdmin.setOnClickListener(v ->
                            NavHostFragment.findNavController(this).navigate(R.id.adminFragment));
                }
            });
        });
    }

    private void filterEvents(String query) {
        if (query.isEmpty()) { adapter.updateList(allEvents); return; }
        String lower = query.toLowerCase();
        List<EventDetail> results = new ArrayList<>();
        for (EventDetail event : allEvents) {
            if (event.getName() != null && event.getName().toLowerCase().contains(lower)) {
                results.add(event);
            }
        }
        adapter.updateList(results);
    }

    private void setEventsList() {
        String uid = authService.getCurrentUserId();

        requireActivity().runOnUiThread(() -> {
            recyclerView.setVisibility(View.GONE);
            rootView.findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        });

        entrantHomeRepo.getEvents(events -> {
            if (events == null || events.isEmpty()) {
                requireActivity().runOnUiThread(() -> {
                    rootView.findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    allEvents.clear();
                    adapter.updateList(new ArrayList<>());
                });
                return;
            }

            List<EventDetail> filtered = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger(0);
            long now = System.currentTimeMillis();

            for (EventDetail event : events) {
                if (event.isPrivateEvent()) {
                    if (counter.incrementAndGet() == events.size()) flush(filtered);
                    continue;
                }
                if (uid != null && uid.equals(event.getOrganizerUid())) {
                    if (counter.incrementAndGet() == events.size()) flush(filtered);
                    continue;
                }

                long regStart = parseDateMillis(event.getStartingRegistrationPeriod());
                long regEnd   = parseDateMillis(event.getEndingRegistrationPeriod());
                if (regStart == -1 || regEnd == -1 || now < regStart || now > regEnd) {
                    if (counter.incrementAndGet() == events.size()) flush(filtered);
                    continue;
                }

                joinRepo.hasJoined(event.getEventId(), uid, joined -> {
                    if (!joined) {
                        long capacity = event.getCapacity();
                        if (capacity == 0 || event.getWaitlistCount() < capacity) {
                            synchronized (filtered) { filtered.add(event); }
                        }
                    }
                    if (counter.incrementAndGet() == events.size()) flush(filtered);
                });
            }
        });
    }

    private void flush(List<EventDetail> filtered) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            allEvents = new ArrayList<>(filtered);
            adapter.updateList(filtered);
            rootView.findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        });
    }

    /**
     * Parses a date string into epoch milliseconds.
     * Expects format "yyyy-MM-dd" or "MM/dd/yyyy" — adjust to match your stored format.
     * Returns -1 if unparseable.
     */
    private long parseDateMillis(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return -1;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            sdf.setLenient(false);
            return sdf.parse(dateStr).getTime();
        } catch (Exception ignored) {}
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.getDefault());
            sdf.setLenient(false);
            return sdf.parse(dateStr).getTime();
        } catch (Exception ignored) {}
        return -1;
    }

    private void openEventDetail(EventDetail event) {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", event.getEventId());
        NavHostFragment.findNavController(this).navigate(R.id.toEventDetailFragment, bundle);
    }
}
