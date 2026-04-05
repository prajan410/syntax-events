package com.example.syntaxappproject.ui;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.EntrantHomeRepository;
import com.example.syntaxappproject.EventAdapter;
import com.example.syntaxappproject.EventDetail;
import com.example.syntaxappproject.EventFilterViewModel;
import com.example.syntaxappproject.EventJoinRepository;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment that serves as the home screen for entrants and organizers.
 * Entrants see a filterable list of open events they have not yet joined.
 * Organizers are redirected to their own events screen. Admin users also
 * receive a floating action button to access the admin panel.
 */
public class HomeFragment extends HomeBar {

    private EventAdapter adapter;
    private RecyclerView recyclerView;
    private View rootView;
    private View emptyText;

    private final AuthenticationService authService = new AuthenticationService();
    private final EntrantHomeRepository entrantHomeRepo = new EntrantHomeRepository();
    private final EventJoinRepository joinRepo = new EventJoinRepository();
    private final ProfileRepository profileRepo = new ProfileRepository();
    private List<EventDetail> allEvents = new ArrayList<>();
    private List<EventDetail> displayEvents = new ArrayList<>();

    private TextInputLayout searchLayout;
    private String startDateFilter = null;
    private String endDateFilter = null;
    private long capacityFilter = -1;
    private String searchQuery = "";
    private EventFilterViewModel filterViewModel;

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }
    @Override
    public void onResume() {
        super.onResume();
        setEventsList();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupHotbar(view);
        this.rootView = view;
        emptyText = view.findViewById(R.id.emptyText);

        TextInputEditText searchBar = view.findViewById(R.id.searchInput);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { searchFilter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        searchLayout = view.findViewById(R.id.searchLayout);

        filterViewModel = new ViewModelProvider(requireActivity()).get(EventFilterViewModel.class);

        searchLayout.setEndIconOnClickListener(v -> {
            FilterDialogFragment filter = new FilterDialogFragment();
            filter.show(getParentFragmentManager(), "FilterDialog");
        });

        Observer<Object> observer = o -> applyAllFilters();

        filterViewModel.getStartDate().observe(getViewLifecycleOwner(), observer);
        filterViewModel.getEndDate().observe(getViewLifecycleOwner(), observer);
        filterViewModel.getCapacity().observe(getViewLifecycleOwner(), observer);

        recyclerView = view.findViewById(R.id.eventList);
        Button eventsButton     = view.findViewById(R.id.eventsButton);
        Button createEventButton = view.findViewById(R.id.createEventButton);
        View fabAdmin            = view.findViewById(R.id.fabAdmin);
        View titleText           = view.findViewById(R.id.textView);
        View roleButtonRow       = view.findViewById(R.id.roleButtonRow);
        View mainCard            = view.findViewById(R.id.mainCard);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(new ArrayList<>(), this::openEventDetail);
        recyclerView.setAdapter(adapter);

        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View v,
                                       @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.top    = 12;
                outRect.bottom = 12;
            }

            @Override
            public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state) {
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(1f);

                for (int i = 0; i < parent.getChildCount() - 1; i++) {
                    View child = parent.getChildAt(i);
                    float y = child.getBottom() + 12f;
                    c.drawLine(child.getLeft(), y, child.getRight(), y, paint);
                }
            }
        });

        titleText.setTranslationY(-20f);
        titleText.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start();
        roleButtonRow.setTranslationY(-10f);
        roleButtonRow.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(200).start();
        mainCard.setTranslationY(30f);
        mainCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(300).start();

        String uid = authService.getCurrentUserId();
        profileRepo.getProfile(uid, profile -> {
            if (profile == null || !isAdded()) return;

            boolean isEntrant   = profile.isEntrant();
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

    /**
     * Filters the displayed event list by name using case-insensitive substring match.
     */
    private void searchFilter(String query) {
        if (query.isEmpty()) { searchQuery = ""; }
        else {
            searchQuery = query.toLowerCase();
        }
        applyAllFilters();
    }

    /**
     * Loads joinable events for entrants, filtering out:
     * - Events the user has already joined
     * - Events outside registration window
     * - Events created by the current user
     * - Events with full waitlist
     */
    private void setEventsList() {
        String uid = authService.getCurrentUserId();

        requireActivity().runOnUiThread(() -> {
            recyclerView.setVisibility(View.GONE);
            rootView.findViewById(R.id.loadingSpinner).setVisibility(View.VISIBLE);
        });

        entrantHomeRepo.getEvents(events -> {
            //-- No events currently available.
            if (events == null || events.isEmpty()) {
                requireActivity().runOnUiThread(() -> {
                    rootView.findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                    allEvents.clear();
                    adapter.updateList(new ArrayList<>());
                });
                return;
            }
            //otherwise list events
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

    /**
     * Updates the adapter with filtered events and restores RecyclerView visibility.
     * Called after all async join-checks complete.
     */
    private void flush(List<EventDetail> filtered) {
        if (!isAdded()) return;
        requireActivity().runOnUiThread(() -> {
            allEvents = new ArrayList<>(filtered);
            applyAllFilters();
            rootView.findViewById(R.id.loadingSpinner).setVisibility(View.GONE);
            if (filtered.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyText.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyText.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Apply all the filters include keyword search, time filter, capacity filter
     * and display the result events.
     */
    private void applyAllFilters() {

        startDateFilter = filterViewModel.getStartValue();
        endDateFilter = filterViewModel.getEndValue();
        capacityFilter = filterViewModel.getCapacityValue();


        List<EventDetail> result = new ArrayList<>();

        for (EventDetail event : allEvents) {

            long eventStartDate = parseDateMillis(event.getStartingEventDate());
            long eventEndDate = parseDateMillis(event.getEndingEventDate());

            if (!searchQuery.isEmpty()){
                if (event.getName() == null || !event.getName().toLowerCase().contains(searchQuery)) {
                    continue;
                }
            }

            if (startDateFilter != null && !startDateFilter.isEmpty()) {
                long start = parseDateMillis(startDateFilter);
                if (eventStartDate == -1 || eventStartDate < start) continue;
            }

            if (endDateFilter != null && !endDateFilter.isEmpty()) {
                long end = parseDateMillis(endDateFilter);
                if (eventStartDate == -1 || eventStartDate > end) continue;
            }

            if (capacityFilter != -1) {
                if (event.getCapacity() < capacityFilter) continue;
            }

            result.add(event);
        }

        if (filterViewModel.hasFilter()) {
            searchLayout.setEndIconTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            searchLayout.setEndIconTintList(ColorStateList.valueOf(Color.GRAY));
        }

        displayEvents = result;
        adapter.updateList(result);
    }

    /**
     * Parses a date string into epoch milliseconds.
     * Supports "yyyy-MM-dd" and "MM/dd/yyyy" formats.
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

    /**
     * Navigates to the event detail screen for the given event.
     */
    private void openEventDetail(EventDetail event) {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", event.getEventId());
        NavHostFragment.findNavController(this).navigate(R.id.toEventDetailFragment, bundle);
    }

    void setEventsForTest(List<EventDetail> events) {
        this.allEvents = events;
        this.displayEvents = events;
        if (adapter != null) {
            adapter.updateList(events);
        }
    }

}