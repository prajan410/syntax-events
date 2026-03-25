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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.EventAdapter;
import com.example.syntaxappproject.EventDetail;
import com.example.syntaxappproject.ImageCacheManager;
import com.example.syntaxappproject.ImageItem;
import com.example.syntaxappproject.OrganizerEventsRepository;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment displaying events owned by the currently authenticated organizer.
 *
 * <p>Shows the organizer's event list in a {@link RecyclerView} with black line
 * dividers and spacing between items. Includes a FAB to create new events.
 * Dynamically shows or hides the "All Events" tab based on whether the user
 * also has the entrant role. Shows an admin FAB if the current user has admin
 * privileges. Extends {@link HomeBar} for bottom navigation.</p>
 */
public class OrganizerEventsFragment extends HomeBar {

    private EventAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyText;
    private final OrganizerEventsRepository repo = new OrganizerEventsRepository();
    private final AuthenticationService authService = new AuthenticationService();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try { setupHotbar(view); } catch (Exception ignored) {}

        View titleText = view.findViewById(R.id.textView);
        View tabRow    = view.findViewById(R.id.tabRow);
        View mainCard  = view.findViewById(R.id.mainCard);
        View fab       = view.findViewById(R.id.fabNewEvent);
        View fabAdmin  = view.findViewById(R.id.fabAdmin);

        titleText.setTranslationY(-20f);
        titleText.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(100).start();

        tabRow.setTranslationY(-10f);
        tabRow.animate().alpha(1f).translationY(0f)
                .setDuration(400).setStartDelay(200).start();

        mainCard.setTranslationY(30f);
        mainCard.animate().alpha(1f).translationY(0f)
                .setDuration(500).setStartDelay(300).start();

        fab.setScaleX(0f);
        fab.setScaleY(0f);
        fab.animate().alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(400).setStartDelay(500).start();

        view.findViewById(R.id.eventsButton).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.toHomeFragment)
        );

        view.findViewById(R.id.fabNewEvent).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.createEventFragment)
        );

        recyclerView = view.findViewById(R.id.organizerEventList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(new ArrayList<>(), this::openEventDetail);
        recyclerView.setAdapter(adapter);
        emptyText = view.findViewById(R.id.emptyText);

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

                for (int i = 0; i < parent.getChildCount() - 1; i++) { // Draw line between items, not after the last one
                    View child = parent.getChildAt(i);
                    float y = child.getBottom() + 12f;
                    c.drawLine(child.getLeft(), y, child.getRight(), y, paint);
                }
            }
        });

        String uid = authService.getCurrentUserId();

        repo.getOrganizerEvents(uid, events -> {
            if (!isAdded()) return;
            if (events == null || events.isEmpty()) {
                requireActivity().runOnUiThread(() -> {
                    adapter.updateList(new ArrayList<>());
                    recyclerView.setVisibility(View.GONE);
                    emptyText.setVisibility(View.VISIBLE);
                });
                return;
            }

            boolean allCached = true;
            for (EventDetail event : events) {
                if (!ImageCacheManager.has(event.getEventId())) {
                    allCached = false;
                    break;
                }
            }

            if (allCached) {
                requireActivity().runOnUiThread(() -> {
                    adapter.updateList(events);
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyText.setVisibility(View.GONE);
                });
                return;
            }

            int total = events.size();
            AtomicInteger loaded = new AtomicInteger(0);

            for (EventDetail event : events) {
                String eventId = event.getEventId();
                if (ImageCacheManager.has(eventId)) {
                    if (loaded.incrementAndGet() == total)
                        requireActivity().runOnUiThread(() -> {
                            adapter.updateList(events);
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyText.setVisibility(View.GONE);
                        });
                    continue;
                }
                ImageItem.fetchByEventId(eventId, new ImageItem.ImageCallback() {
                    @Override
                    public void onImageLoaded(ImageItem imageItem) {
                        new Thread(() -> {
                            try {
                                if (imageItem != null && imageItem.imageUrl != null) {
                                    byte[] decoded = Base64.decode(imageItem.imageUrl, Base64.DEFAULT);
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                                    if (bitmap != null) ImageCacheManager.put(eventId, bitmap);
                                }
                            } catch (Exception ignored) {}
                            if (loaded.incrementAndGet() == total)
                                requireActivity().runOnUiThread(() -> {
                                    adapter.updateList(events);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    emptyText.setVisibility(View.GONE);
                                });
                        }).start();
                    }

                    @Override
                    public void onError(Exception e) {
                        if (loaded.incrementAndGet() == total)
                            requireActivity().runOnUiThread(() -> {
                                adapter.updateList(events);
                                recyclerView.setVisibility(View.VISIBLE);
                                emptyText.setVisibility(View.GONE);
                            });
                    }
                });
            }
        });

        ProfileRepository profileRepo = new ProfileRepository();
        profileRepo.getProfile(uid, profile -> {
            if (profile == null || !isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                view.findViewById(R.id.eventsButton)
                        .setVisibility(profile.isEntrant() ? View.VISIBLE : View.GONE);

                if (profile.isAdmin()) {
                    fabAdmin.setVisibility(View.VISIBLE);
                    fabAdmin.setScaleX(0f);
                    fabAdmin.setScaleY(0f);
                    fabAdmin.animate().alpha(1f).scaleX(1f).scaleY(1f)
                            .setDuration(400).setStartDelay(600).start();
                    fabAdmin.setOnClickListener(v ->
                            NavHostFragment.findNavController(this).navigate(R.id.adminFragment)
                    );
                }
            });
        });
    }

    /**
     * Test helper: Manually sets the events list, bypassing Firestore.
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
        NavHostFragment.findNavController(this)
                .navigate(R.id.toEventDetailFragment, bundle);
    }
}