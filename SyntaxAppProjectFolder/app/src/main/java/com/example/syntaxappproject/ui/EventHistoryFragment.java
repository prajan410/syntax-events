package com.example.syntaxappproject.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.EntrantHomeRepository;
import com.example.syntaxappproject.EventAdapter;
import com.example.syntaxappproject.EventDetail;
import com.example.syntaxappproject.EventJoinRepository;
import com.example.syntaxappproject.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment that displays the list of events the currently authenticated
 * entrant has joined, forming their personal event history.
 *
 * <p>All events are fetched from Firestore via {@link EntrantHomeRepository},
 * then filtered to only those the user has joined using {@link EventJoinRepository}.
 * The results are displayed in a {@link RecyclerView} backed by an
 * {@link EventAdapter}.</p>
 */
public class EventHistoryFragment extends HomeBar {

    private EventAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyText;
    private View loadingSpinner;

    final AuthenticationService authService = new AuthenticationService();
    boolean disableFirestoreForTest = false;
    final EntrantHomeRepository entrantHomeRepo = new EntrantHomeRepository();
    EventJoinRepository joinRepo = new EventJoinRepository();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try { setupHotbar(view); } catch (Exception ignored) {}

        View headerTitle = view.findViewById(R.id.headerTitle);
        View mainCard    = view.findViewById(R.id.mainCard);

        headerTitle.setTranslationY(-20f);
        headerTitle.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        mainCard.setTranslationY(30f);
        mainCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(200).start();

        recyclerView = view.findViewById(R.id.eventHistoryList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(new ArrayList<>(), this::openEventDetail);
        recyclerView.setAdapter(adapter);
        emptyText = view.findViewById(R.id.emptyText);
        loadingSpinner = view.findViewById(R.id.loadingSpinner);

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

        if (!disableFirestoreForTest) {
            loadingSpinner.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            loadJoinedEvents();
        }
    }

    /**
     * Loads all events and filters to those the user has joined.
     * Uses AtomicInteger to track async completion across multiple hasJoined checks.
     */
    private void loadJoinedEvents() {
        if (disableFirestoreForTest) return;

        String uid = authService.getCurrentUserId();

        entrantHomeRepo.getEvents(events -> {
            if (events == null || events.isEmpty()) {
                requireActivity().runOnUiThread(() -> {
                    adapter.updateList(new ArrayList<>());
                    recyclerView.setVisibility(View.GONE);
                    loadingSpinner.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                });
                return;
            }

            List<EventDetail> joinedEvents = new ArrayList<>();
            AtomicInteger counter = new AtomicInteger(0);

            for (EventDetail event : events) {
                joinRepo.hasJoined(event.getEventId(), uid, joined -> {
                    if (joined) {
                        synchronized (joinedEvents) {
                            joinedEvents.add(event);
                        }
                    }
                    if (counter.incrementAndGet() == events.size()) {
                        requireActivity().runOnUiThread(() -> {
                            adapter.updateList(joinedEvents);
                            if (joinedEvents.isEmpty()) {
                                loadingSpinner.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.GONE);
                                emptyText.setVisibility(View.VISIBLE);
                            } else {
                                loadingSpinner.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                emptyText.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Test helper: Manually sets events list, bypassing Firestore.
     */
    void setEventsForTest(List<EventDetail> events) {
        if (adapter != null) {
            adapter.updateList(events);
        }
    }

    /**
     * Navigates to the event detail screen for the given event.
     */
    private void openEventDetail(EventDetail event) {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", event.getEventId());
        Navigation.findNavController(requireView())
                .navigate(R.id.toEventDetailFragment, bundle);
    }

    /**
     * Replaces the join repository, intended for use in instrumented tests.
     */
    public void setEventJoinRepo(EventJoinRepository repo) {
        this.joinRepo = repo;
    }
}