package com.example.syntaxappproject.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.Notification;
import com.example.syntaxappproject.NotificationAdapter;
import com.example.syntaxappproject.NotificationRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Admin-only fragment that provides two capabilities:
 * <ol>
 *   <li>Browsing all notifications ever sent across all events, displayed
 *       in a scrollable list ordered by timestamp descending.</li>
 *   <li>Composing and sending a global admin notification to every user
 *       in the system who has not opted out of admin notifications.</li>
 * </ol>
 *
 * <p>This fragment does not require an event ID — it operates at the
 * platform level. All sent notifications use {@code eventId = "ADMINISTRATION"}
 * and {@code senderRole = "ADMIN"}, and are delivered to all users via
 * {@link NotificationRepository#sendNotification}.</p>
 *
 * <p>The send button is disabled while a send is in progress to prevent
 * duplicate submissions.</p>
 *
 * @see NotificationRepository
 * @see NotificationAdapter
 */
public class AdminBrowseNotificationsFragment extends Fragment {

    /** Bundle argument key for the event ID (unused in global admin mode). */
    private static final String ARG_EVENT_ID = "eventId";

    /** Service for retrieving the current authenticated user's ID. */
    private final AuthenticationService authService = new AuthenticationService();

    /** Repository for reading and writing notification data in Firestore. */
    private final NotificationRepository notifRepository = new NotificationRepository();

    /** Event ID associated with this fragment instance (unused for global admin sends). */
    private String eventId;

    // ── Views ────────────────────────────────────────────────────────────────

    /** Header title view used for entrance animation. */
    private View headerTitle;

    /** Card view containing the compose and send UI. */
    private View sendPanelCard;

    /** RecyclerView displaying the list of all sent notifications. */
    private RecyclerView recyclerView;

    /** Adapter that binds notification data to the RecyclerView. */
    private NotificationAdapter adapter;

    /** Progress indicator shown while notifications are loading. */
    private ProgressBar loadingSpinner;

    /** TextView shown when no notifications have been sent yet. */
    private TextView emptyText;

    /** TextView showing the total count of loaded notifications. */
    private TextView notifCountBadge;

    /** Input field for the notification title. */
    private EditText notifTitleInput;

    /** Input field for the notification message body. */
    private EditText notifMessageInput;

    /** Button that triggers the global notification send. */
    private MaterialButton sendNotifBtn;

    /** Button that navigates back to the previous screen. */
    private MaterialButton doneButton;

    // ── Factory ──────────────────────────────────────────────────────────────

    /**
     * Creates a new instance of {@code AdminBrowseNotificationsFragment}
     * with the given event ID stored in its arguments bundle.
     *
     * @param eventId the Firestore document ID of the event (unused in global admin mode)
     * @return a new fragment instance
     */
    public static AdminBrowseNotificationsFragment newInstance(String eventId) {
        AdminBrowseNotificationsFragment f = new AdminBrowseNotificationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        f.setArguments(args);
        return f;
    }

    /**
     * Required empty public constructor for fragment instantiation.
     */
    public AdminBrowseNotificationsFragment() {}

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /**
     * Inflates the layout for this fragment.
     *
     * @param inflater           the layout inflater
     * @param container          the parent view group
     * @param savedInstanceState previously saved state, if any
     * @return the inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_browse_notifications, container, false);
    }

    /**
     * Called immediately after {@link #onCreateView}. Binds all views,
     * sets up the RecyclerView, configures the send button, attaches the
     * done button listener, loads existing notifications, and starts
     * entrance animations.
     *
     * @param view               the inflated view
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupRecycler();
        setupSendButton();
        doneButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            }
        });
        loadNotifications();
        animateIn();
    }

    // ── Bind ──────────────────────────────────────────────────────────────────

    /**
     * Binds all child views from the inflated layout to their corresponding
     * field references.
     *
     * @param view the root view of the fragment layout
     */
    private void bindViews(View view) {
        headerTitle       = view.findViewById(R.id.headerTitle);
        sendPanelCard     = view.findViewById(R.id.sendPanelCard);
        notifCountBadge   = view.findViewById(R.id.notifCountBadge);
        recyclerView      = view.findViewById(R.id.recycler_notifications);
        loadingSpinner    = view.findViewById(R.id.loadingSpinner);
        emptyText         = view.findViewById(R.id.emptyText);
        notifTitleInput   = view.findViewById(R.id.notifTitleInput);
        notifMessageInput = view.findViewById(R.id.notifMessageInput);
        sendNotifBtn      = view.findViewById(R.id.sendNotifBtn);
        doneButton        = view.findViewById(R.id.doneButton);
    }

    // ── RecyclerView ──────────────────────────────────────────────────────────

    /**
     * Initializes the {@link RecyclerView} with a {@link LinearLayoutManager}
     * and attaches the {@link NotificationAdapter}.
     */
    private void setupRecycler() {
        adapter = new NotificationAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    /**
     * Fetches all notifications from Firestore via
     * {@link NotificationRepository#getAllNotifications} and updates the UI.
     * Shows a loading spinner during the fetch, and displays an empty state
     * if no notifications exist.
     */
    private void loadNotifications() {
        showLoading(true);
        notifRepository.getAllNotifications(notifications -> {
            if (!isAdded()) return;
            requireActivity().runOnUiThread(() -> {
                showLoading(false);
                if (notifications == null || notifications.isEmpty()) {
                    showEmpty(true);
                    notifCountBadge.setText("0 notifications");
                    return;
                }
                adapter.setNotifications(notifications);
                notifCountBadge.setText(notifications.size() + " notification" + (notifications.size() == 1 ? "" : "s"));
                showEmpty(false);
            });
        });
    }

    // ── Send ──────────────────────────────────────────────────────────────────

    /**
     * Configures the send button click listener. On click, validates the title
     * and message inputs, builds a {@link Notification} with
     * {@code senderRole = "ADMIN"}, {@code eventId = "ADMINISTRATION"}, and
     * {@code targetGroup = "ALL"}, then dispatches it to all users via
     * {@link NotificationRepository#sendNotification}.
     *
     * <p>The button is disabled and its label changed to "Sending…" while the
     * request is in flight to prevent duplicate submissions. It is re-enabled
     * on both success and failure.</p>
     */
    private void setupSendButton() {
        sendNotifBtn.setOnClickListener(v -> {
            String title   = notifTitleInput.getText().toString().trim();
            String message = notifMessageInput.getText().toString().trim();

            if (title.isEmpty()) {
                notifTitleInput.setError("Title required");
                return;
            }
            if (message.isEmpty()) {
                notifMessageInput.setError("Message required");
                return;
            }

            Notification notification = new Notification();
            notification.setSenderId(authService.getCurrentUserId());
            notification.setTitle(title);
            notification.setSenderRole("ADMIN");
            notification.setBody(message);
            notification.setEventId("ADMINISTRATION");
            notification.setTargetGroup("ALL");
            notification.setTimestamp(System.currentTimeMillis());
            notification.setStatus("SENT");

            List<String> targetGroups = new ArrayList<>();
            targetGroups.add("ALL");

            sendNotifBtn.setEnabled(false);
            sendNotifBtn.setText("Sending…");

            notifRepository.sendNotification(notification, "ADMINISTRATION", targetGroups, success -> {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    sendNotifBtn.setEnabled(true);
                    sendNotifBtn.setText("Send to All Entrants");
                    if (success) {
                        notifTitleInput.setText("");
                        notifMessageInput.setText("");
                        Toast.makeText(getContext(), "Notification sent to all entrants", Toast.LENGTH_SHORT).show();
                        loadNotifications();
                    } else {
                        Toast.makeText(getContext(), "Failed to send notification", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    /**
     * Toggles the loading state of the fragment. Shows the progress spinner
     * and hides the RecyclerView while loading, and vice versa.
     *
     * @param show {@code true} to show the loading spinner, {@code false} to hide it
     */
    private void showLoading(boolean show) {
        loadingSpinner.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyText.setVisibility(View.GONE);
    }

    /**
     * Toggles the empty state of the fragment. Shows the empty text view
     * and hides the RecyclerView when there are no notifications.
     *
     * @param show {@code true} to show the empty state, {@code false} to show the list
     */
    private void showEmpty(boolean show) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ── Animation ─────────────────────────────────────────────────────────────

    /**
     * Animates the header title and send panel card into view using a
     * staggered fade-in effect with an 80ms delay between each element.
     */
    private void animateIn() {
        View[] views = {headerTitle, sendPanelCard};
        AnimatorSet set = new AnimatorSet();
        android.animation.Animator[] animators = new android.animation.Animator[views.length];
        for (int i = 0; i < views.length; i++) {
            ObjectAnimator fade = ObjectAnimator.ofFloat(views[i], "alpha", 0f, 1f);
            fade.setStartDelay(i * 80L);
            fade.setDuration(300);
            animators[i] = fade;
        }
        set.playTogether(animators);
        set.start();
    }
}