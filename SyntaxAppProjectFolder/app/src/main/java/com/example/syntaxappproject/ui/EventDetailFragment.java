package com.example.syntaxappproject.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.BulletPointHelper;
import com.example.syntaxappproject.CoOrganizerRepository;
import com.example.syntaxappproject.Comment;
import com.example.syntaxappproject.CommentAdapter;
import com.example.syntaxappproject.CommentRepository;
import com.example.syntaxappproject.EventDetail;
import com.example.syntaxappproject.EventDetailRepository;
import com.example.syntaxappproject.EventJoinRepository;
import com.example.syntaxappproject.ImageCacheManager;
import com.example.syntaxappproject.ImageItem;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays full details for a single event, including its poster,
 * QR code, metadata, comments section, and a context-sensitive action button.
 *
 * <p>This fragment supports multiple user roles:</p>
 * <ul>
 *   <li><b>Entrants:</b> Can view event details, join/leave the waiting list,
 *       post comments, and report inappropriate comments.</li>
 *   <li><b>Organizers/Co-organizers:</b> Can manage the event, view entrants,
 *       send notifications, and delete comments.</li>
 *   <li><b>Admins:</b> Can delete any comment across all events.</li>
 * </ul>
 *
 * <p>The action button dynamically changes based on user role and event state:</p>
 * <ul>
 *   <li>Organizers see "Manage Event" (orange button)</li>
 *   <li>Entrants see "Join" or "Leave" (green button) based on waitlist status</li>
 *   <li>Button is disabled if registration period is closed or waitlist is full</li>
 * </ul>
 *
 * <p>Comments are paginated with 10 comments per page. Users can post new comments,
 * delete their own comments (or any if admin/organizer), and report other users' comments.</p>
 *
 * @see EventDetail
 * @see Comment
 * @see CommentAdapter
 * @see EventJoinRepository
 * @see HomeBar
 */
public class EventDetailFragment extends HomeBar {

    /** Number of comments to display per page. */
    private static final int COMMENTS_PER_PAGE = 10;

    /** Firestore document ID of the event being displayed. */
    private String eventId;

    /** Service for handling anonymous authentication. */
    private final AuthenticationService authService = new AuthenticationService();

    /** Repository for join/leave operations on the event waitlist. */
    private final EventJoinRepository joinRepo = new EventJoinRepository();

    /** Repository for comment CRUD operations. */
    private CommentRepository commentRepository;

    /** Adapter for displaying comments in the RecyclerView. */
    private CommentAdapter commentAdapter;

    /** RecyclerView that displays the list of comments. */
    private RecyclerView commentsRecyclerView;

    /** TextView shown when there are no comments. */
    private TextView emptyCommentsText;

    /** Input field for writing new comments. */
    private EditText commentInput;

    /** Button to post a new comment. */
    private MaterialButton postCommentButton;

    /** Button to navigate to the previous page of comments. */
    private MaterialButton prevPageButton;

    /** Button for organizers to send notifications to entrants. */
    private MaterialButton notifyButton;

    /** Button to navigate to the next page of comments. */
    private MaterialButton nextPageButton;

    /** Button for organizers to view the list of event entrants. */
    private MaterialButton viewEntrantsButton;

    /** TextView displaying current page number and total pages. */
    private TextView pageIndicator;

    /** Container layout for pagination controls. */
    private View paginationContainer;

    /** Complete list of all comments for this event. */
    private List<Comment> allComments = new ArrayList<>();

    /** Current page index (0-based). */
    private int currentPage = 0;

    /** Total number of pages based on comment count. */
    private int totalPages = 0;

    /** Flag to enable test mode (bypasses authentication and Firestore). */
    public boolean testingMode = false;

    /** UID of the currently authenticated user. */
    private String uid;

    /** Whether the current user is an organizer for this event. */
    private boolean isOrganizer = false;

    /** Whether the current user has admin privileges. */
    private boolean isAdmin = false;

    /** Counter for async profile loading to know when both profiles are loaded. */
    private int profilesLoaded = 0;

    /** Whether the current user is a co-organizer for this event. */
    private boolean isCoOrganizer = false;

    /** Client for accessing the device's last known location. */
    private FusedLocationProviderClient fusedLocationClient;

    /** Launcher for requesting location permissions. */
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    /** Reference to the join button for UI updates after async operations. */
    private MaterialButton joinButtonToUpdate;

    /** Reference to the waitlist count TextView for UI updates. */
    private TextView wLCountToUpdate;

    /**
     * Called when the fragment is created. Initializes the location client
     * and sets up the permission request launcher.
     *
     * @param savedInstanceState previously saved instance state, if any
     */
    private String loadedEventName;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        locationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationAllowed = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationAllowed = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    if (fineLocationAllowed || coarseLocationAllowed) {
                        attemptJoinWithLocation();
                    } else {
                        new EventDetailRepository().getEventDetail(eventId, event -> {
                            if (event != null && event.isGeoReq()) {
                                Toast.makeText(getContext(), "Location permission is required to join this event", Toast.LENGTH_SHORT).show();
                            } else {
                                performJoin(null, null);
                            }
                        });
                    }
                }
        );
    }

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater           the layout inflater
     * @param container          the parent view group
     * @param savedInstanceState previously saved state, if any
     * @return the inflated view
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_detail, container, false);
    }

    /**
     * Called immediately after onCreateView. Initializes all UI components,
     * loads event data, sets up animations, and configures click listeners.
     *
     * @param view               the inflated view
     * @param savedInstanceState previously saved state, if any
     */
    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            testingMode = getArguments().getBoolean("testingMode", false);
        }
        if (!testingMode) super.onViewCreated(view, savedInstanceState);
        if (!testingMode) setupHotbar(view);

        ImageView eventPoster     = view.findViewById(R.id.eventPoster);
        ImageView eventQRCode     = view.findViewById(R.id.eventQRCode);
        TextView eventName        = view.findViewById(R.id.eventName);
        TextView description      = view.findViewById(R.id.eventDescription);
        TextView date             = view.findViewById(R.id.eventDate);
        TextView locationText     = view.findViewById(R.id.eventLocation);
        TextView regiPeriod       = view.findViewById(R.id.eventRegiPeriod);
        TextView capacity         = view.findViewById(R.id.eventCapacity);
        TextView wLCount          = view.findViewById(R.id.eventWLCount);
        TextView lotteryCriteria  = view.findViewById(R.id.eventLotteryCriteria);
        MaterialButton joinButton = view.findViewById(R.id.joinButton);
        MaterialButton doneButton = view.findViewById(R.id.doneButton);
        MaterialButton mapButton  = view.findViewById(R.id.mapButton);
        viewEntrantsButton        = view.findViewById(R.id.viewEntrantsButton);
        View headerTitle          = view.findViewById(R.id.headerTitle);
        View posterCard           = view.findViewById(R.id.posterCard);
        View nameCard             = view.findViewById(R.id.nameCard);
        View detailsCard          = view.findViewById(R.id.detailsCard);
        View actionCard           = view.findViewById(R.id.actionCard);
        View commentsCard         = view.findViewById(R.id.commentsCard);
        View notifyCard           = view.findViewById(R.id.notifyCard);
        commentsRecyclerView      = view.findViewById(R.id.commentsRecyclerView);
        emptyCommentsText         = view.findViewById(R.id.emptyCommentsText);
        commentInput              = view.findViewById(R.id.commentInput);
        postCommentButton         = view.findViewById(R.id.postCommentButton);
        prevPageButton            = view.findViewById(R.id.prevPageButton);
        nextPageButton            = view.findViewById(R.id.nextPageButton);
        pageIndicator             = view.findViewById(R.id.pageIndicator);
        paginationContainer       = view.findViewById(R.id.paginationContainer);
        notifyButton              = view.findViewById(R.id.notifyEntrantsButton);

        animateViews(headerTitle, posterCard, nameCard, detailsCard, commentsCard, actionCard, notifyCard);

        doneButton.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        mapButton.setVisibility(View.GONE);
        mapButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);
            Navigation.findNavController(v).navigate(R.id.to_mapFragment, bundle);
        });

        uid = authService.getCurrentUserId();
        if (testingMode && uid == null) uid = "test_user";

        setupCommentsSection();

        loadUserRoles();

        loadComments();
        postCommentButton.setOnClickListener(v -> postComment());

        if (testingMode) {
            setupTestingMode(joinButton, wLCount);
            return;
        }

        loadEventDetails(eventPoster, eventQRCode, eventName, description, date, locationText,
                regiPeriod, capacity, wLCount, lotteryCriteria, joinButton, notifyCard, notifyButton, mapButton);
    }

    /**
     * Animates the entrance of the header and card views with fade-in and slide-up effects.
     *
     * @param views the views to animate
     */
    private void animateViews(View... views) {
        for (View v : views) {
            if (v != null && v.getId() != R.id.notifyCard) {
                v.setAlpha(0f);
                v.setTranslationY(30f);
                v.setVisibility(View.VISIBLE);
                v.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();
            }
        }
    }

    /**
     * Initializes the comments RecyclerView and adapter.
     */
    private void setupCommentsSection() {
        commentRepository = new CommentRepository();
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        commentAdapter = new CommentAdapter(uid, isOrganizer, isAdmin, this::deleteComment, this::reportComment);
        commentsRecyclerView.setAdapter(commentAdapter);
    }

    /**
     * Loads user role information (admin and organizer status) asynchronously.
     * Updates the comment adapter when both profiles are loaded.
     */
    private void loadUserRoles() {
        new ProfileRepository().getProfile(uid, profile -> {
            if (profile != null) {
                isAdmin = profile.isAdmin();
            }
            checkAndUpdateAdapter();
        });

        new EventDetailRepository().getEventDetail(eventId, event -> {
            if (event != null && uid != null) {
                isOrganizer = uid.equals(event.getOrganizerUid());
            }
            checkAndUpdateAdapter();
        });
    }

    /**
     * Checks if both profile and event data have been loaded, then updates the adapter.
     */
    private void checkAndUpdateAdapter() {
        profilesLoaded++;
        if (profilesLoaded == 2) {
            requireActivity().runOnUiThread(() -> {
                commentAdapter.updateRoles(isOrganizer, isAdmin);
                displayCurrentPage();
            });
        }
    }

    /**
     * Loads comments for this event from Firestore and updates the UI.
     */
    private void loadComments() {
        Log.d("EventDetailFragment", "Loading comments for event: " + eventId);
        commentRepository.getCommentsForEvent(eventId, comments -> {
            Log.d("EventDetailFragment", "Comments received: " + (comments != null ? comments.size() : 0));
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                allComments = comments != null ? comments : new ArrayList<>();

                if (allComments.isEmpty()) {
                    emptyCommentsText.setVisibility(View.VISIBLE);
                    commentsRecyclerView.setVisibility(View.GONE);
                    paginationContainer.setVisibility(View.GONE);
                } else {
                    emptyCommentsText.setVisibility(View.GONE);
                    commentsRecyclerView.setVisibility(View.VISIBLE);

                    totalPages = (int) Math.ceil((double) allComments.size() / COMMENTS_PER_PAGE);
                    currentPage = 0;

                    displayCurrentPage();
                }
            });
        });
    }

    /**
     * Displays the current page of comments in the RecyclerView.
     * Updates pagination controls based on total pages.
     */
    private void displayCurrentPage() {
        int start = currentPage * COMMENTS_PER_PAGE;
        int end = Math.min(start + COMMENTS_PER_PAGE, allComments.size());

        List<Comment> pageComments = allComments.subList(start, end);
        commentAdapter.setComments(pageComments);

        if (totalPages > 1) {
            paginationContainer.setVisibility(View.VISIBLE);
            pageIndicator.setText("Page " + (currentPage + 1) + " of " + totalPages);

            prevPageButton.setVisibility(currentPage > 0 ? View.VISIBLE : View.INVISIBLE);
            nextPageButton.setVisibility(currentPage < totalPages - 1 ? View.VISIBLE : View.INVISIBLE);
        } else {
            paginationContainer.setVisibility(View.GONE);
        }

        commentsRecyclerView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        commentsRecyclerView.requestLayout();
    }

    /**
     * Posts a new comment to Firestore.
     * Validates input and retrieves user profile information before posting.
     */
    private void postComment() {
        String commentText = commentInput.getText() != null ?
                commentInput.getText().toString().trim() : "";

        if (commentText.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        if (uid == null) {
            Toast.makeText(requireContext(), "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }

        new ProfileRepository().getProfile(uid, profile -> {
            if (!isAdded()) return;
            String userName = profile != null && profile.getName() != null ?
                    profile.getName() : "Anonymous";
            String deviceId = profile != null && profile.getDeviceId() != null ?
                    profile.getDeviceId() : "";

            Comment comment = new Comment(eventId, commentText, uid, userName, deviceId);

            commentRepository.addComment(comment, success -> {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        commentInput.setText("");
                        loadComments();
                        Toast.makeText(requireContext(), "Comment posted!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Failed to post comment", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    /**
     * Deletes a comment from Firestore after user confirmation.
     * Available to admins, organizers, and the comment's author.
     *
     * @param comment the comment to delete
     */
    private void deleteComment(Comment comment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Comment")
                .setMessage("Are you sure you want to delete this comment?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    commentRepository.deleteComment(comment.getCommentId(), success -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                loadComments();
                                Toast.makeText(requireContext(), "Comment deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(requireContext(), "Failed to delete comment", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Reports a comment to administrators.
     * Increments the report count and adds the reporting user to the reportedBy list.
     *
     * @param comment the comment to report
     */
    private void reportComment(Comment comment) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Report Comment")
                .setMessage("Are you sure you want to report this comment?")
                .setPositiveButton("Report", (dialog, which) -> {
                    commentRepository.reportComment(comment.getCommentId(), uid, success -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(requireContext(), "Comment reported. Thank you for helping keep our community safe.", Toast.LENGTH_LONG).show();
                                if (comment.getReportedBy() == null) {
                                    comment.setReportedBy(new ArrayList<>());
                                }
                                comment.getReportedBy().add(uid);
                                comment.setReportCount(comment.getReportCount() + 1);
                                commentAdapter.notifyDataSetChanged();
                            } else {
                                Toast.makeText(requireContext(), "Failed to report comment", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Loads the event poster image from cache or Firebase Realtime Database.
     *
     * @param eventPoster the ImageView to display the poster in
     */
    private void loadPoster(ImageView eventPoster) {
        if (ImageCacheManager.has(eventId)) {
            eventPoster.setImageBitmap(ImageCacheManager.get(eventId));
            return;
        }
        ImageItem.fetchByEventId(eventId, new ImageItem.ImageCallback() {
            @Override
            public void onImageLoaded(ImageItem imageItem) {
                if (imageItem == null || imageItem.imageUrl == null) return;
                new Thread(() -> {
                    try {
                        byte[] decoded = Base64.decode(imageItem.imageUrl, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                        if (bitmap == null || !isAdded()) return;
                        ImageCacheManager.put(eventId, bitmap);
                        requireActivity().runOnUiThread(() -> eventPoster.setImageBitmap(bitmap));
                    } catch (Exception ignored) {}
                }).start();
            }
            @Override
            public void onError(Exception e) {}
        });
    }

    /**
     * Loads the event QR code image from Firebase Realtime Database.
     *
     * @param eventQRCode the ImageView to display the QR code in
     */
    private void loadQRCode(ImageView eventQRCode) {
        FirebaseDatabase.getInstance()
                .getReference("event_qr_codes")
                .child(eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot == null || !snapshot.exists() || !isAdded()) return;
                    String base64 = snapshot.child("image").getValue(String.class);
                    if (base64 == null) return;
                    new Thread(() -> {
                        try {
                            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            if (bitmap == null || !isAdded()) return;
                            requireActivity().runOnUiThread(() -> eventQRCode.setImageBitmap(bitmap));
                        } catch (Exception ignored) {}
                    }).start();
                });
    }

    /**
     * Configures the action button based on user role and event state.
     *
     * @param joinButton   the action button
     * @param wLCount      TextView showing waitlist count
     * @param event        the EventDetail object
     * @param notifyCard   the notification card view
     * @param notifyButton the notification button
     * @param mapButton    the map button
     */
    private void configureActionButton(MaterialButton joinButton, TextView wLCount, EventDetail event,
                                       View notifyCard, MaterialButton notifyButton, MaterialButton mapButton) {
        boolean isMainOrganizer = uid != null && uid.equals(event.getOrganizerUid());
        boolean isEventOrganizer = isMainOrganizer || isCoOrganizer;

        if (isEventOrganizer) {
            joinButton.setText("Manage Event");
            joinButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF9800")));
            joinButton.setOnClickListener(v -> navigateToManageEvent());
            mapButton.setVisibility(View.VISIBLE);
            viewEntrantsButton.setVisibility(View.VISIBLE);
            viewEntrantsButton.setOnClickListener(v -> {
                Bundle bundle = new Bundle();
                bundle.putString("eventId", eventId);
                Navigation.findNavController(v).navigate(R.id.toEventSignupList, bundle);
            });
            notifyCard.setAlpha(1f);
            notifyCard.setVisibility(View.VISIBLE);
            notifyButton.setOnClickListener(v -> navigateToNotifyEntrants());
            return;
        }

        mapButton.setVisibility(View.GONE);
        viewEntrantsButton.setVisibility(View.GONE);

        long now = System.currentTimeMillis();
        long regStart = parseDateMillis(event.getStartingRegistrationPeriod());
        long regEnd = parseDateMillis(event.getEndingRegistrationPeriod());
        boolean inWindow = regStart != -1 && regEnd != -1 && now >= regStart && now <= regEnd;

        if (!inWindow) {
            joinButton.setText(now < regStart ? "Registration Not Open" : "Registration Closed");
            joinButton.setAlpha(0.5f);
            joinButton.setEnabled(false);
            return;
        }

        joinRepo.hasJoined(eventId, uid, joined ->
                requireActivity().runOnUiThread(() -> joinButton.setText(joined ? "Leave" : "Join"))
        );
        joinButton.setEnabled(true);
        joinButton.setAlpha(1f);
        joinButton.setOnClickListener(v -> handleJoinLeave(joinButton, wLCount));
    }

    /**
     * Navigates to the Manage Event screen for organizers.
     */
    private void navigateToManageEvent() {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", eventId);
        Navigation.findNavController(requireView()).navigate(R.id.manageEventFragment, bundle);
    }

    /**
     * Navigates to the Notify Entrants screen for organizers.
     */
    private void navigateToNotifyEntrants() {
        Bundle bundle = new Bundle();
        bundle.putString("eventId", eventId);
        bundle.putString("eventName", loadedEventName);
        Navigation.findNavController(requireView()).navigate(R.id.notifyEntrantsFragment, bundle);
    }

    /**
     * Handles the join/leave button click logic.
     * Checks capacity, registration window, and user's current status.
     *
     * @param joinButton the action button
     * @param wLCount    TextView showing waitlist count
     */
    private void handleJoinLeave(MaterialButton joinButton, TextView wLCount) {
        if (uid == null) {
            Toast.makeText(getContext(), "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        joinButtonToUpdate = joinButton;
        wLCountToUpdate = wLCount;

        new EventDetailRepository().getEventDetail(eventId, freshEvent -> {
            if (!isAdded()) return;
            long eventCapacity = freshEvent.getCapacity();
            long waitlistCount = freshEvent.getWaitlistCount();

            joinRepo.hasJoined(eventId, uid, joined -> requireActivity().runOnUiThread(() -> {
                if (joined) {
                    joinRepo.leaveEvent(eventId, uid, success -> requireActivity().runOnUiThread(() -> {
                        if (success) {
                            joinButton.setText("Join");
                            joinButton.setAlpha(1f);
                            joinButton.setEnabled(true);
                            int c = Integer.parseInt(wLCount.getText().toString().replaceAll("[^0-9]", ""));
                            wLCount.setText("Waitlist: " + (c - 1));
                            Toast.makeText(getContext(), "You left the event", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to leave", Toast.LENGTH_SHORT).show();
                        }
                    }));
                } else {
                    if (eventCapacity > 0 && waitlistCount >= eventCapacity) {
                        joinButton.setAlpha(0.5f);
                        joinButton.setText("Waitlist Full");
                        joinButton.setEnabled(false);
                        Toast.makeText(getContext(), "Event is at capacity", Toast.LENGTH_SHORT).show();
                    } else {
                        checkLocationAndJoin();
                    }
                }
            }));
        });
    }

    /**
     * Checks location permission and requests it if not granted,
     * then proceeds to join the event.
     */
    private void checkLocationAndJoin() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            attemptJoinWithLocation();
        } else {
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    /**
     * Attempts to get the user's current location and joins the event.
     * If location is unavailable, proceeds with null location.
     */
    @SuppressLint("MissingPermission")
    private void attemptJoinWithLocation() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                joinRepo.setContext(getContext());
                performJoin(location.getLatitude(), location.getLongitude());
            } else {
                joinRepo.setContext(getContext());
                performJoin(null, null);
            }
        }).addOnFailureListener(e -> {
            joinRepo.setContext(getContext());
            performJoin(null, null);
        });
    }

    /**
     * Performs the actual join operation by calling the repository.
     *
     * @param lat user's latitude (may be null)
     * @param lon user's longitude (may be null)
     */
    private void performJoin(Double lat, Double lon) {
        joinRepo.joinEvent(eventId, uid, lat, lon, success -> requireActivity().runOnUiThread(() -> {
            if (success) {
                if (joinButtonToUpdate != null) joinButtonToUpdate.setText("Leave");
                if (wLCountToUpdate != null) {
                    int c = Integer.parseInt(wLCountToUpdate.getText().toString().replaceAll("[^0-9]", ""));
                    wLCountToUpdate.setText("Waitlist: " + (c + 1));
                }
                Toast.makeText(getContext(), "Successfully joined!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Join failed", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    /**
     * Parses a date string into milliseconds since epoch.
     * Supports formats "yyyy-MM-dd" and "MM/dd/yyyy".
     *
     * @param dateStr the date string to parse
     * @return milliseconds since epoch, or -1 if parsing fails
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
     * Sets up testing mode with mock join/leave behavior.
     *
     * @param joinButton the action button
     * @param wLCount    TextView showing waitlist count
     */
    private void setupTestingMode(MaterialButton joinButton, TextView wLCount) {
        joinButton.setOnClickListener(v -> {
            if (uid == null) return;
            String currentText = wLCount.getText().toString();
            int currentCount = Integer.parseInt(currentText.replaceAll("[^0-9]", ""));
            if ("Join".equals(joinButton.getText().toString())) {
                joinButton.setText("Leave");
                wLCount.setText("Waitlist: " + (currentCount + 1));
            } else {
                joinButton.setText("Join");
                wLCount.setText("Waitlist: " + (currentCount - 1));
            }
        });
    }

    /**
     * Loads event details from Firestore and populates the UI.
     *
     * @param eventPoster    ImageView for event poster
     * @param eventQRCode    ImageView for QR code
     * @param eventName      TextView for event name
     * @param description    TextView for event description
     * @param date           TextView for event date
     * @param locationText   TextView for event location
     * @param regiPeriod     TextView for registration period
     * @param capacity       TextView for event capacity
     * @param wLCount        TextView for waitlist count
     * @param lotteryCriteria TextView for lottery criteria
     * @param joinButton     the action button
     * @param notifyCard     the notification card view
     * @param notifyButton   the notification button
     * @param mapButton      the map button
     */
    private void loadEventDetails(ImageView eventPoster, ImageView eventQRCode, TextView eventName,
                                  TextView description, TextView date, TextView locationText,
                                  TextView regiPeriod, TextView capacity, TextView wLCount,
                                  TextView lotteryCriteria, MaterialButton joinButton,
                                  View notifyCard, MaterialButton notifyButton, MaterialButton mapButton) {
        new EventDetailRepository().getEventDetail(eventId, event -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                eventName.setText(event.getName());
                loadedEventName = event.getName();
                description.setText(event.getDescription());
                date.setText(event.getStartingEventDate());
                locationText.setText(event.getLocation());
                regiPeriod.setText(event.getStartingRegistrationPeriod());
                capacity.setText("Capacity: " + event.getCapacity());
                wLCount.setText("Waitlist: " + event.getWaitlistCount());

                String criteria = event.getLotteryCriteria();
                if (criteria != null && !criteria.isEmpty()) {
                    List<String> criteriaList = BulletPointHelper.parseBulletPoints(criteria);
                    if (!criteriaList.isEmpty()) {
                        StringBuilder displayText = new StringBuilder();
                        for (String point : criteriaList) {
                            displayText.append("• ").append(point).append("\n");
                        }
                        lotteryCriteria.setText(displayText.toString().trim());
                        lotteryCriteria.setVisibility(View.VISIBLE);
                    } else {
                        lotteryCriteria.setVisibility(View.GONE);
                    }
                } else {
                    lotteryCriteria.setVisibility(View.GONE);
                }

                loadPoster(eventPoster);
                loadQRCode(eventQRCode);
                new CoOrganizerRepository().isCoOrganizer(eventId, uid, coOrganizer -> {
                    if (event != null && uid != null) {
                        isCoOrganizer = coOrganizer;
                    }
                    if (coOrganizer) {
                        isOrganizer = coOrganizer;
                    }
                    checkAndUpdateAdapter();
                    configureActionButton(joinButton, wLCount, event, notifyCard, notifyButton, mapButton);
                });
            });
        });
    }
}