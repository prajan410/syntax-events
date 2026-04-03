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
import java.util.Date;
import java.util.List;

/**
 * Admin-only fragment: browse all notifications sent for an event,
 * and send a global notification to ALL entrants.
 */
public class AdminBrowseNotificationsFragment extends Fragment {

    private static final String ARG_EVENT_ID = "eventId";

    private final AuthenticationService authService      = new AuthenticationService();
    private final NotificationRepository notifRepository = new NotificationRepository();

    private String eventId;

    // ── Views ────────────────────────────────────────────────────────────────
    private View headerTitle;
    private View sendPanelCard;
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ProgressBar loadingSpinner;
    private TextView emptyText;
    private TextView notifCountBadge;
    private EditText notifTitleInput;
    private EditText notifMessageInput;
    private MaterialButton sendNotifBtn;
    private MaterialButton doneButton;

    // ── Factory ──────────────────────────────────────────────────────────────

    public static AdminBrowseNotificationsFragment newInstance(String eventId) {
        AdminBrowseNotificationsFragment f = new AdminBrowseNotificationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        f.setArguments(args);
        return f;
    }

    public AdminBrowseNotificationsFragment() {}

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_browse_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            eventId = getArguments().getString(ARG_EVENT_ID);
        }

        Log.d("NotifDebug", "eventId on load = " + eventId);

        if (eventId == null) {
            Toast.makeText(getContext(), "No event ID provided", Toast.LENGTH_SHORT).show();
            return;
        }

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

    private void setupRecycler() {
        adapter = new NotificationAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    // ── Load ──────────────────────────────────────────────────────────────────

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

    // ── Send (ALL entrants only) ───────────────────────────────────────────────

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
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(getContext(), "No event selected", Toast.LENGTH_SHORT).show();
                return;
            }

            Notification notification = new Notification();
            notification.setEventId(eventId);
            notification.setSenderId(authService.getCurrentUserId());
            notification.setTitle(title);
            notification.setBody(message);
            notification.setSenderRole("ADMIN");
            notification.setTargetGroup("ALL");
            notification.setTimestamp(System.currentTimeMillis());
            notification.setStatus("SENT");

            List<String> targetGroups = new ArrayList<>();
            targetGroups.add("ALL");

            sendNotifBtn.setEnabled(false);
            sendNotifBtn.setText("Sending…");

            notifRepository.sendNotification(notification, eventId, targetGroups, success -> {
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

    private void showLoading(boolean show) {
        loadingSpinner.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyText.setVisibility(View.GONE);
    }

    private void showEmpty(boolean show) {
        emptyText.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    // ── Animation ─────────────────────────────────────────────────────────────

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