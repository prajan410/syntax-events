package com.example.syntaxappproject.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.CSVService;
import com.example.syntaxappproject.EntrantNameAdapter;
import com.example.syntaxappproject.EventDetailRepository;
import com.example.syntaxappproject.Profile;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment that displays all entrants for an event organized into four categories:
 * Waiting List, Selected Entrants, Final Entrants, and Cancelled Entrants.
 *
 * <p>This fragment is used by event organizers to manage and view all users
 * who have interacted with their event. Each category is displayed in a
 * collapsible card with a count badge showing the number of entrants.</p>
 *
 * <p>Features:</p>
 * <ul>
 *   <li>Collapsible/expandable sections for each entrant category</li>
 *   <li>Real-time count badges showing number of entrants per category</li>
 *   <li>Download final entrants list as CSV file</li>
 *   <li>Entrance animations for visual appeal</li>
 *   <li>Empty state messages when no entrants in a category</li>
 * </ul>
 *
 * <p>The fragment loads data from Firestore subcollections:
 * <ul>
 *   <li><b>waitlist-entrants</b> - Users who joined the waiting list</li>
 *   <li><b>selected-entrants</b> - Users who won the lottery</li>
 *   <li><b>cancelled-entrants</b> - Users who declined or cancelled</li>
 * </ul>
 * Final entrants are derived from selected-entrants for CSV export.</p>
 *
 * @see EntrantNameAdapter
 * @see CSVService
 * @see HomeBar
 */
public class EventSignupListFragment extends HomeBar {

    /** Firestore document ID of the event being displayed. */
    private String eventId;

    /** Name of the event (loaded from Firestore). */
    private String eventName;

    /** Firestore database instance. */
    private FirebaseFirestore db;

    /** Repository for profile data operations. */
    private ProfileRepository profileRepo;

    /** Repository for event detail operations. */
    private EventDetailRepository eventDetailRepo;

    /** Adapter for displaying waiting list entrants. */
    private EntrantNameAdapter waitingAdapter;

    /** Adapter for displaying selected entrants. */
    private EntrantNameAdapter selectedAdapter;

    /** Adapter for displaying final entrants. */
    private EntrantNameAdapter finalAdapter;

    /** Adapter for displaying cancelled entrants. */
    private EntrantNameAdapter cancelledAdapter;

    /** List of final entrants for CSV export (name, email pairs). */
    private List<Pair<String, String>> finalEntrants = new ArrayList<>();

    /** Container layouts for each collapsible section. */
    private LinearLayout waitingContent, selectedContent, finalContent, cancelledContent;

    /** TextView badges showing counts for each section. */
    private TextView waitingCount, selectedCount, finalCount, cancelledCount;

    /** Empty state TextViews for each section. */
    private TextView waitingEmpty, selectedEmpty, finalEmpty, cancelledEmpty;

    public boolean testingMode = false;

    /** Expanded state flags for each collapsible section. */
    private boolean waitingExpanded = true;
    private boolean selectedExpanded = true;
    private boolean finalExpanded = true;
    private boolean cancelledExpanded = true;

    /**
     * Called when the fragment is created. Initializes Firestore and repositories,
     * and retrieves the event ID from arguments.
     *
     * @param savedInstanceState previously saved instance state, if any
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        profileRepo = new ProfileRepository();
        eventDetailRepo = new EventDetailRepository();
        testingMode = getArguments().getBoolean("testingMode", false);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
    }

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater           the layout inflater
     * @param container          the parent view group
     * @param savedInstanceState previously saved state, if any
     * @return the inflated view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_signup_list, container, false);
    }

    /**
     * Called immediately after onCreateView. Initializes all UI components,
     * sets up adapters, configures click listeners, and loads event data.
     *
     * @param view               the inflated view
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!testingMode) setupHotbar(view);

        loadEventName(view);
        initializeAdapters();
        setupRecyclerViews(view);
        initializeContentContainers(view);
        initializeCountBadges(view);
        initializeEmptyTextViews(view);
        setupHeaderClickListeners(view);
        setupButtonClickListeners(view);
        animateViews(view);
        if (eventId != null) {
            loadAllEntrants();
        } else {
            Toast.makeText(getContext(), "No event ID provided", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Loads the event name from Firestore and displays it in the subtitle.
     *
     * @param view the fragment view
     */
    private void loadEventName(View view) {
        if (eventId != null) {
            eventDetailRepo.getEventDetail(eventId, event -> {
                if (event != null && event.getName() != null) {
                    eventName = event.getName();
                    TextView eventNameSubtitle = view.findViewById(R.id.eventNameSubtitle);
                    if (eventNameSubtitle != null) {
                        eventNameSubtitle.setText(eventName);
                    }
                }
            });
        }
    }

    /**
     * Initializes all entrant adapters.
     */
    private void initializeAdapters() {
        waitingAdapter = new EntrantNameAdapter();
        selectedAdapter = new EntrantNameAdapter();
        finalAdapter = new EntrantNameAdapter();
        cancelledAdapter = new EntrantNameAdapter();
    }

    /**
     * Sets up RecyclerViews with their respective adapters and layout managers.
     *
     * @param view the fragment view
     */
    private void setupRecyclerViews(View view) {
        RecyclerView waitingRecycler = view.findViewById(R.id.waitingList);
        RecyclerView selectedRecycler = view.findViewById(R.id.selectedList);
        RecyclerView finalRecycler = view.findViewById(R.id.finalList);
        RecyclerView cancelledRecycler = view.findViewById(R.id.cancelledList);

        waitingRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        waitingRecycler.setAdapter(waitingAdapter);

        selectedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        selectedRecycler.setAdapter(selectedAdapter);

        finalRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        finalRecycler.setAdapter(finalAdapter);

        cancelledRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        cancelledRecycler.setAdapter(cancelledAdapter);
    }

    /**
     * Initializes references to collapsible content containers.
     *
     * @param view the fragment view
     */
    private void initializeContentContainers(View view) {
        waitingContent = view.findViewById(R.id.waitingContent);
        selectedContent = view.findViewById(R.id.selectedContent);
        finalContent = view.findViewById(R.id.finalContent);
        cancelledContent = view.findViewById(R.id.cancelledContent);
    }

    /**
     * Initializes references to count badge TextViews.
     *
     * @param view the fragment view
     */
    private void initializeCountBadges(View view) {
        waitingCount = view.findViewById(R.id.waitingCount);
        selectedCount = view.findViewById(R.id.selectedCount);
        finalCount = view.findViewById(R.id.finalCount);
        cancelledCount = view.findViewById(R.id.cancelledCount);
    }

    /**
     * Initializes references to empty state TextViews.
     *
     * @param view the fragment view
     */
    private void initializeEmptyTextViews(View view) {
        waitingEmpty = view.findViewById(R.id.waitingEmpty);
        selectedEmpty = view.findViewById(R.id.selectedEmpty);
        finalEmpty = view.findViewById(R.id.finalEmpty);
        cancelledEmpty = view.findViewById(R.id.cancelledEmpty);
    }

    /**
     * Sets up click listeners for section headers to enable collapse/expand functionality.
     *
     * @param view the fragment view
     */
    private void setupHeaderClickListeners(View view) {
        View waitingHeader = view.findViewById(R.id.waitingHeader);
        View selectedHeader = view.findViewById(R.id.selectedHeader);
        View finalHeader = view.findViewById(R.id.finalHeader);
        View cancelledHeader = view.findViewById(R.id.cancelledHeader);

        if (waitingHeader != null) {
            waitingHeader.setOnClickListener(v -> toggleSection(waitingContent, waitingExpanded));
        }
        if (selectedHeader != null) {
            selectedHeader.setOnClickListener(v -> toggleSection(selectedContent, selectedExpanded));
        }
        if (finalHeader != null) {
            finalHeader.setOnClickListener(v -> toggleSection(finalContent, finalExpanded));
        }
        if (cancelledHeader != null) {
            cancelledHeader.setOnClickListener(v -> toggleSection(cancelledContent, cancelledExpanded));
        }
    }

    /**
     * Sets up click listeners for action buttons (Done and Download CSV).
     *
     * @param view the fragment view
     */
    private void setupButtonClickListeners(View view) {
        MaterialButton doneButton = view.findViewById(R.id.doneButton);
        if (doneButton != null) {
            doneButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            });
        }

        MaterialButton downloadButton = view.findViewById(R.id.downloadFinalEntrantsBtn);
        if (downloadButton != null) {
            downloadButton.setOnClickListener(v -> {
                if (finalEntrants.isEmpty()) {
                    Toast.makeText(getContext(), "No final entrants to download", Toast.LENGTH_SHORT).show();
                } else {
                    CSVService.storeCSV(getContext(), finalEntrants);
                    Toast.makeText(getContext(), "Downloading final entrants CSV...", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Toggles the visibility of a collapsible section.
     *
     * @param content   the LinearLayout containing the section content
     * @param isExpanded the current expanded state of the section
     */
    private void toggleSection(LinearLayout content, boolean isExpanded) {
        if (isExpanded) {
            content.setVisibility(View.GONE);
        } else {
            content.setVisibility(View.VISIBLE);
        }

        if (content == waitingContent) waitingExpanded = !isExpanded;
        else if (content == selectedContent) selectedExpanded = !isExpanded;
        else if (content == finalContent) finalExpanded = !isExpanded;
        else if (content == cancelledContent) cancelledExpanded = !isExpanded;
    }

    /**
     * Animates the entrance of UI elements with fade-in and slide-up effects.
     *
     * @param view the fragment view
     */
    private void animateViews(View view) {
        View headerTitle = view.findViewById(R.id.headerTitle);
        View eventNameSubtitle = view.findViewById(R.id.eventNameSubtitle);
        View waitingCard = view.findViewById(R.id.waitingCard);
        View selectedCard = view.findViewById(R.id.selectedCard);
        View finalCard = view.findViewById(R.id.finalCard);
        View cancelledCard = view.findViewById(R.id.cancelledCard);
        View downloadButton = view.findViewById(R.id.downloadFinalEntrantsBtn);

        if (headerTitle != null) {
            headerTitle.setTranslationY(-20f);
            headerTitle.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(100).start();
        }

        if (eventNameSubtitle != null) {
            eventNameSubtitle.setTranslationY(-20f);
            eventNameSubtitle.animate().alpha(1f).translationY(0f).setDuration(400).setStartDelay(150).start();
        }

        if (waitingCard != null) {
            waitingCard.setTranslationY(30f);
            waitingCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(200).start();
        }

        if (selectedCard != null) {
            selectedCard.setTranslationY(30f);
            selectedCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(300).start();
        }

        if (finalCard != null) {
            finalCard.setTranslationY(30f);
            finalCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(400).start();
        }

        if (cancelledCard != null) {
            cancelledCard.setTranslationY(30f);
            cancelledCard.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(500).start();
        }

        if (downloadButton != null) {
            downloadButton.setTranslationY(30f);
            downloadButton.animate().alpha(1f).translationY(0f).setDuration(500).setStartDelay(600).start();
        }
    }

    /**
     * Loads all entrant categories from Firestore.
     */
    private void loadAllEntrants() {
        loadWaitlistEntrants();
        loadSelectedEntrants();
        loadFinalEntrants();
        loadCancelledEntrants();
    }

    /**
     * Loads waiting list entrants from the "waitlist-entrants" subcollection.
     * Updates the waiting list adapter and count badge.
     */
    private void loadWaitlistEntrants() {
        Log.d("SignupList", "Loading waitlist entrants for event: " + eventId);

        db.collection("events")
                .document(eventId)
                .collection("waitlist-entrants")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Profile> profiles = new ArrayList<>();

                    if (snapshots.isEmpty()) {
                        waitingAdapter.setProfiles(profiles);
                        waitingCount.setText("0");
                        waitingEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    waitingCount.setText(String.valueOf(snapshots.size()));
                    waitingEmpty.setVisibility(View.GONE);

                    AtomicInteger pending = new AtomicInteger(snapshots.size());

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String userId = doc.getId();
                        profileRepo.getProfile(userId, profile -> {
                            if (profile != null) {
                                profiles.add(profile);
                            }
                            if (pending.decrementAndGet() == 0) {
                                requireActivity().runOnUiThread(() -> waitingAdapter.setProfiles(profiles));
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SignupList", "Failed to load waitlist entrants", e);
                    waitingAdapter.setProfiles(new ArrayList<>());
                    waitingCount.setText("0");
                });
    }

    /**
     * Loads selected entrants from the "selected-entrants" subcollection.
     * Updates the selected entrants adapter and count badge.
     */
    private void loadSelectedEntrants() {
        db.collection("events")
                .document(eventId)
                .collection("selected-entrants")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Profile> profiles = new ArrayList<>();
                    if (snapshots.isEmpty()) {
                        selectedAdapter.setProfiles(profiles);
                        selectedCount.setText("0");
                        selectedEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    selectedCount.setText(String.valueOf(snapshots.size()));
                    selectedEmpty.setVisibility(View.GONE);

                    AtomicInteger pending = new AtomicInteger(snapshots.size());

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String userId = doc.getId();
                        profileRepo.getProfile(userId, profile -> {
                            if (profile != null) profiles.add(profile);
                            if (pending.decrementAndGet() == 0) {
                                requireActivity().runOnUiThread(() -> selectedAdapter.setProfiles(profiles));
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SignupList", "Failed to load selected entrants", e);
                    selectedAdapter.setProfiles(new ArrayList<>());
                    selectedCount.setText("0");
                });
    }

    /**
     * Loads final entrants from the "selected-entrants" subcollection for CSV export.
     * Updates the final entrants adapter, count badge, and prepares data for CSV download.
     */
    private void loadFinalEntrants() {
        finalEntrants.clear();

        db.collection("events")
                .document(eventId)
                .collection("selected-entrants")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Profile> profiles = new ArrayList<>();
                    if (snapshots.isEmpty()) {
                        finalAdapter.setProfiles(profiles);
                        finalCount.setText("0");
                        finalEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    finalCount.setText(String.valueOf(snapshots.size()));
                    finalEmpty.setVisibility(View.GONE);

                    AtomicInteger pending = new AtomicInteger(snapshots.size());

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String userId = doc.getId();
                        profileRepo.getProfile(userId, profile -> {
                            if (profile != null) {
                                profiles.add(profile);
                                finalEntrants.add(new Pair<>(profile.getName(), profile.getEmail()));
                            }
                            if (pending.decrementAndGet() == 0) {
                                requireActivity().runOnUiThread(() -> finalAdapter.setProfiles(profiles));
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SignupList", "Failed to load final entrants", e);
                    finalAdapter.setProfiles(new ArrayList<>());
                    finalCount.setText("0");
                });
    }

    /**
     * Loads cancelled entrants from the "cancelled-entrants" subcollection.
     * Updates the cancelled entrants adapter and count badge.
     */
    private void loadCancelledEntrants() {
        db.collection("events")
                .document(eventId)
                .collection("cancelled-entrants")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Profile> profiles = new ArrayList<>();
                    if (snapshots.isEmpty()) {
                        cancelledAdapter.setProfiles(profiles);
                        cancelledCount.setText("0");
                        cancelledEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    cancelledCount.setText(String.valueOf(snapshots.size()));
                    cancelledEmpty.setVisibility(View.GONE);

                    AtomicInteger pending = new AtomicInteger(snapshots.size());

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String userId = doc.getId();
                        profileRepo.getProfile(userId, profile -> {
                            if (profile != null) profiles.add(profile);
                            if (pending.decrementAndGet() == 0) {
                                requireActivity().runOnUiThread(() -> cancelledAdapter.setProfiles(profiles));
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("SignupList", "Failed to load cancelled entrants", e);
                    cancelledAdapter.setProfiles(new ArrayList<>());
                    cancelledCount.setText("0");
                });
    }
}