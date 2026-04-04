package com.example.syntaxappproject.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.AuthenticationService;
import com.example.syntaxappproject.CoOrganizerRepository;
import com.example.syntaxappproject.EventDetail;
import com.example.syntaxappproject.EventDetailRepository;
import com.example.syntaxappproject.Notification;
import com.example.syntaxappproject.NotificationAdapter;
import com.example.syntaxappproject.NotificationRepository;
import com.example.syntaxappproject.EventJoinRepository;
import com.example.syntaxappproject.Invitation;
import com.example.syntaxappproject.InvitationRepository;
import com.example.syntaxappproject.ProfileRepository;
import com.example.syntaxappproject.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fragment that displays both broadcast notifications and actionable invitations
 * in a single unified list.
 *
 * <p>This fragment serves as the central hub for all user notifications, combining
 * two different types of items:</p>
 * <ul>
 *   <li><b>Broadcast Notifications</b> - Read-only messages sent by organizers or
 *       admins to groups of users (stored in users/{userId}/notifications/).</li>
 *   <li><b>Invitations</b> - Actionable items that require user response (Accept/Decline),
 *       including lottery win invitations and private event invitations (stored in
 *       the invitations collection).</li>
 * </ul>
 *
 * <p>The fragment also provides notification preference toggles, allowing users to
 * opt in/out of receiving notifications from organizers and admins. These preferences
 * are stored in the user's profile and respected when notifications are sent.</p>
 *
 * <p>Items are displayed in reverse chronological order (newest first) with a badge
 * showing the total number of unread items. Invitations show Accept/Decline buttons,
 * while broadcast notifications are read-only.</p>
 *
 * @see Notification
 * @see Invitation
 * @see NotificationRepository
 * @see InvitationRepository
 * @see HomeBar
 */
public class NotificationFragment extends HomeBar {

    /** Service for handling anonymous authentication and user ID retrieval. */
    private final AuthenticationService authService = new AuthenticationService();

    /** Repository for user profile operations and notification preferences. */
    private final ProfileRepository profileRepository = new ProfileRepository();

    /** Repository for broadcast notification CRUD operations. */
    private final NotificationRepository notificationRepository = new NotificationRepository();

    /** Repository for invitation CRUD and response operations. */
    private final InvitationRepository invitationRepository = new InvitationRepository();

    /** Repository for retrieving event details to display invitation context. */
    private final EventDetailRepository eventDetailRepository = new EventDetailRepository();

    /** Switch for toggling organizer notifications on/off. */
    private MaterialSwitch organizerToggle;

    /** Switch for toggling admin notifications on/off. */
    private MaterialSwitch adminToggle;

    /** RecyclerView that displays the combined list of notifications and invitations. */
    private RecyclerView notificationsRecyclerView;

    /** TextView badge showing the total count of unread items. */
    private TextView newBadge;

    /** Header title view for entrance animation. */
    private View headerTitle;

    /** Toggle card view for entrance animation. */
    private View toggleCard;

    /** Notifications card view for entrance animation. */
    private View notificationsCard;

    /** Combined adapter that handles both Notification and Invitation view types. */
    private CombinedNotificationAdapter adapter;

    /**
     * Required empty public constructor for fragment instantiation.
     */
    public NotificationFragment() {}

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
        return inflater.inflate(R.layout.fragment_notification, container, false);
    }

    /**
     * Called immediately after onCreateView. Initializes all UI components,
     * loads notification preferences, loads all items (notifications and invitations),
     * and starts entrance animations.
     *
     * @param view               the inflated view
     * @param savedInstanceState previously saved state, if any
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupHotbar(view);

        organizerToggle = view.findViewById(R.id.organizerToggle);
        adminToggle = view.findViewById(R.id.adminToggle);
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView);
        newBadge = view.findViewById(R.id.newBadge);
        headerTitle = view.findViewById(R.id.headerTitle);
        toggleCard = view.findViewById(R.id.toggleCard);
        notificationsCard = view.findViewById(R.id.notificationsCard);

        adapter = new CombinedNotificationAdapter();
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationsRecyclerView.setAdapter(adapter);

        String userId = authService.getCurrentUserId();
        if (userId != null) {
            loadToggleStates(userId);
            loadAllItems(userId);
        } else {
            newBadge.setVisibility(View.GONE);
        }

        animateIn();
    }

    /**
     * Loads both broadcast notifications and pending invitations for the user.
     * Combines both data sources into a single list and sorts by timestamp.
     *
     * @param userId the ID of the currently authenticated user
     */
    private void loadAllItems(String userId) {
        List<Object> allItems = new ArrayList<>();
        AtomicInteger pendingLoads = new AtomicInteger(2);

        notificationRepository.getNotificationsForUser(userId, notifications -> {
            if (isAdded() && notifications != null) {
                allItems.addAll(notifications);
                Log.d("NotifDebug", "Loaded " + notifications.size() + " broadcast notifications");
            }
            if (pendingLoads.decrementAndGet() == 0) {
                finishLoading(allItems, userId);
            }
        });

        FirebaseFirestore.getInstance()
                .collection("invitations")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;

                    List<Invitation> invitations = new ArrayList<>();
                    for (var doc : snapshots.getDocuments()) {
                        Invitation inv = doc.toObject(Invitation.class);
                        if (inv != null) {
                            inv.setInvitationId(doc.getId());
                            invitations.add(inv);
                        }
                    }

                    if (invitations.isEmpty()) {
                        if (pendingLoads.decrementAndGet() == 0) {
                            finishLoading(allItems, userId);
                        }
                        return;
                    }

                    Map<String, EventDetail> eventCache = new HashMap<>();
                    AtomicInteger pendingFetches = new AtomicInteger(invitations.size());

                    for (Invitation inv : invitations) {
                        eventDetailRepository.getEventDetail(inv.getEventId(), event -> {
                            if (event != null) {
                                eventCache.put(inv.getEventId(), event);
                                inv.setEventName(event.getName() != null ? event.getName() : "Event");
                            } else {
                                inv.setEventName("Event");
                            }
                            allItems.add(inv);

                            if (pendingFetches.decrementAndGet() == 0) {
                                if (pendingLoads.decrementAndGet() == 0) {
                                    finishLoading(allItems, userId);
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("NotifDebug", "Failed to load invitations", e);
                    if (pendingLoads.decrementAndGet() == 0) {
                        finishLoading(allItems, userId);
                    }
                });
    }

    /**
     * Finishes loading by sorting items chronologically and updating the UI.
     *
     * @param items  the combined list of Notification and Invitation objects
     * @param userId the ID of the currently authenticated user
     */
    private void finishLoading(List<Object> items, String userId) {
        if (!isAdded()) return;

        items.sort((a, b) -> {
            long timeA = getTimestamp(a);
            long timeB = getTimestamp(b);
            return Long.compare(timeB, timeA);
        });

        requireActivity().runOnUiThread(() -> {
            int newCount = items.size();
            if (newCount == 0) {
                newBadge.setVisibility(View.GONE);
                notificationsCard.setVisibility(View.GONE);
            } else {
                newBadge.setText(newCount + " new");
                newBadge.setVisibility(View.VISIBLE);
                adapter.setItems(items, userId);
                notificationsCard.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Extracts the timestamp from a Notification or Invitation object for sorting.
     *
     * @param item the Notification or Invitation object
     * @return the timestamp in milliseconds, or 0 if unavailable
     */
    private long getTimestamp(Object item) {
        if (item instanceof Notification) {
            return ((Notification) item).getTimestamp();
        } else if (item instanceof Invitation) {
            Invitation inv = (Invitation) item;
            return inv.getInvitedAt() != null ? inv.getInvitedAt().toDate().getTime() : 0;
        }
        return 0;
    }

    /**
     * Loads the user's notification preference toggle states from Firestore.
     *
     * @param userId the ID of the currently authenticated user
     */
    private void loadToggleStates(String userId) {
        profileRepository.getProfile(userId, profile -> {
            if (profile != null && isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    organizerToggle.setChecked(profile.isOrganizerNotificationEnabled());
                    adminToggle.setChecked(profile.isAdminNotificationEnabled());
                    setupToggleListeners(userId);
                });
            }
        });
    }

    /**
     * Sets up listeners for the notification preference toggles.
     * Saves changes to Firestore when toggles are changed.
     *
     * @param userId the ID of the currently authenticated user
     */
    private void setupToggleListeners(String userId) {
        organizerToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            profileRepository.getProfile(userId, profile -> {
                if (profile != null) {
                    profile.setOrganizerNotificationEnabled(isChecked);
                    profileRepository.updateProfile(userId, profile, success -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (!success) {
                                Toast.makeText(getContext(), "Failed to save preference", Toast.LENGTH_SHORT).show();
                                organizerToggle.setChecked(!isChecked);
                            }
                        });
                    });
                }
            });
        });

        adminToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            profileRepository.getProfile(userId, profile -> {
                if (profile != null) {
                    profile.setAdminNotificationEnabled(isChecked);
                    profileRepository.updateProfile(userId, profile, success -> {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (!success) {
                                Toast.makeText(getContext(), "Failed to save preference", Toast.LENGTH_SHORT).show();
                                adminToggle.setChecked(!isChecked);
                            }
                        });
                    });
                }
            });
        });
    }

    /**
     * Animates the entrance of UI elements with a staggered fade-in effect.
     */
    private void animateIn() {
        View[] views = {headerTitle, toggleCard, notificationsCard};
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

    /**
     * Formats a timestamp into a human-readable string.
     * Shows relative time for recent notifications, date for older ones.
     *
     * @param timestamp the timestamp in milliseconds
     * @return formatted string (e.g., "Just now", "5m ago", "2h ago", "Mar 28")
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) return "";
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 60_000) return "Just now";
        if (diff < 3_600_000) return (diff / 60_000) + "m ago";
        if (diff < 86_400_000) return (diff / 3_600_000) + "h ago";
        return new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date(timestamp));
    }

    /**
     * Inner Adapter class that handles both Notification and Invitation view types
     * within a single RecyclerView.
     *
     * <p>This adapter dynamically determines the view type based on the item class
     * and inflates the appropriate layout. It also handles accept/decline actions
     * for invitation items.</p>
     */
    private class CombinedNotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        /** View type constant for regular Notification items. */
        private static final int TYPE_NOTIFICATION = 0;

        /** View type constant for Invitation items. */
        private static final int TYPE_INVITATION = 1;

        /** Combined list of Notification and Invitation objects. */
        private List<Object> items = new ArrayList<>();

        /** ID of the currently authenticated user. */
        private String userId;

        /** Cache of EventDetail objects keyed by event ID. */
        private Map<String, EventDetail> eventCache = new HashMap<>();

        /**
         * Updates the adapter with a new list of items.
         *
         * @param items  the new list of Notification and Invitation objects
         * @param userId the ID of the currently authenticated user
         */
        public void setItems(List<Object> items, String userId) {
            this.items = items;
            this.userId = userId;
            for (Object item : items) {
                if (item instanceof Invitation) {
                    Invitation inv = (Invitation) item;
                    if (!eventCache.containsKey(inv.getEventId())) {
                        eventDetailRepository.getEventDetail(inv.getEventId(), event -> {
                            if (event != null) {
                                eventCache.put(inv.getEventId(), event);
                                notifyDataSetChanged();
                            }
                        });
                    }
                }
            }
            notifyDataSetChanged();
        }

        /**
         * Returns the view type for the item at the specified position.
         *
         * @param position the position of the item
         * @return TYPE_INVITATION for Invitation objects, TYPE_NOTIFICATION otherwise
         */
        @Override
        public int getItemViewType(int position) {
            return items.get(position) instanceof Invitation ? TYPE_INVITATION : TYPE_NOTIFICATION;
        }

        /**
         * Creates and inflates the appropriate view holder based on view type.
         *
         * @param parent   the parent view group
         * @param viewType the view type (TYPE_NOTIFICATION or TYPE_INVITATION)
         * @return the appropriate ViewHolder instance
         */
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_INVITATION) {
                View view = inflater.inflate(R.layout.item_invitation_card, parent, false);
                return new InvitationViewHolder(view);
            } else {
                View view = inflater.inflate(R.layout.item_notification, parent, false);
                return new NotificationViewHolder(view);
            }
        }

        /**
         * Binds data to the appropriate view holder based on item type.
         *
         * @param holder   the view holder
         * @param position the position of the item
         */
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Object item = items.get(position);

            if (holder instanceof NotificationViewHolder && item instanceof Notification) {
                bindNotification((NotificationViewHolder) holder, (Notification) item);
            } else if (holder instanceof InvitationViewHolder && item instanceof Invitation) {
                bindInvitation((InvitationViewHolder) holder, (Invitation) item, position);
            }
        }

        /**
         * Binds a Notification object to a NotificationViewHolder.
         *
         * @param holder the NotificationViewHolder
         * @param notif  the Notification to display
         */
        private void bindNotification(NotificationViewHolder holder, Notification notif) {
            holder.notifTitle.setText(notif.getTitle());
            holder.senderRole.setText(notif.getSenderRole());
            holder.body.setText(notif.getBody());
            holder.timestamp.setText(formatTimestamp(notif.getTimestamp()));
            holder.unreadDot.setVisibility(View.VISIBLE);
        }

        /**
         * Binds an Invitation object to an InvitationViewHolder.
         * Determines the invitation type (lottery win vs private event) based on
         * the event's privacy flag.
         *
         * @param holder   the InvitationViewHolder
         * @param inv      the Invitation to display
         * @param position the position in the list
         */
        private void bindInvitation(InvitationViewHolder holder, Invitation inv, int position) {
            EventDetail event = eventCache.get(inv.getEventId());
            boolean isPrivateEvent = (event != null && event.isPrivateEvent());

            String title = isPrivateEvent ? "Private Event Invitation" : "You Won the Lottery!";
            String message = isPrivateEvent
                    ? "You've been invited to join the waiting list for " + inv.getEventName()
                    : "Congratulations! You've been selected for " + inv.getEventName();

            holder.titleText.setText(title);
            holder.messageText.setText(message);
            holder.timestamp.setText(formatTimestamp(
                    inv.getInvitedAt() != null ? inv.getInvitedAt().toDate().getTime() : System.currentTimeMillis()
            ));

            holder.acceptButton.setOnClickListener(v -> acceptInvitation(inv, position));
            holder.declineButton.setOnClickListener(v -> declineInvitation(inv, position));
        }

        /**
         * Handles acceptance of an invitation. Determines the invitation type
         * and delegates to the appropriate handler.
         *
         * @param invitation the Invitation to accept
         * @param position   the position in the list
         */
        private void acceptInvitation(Invitation invitation, int position) {
            eventDetailRepository.getEventDetail(invitation.getEventId(), event -> {
                if (event == null) {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                boolean isPrivateEvent = event.isPrivateEvent();

                if (isPrivateEvent) {
                    acceptPrivateEventInvitation(invitation, position);
                } else {
                    acceptLotteryWinInvitation(invitation, position);
                }
            });
        }

        /**
         * Handles acceptance of a private event invitation.
         * Adds the user to the event's waitlist.
         *
         * @param invitation the Invitation to accept
         * @param position   the position in the list
         */
        private void acceptPrivateEventInvitation(Invitation invitation, int position) {
            EventJoinRepository joinRepo = new EventJoinRepository();

            joinRepo.hasJoined(invitation.getEventId(), userId, alreadyJoined -> {
                if (alreadyJoined) {
                    invitationRepository.acceptInvitation(invitation.getInvitationId(), success -> {
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                Toast.makeText(getContext(), "Already on waitlist. Invitation accepted.", Toast.LENGTH_SHORT).show();
                                items.remove(position);
                                notifyItemRemoved(position);
                                updateBadgeCount();
                            }
                        });
                    });
                    return;
                }

                joinRepo.joinEvent(invitation.getEventId(), userId, null, null, new EventJoinRepository.JoinCallback() {
                    @Override
                    public void onComplete(boolean success) {}

                    @Override
                    public void onComplete(boolean success, String message) {
                        requireActivity().runOnUiThread(() -> {
                            if (success) {
                                invitationRepository.acceptInvitation(invitation.getInvitationId(), acceptSuccess -> {
                                    if (acceptSuccess) {
                                        Toast.makeText(getContext(), "Added to waitlist!", Toast.LENGTH_SHORT).show();
                                        items.remove(position);
                                        notifyItemRemoved(position);
                                        updateBadgeCount();
                                    } else {
                                        Toast.makeText(getContext(), "Failed to accept invitation", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(getContext(), message != null ? message : "Failed to join waitlist", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            });
        }

        /**
         * Handles acceptance of a lottery win invitation.
         * Adds the user to the event's selected entrants list.
         *
         * @param invitation the Invitation to accept
         * @param position   the position in the list
         */
        private void acceptLotteryWinInvitation(Invitation invitation, int position) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Confirm Registration")
                    .setMessage("You've been selected for " + invitation.getEventName() + ". Do you want to sign up for this event?")
                    .setPositiveButton("Sign Up", (dialog, which) -> {
                        FirebaseFirestore.getInstance()
                                .collection("events")
                                .document(invitation.getEventId())
                                .collection("selected-entrants")
                                .document(userId)
                                .set(new HashMap<>())
                                .addOnSuccessListener(unused -> {
                                    invitationRepository.acceptInvitation(invitation.getInvitationId(), success -> {
                                        requireActivity().runOnUiThread(() -> {
                                            if (success) {
                                                Toast.makeText(getContext(), "You've been registered for the event!", Toast.LENGTH_SHORT).show();
                                                items.remove(position);
                                                notifyItemRemoved(position);
                                                updateBadgeCount();
                                            }
                                        });
                                    });
                                })
                                .addOnFailureListener(e -> {
                                    requireActivity().runOnUiThread(() ->
                                            Toast.makeText(getContext(), "Failed to register", Toast.LENGTH_SHORT).show()
                                    );
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }

        /**
         * Handles declining an invitation.
         *
         * @param invitation the Invitation to decline
         * @param position   the position in the list
         */
        private void declineInvitation(Invitation invitation, int position) {
            invitationRepository.declineInvitation(invitation.getInvitationId(), success -> {
                requireActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(), "Invitation declined", Toast.LENGTH_SHORT).show();
                        items.remove(position);
                        notifyItemRemoved(position);
                        updateBadgeCount();
                    } else {
                        Toast.makeText(getContext(), "Failed to decline", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        /**
         * Updates the badge count displayed at the top of the fragment.
         * Hides the badge and card when no items remain.
         */
        private void updateBadgeCount() {
            int count = items.size();
            if (count == 0) {
                newBadge.setVisibility(View.GONE);
                notificationsCard.setVisibility(View.GONE);
            } else {
                newBadge.setText(count + " new");
                newBadge.setVisibility(View.VISIBLE);
            }
        }

        /**
         * Returns the total number of items in the adapter.
         *
         * @return the item count
         */
        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    /**
     * ViewHolder for regular broadcast notification items.
     * Displays title, sender role, message body, timestamp, and unread indicator.
     */
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        /** TextView displaying the notification title. */
        TextView notifTitle;

        /** TextView displaying the sender's role (Organizer/Admin). */
        TextView senderRole;

        /** TextView displaying the notification message body. */
        TextView body;

        /** TextView displaying the formatted timestamp. */
        TextView timestamp;

        /** View indicating whether the notification has been read. */
        View unreadDot;

        /**
         * Constructs a NotificationViewHolder and binds child views.
         *
         * @param itemView the inflated item view
         */
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notifTitle = itemView.findViewById(R.id.notifTitle);
            senderRole = itemView.findViewById(R.id.notifSender);
            body = itemView.findViewById(R.id.notifBody);
            timestamp = itemView.findViewById(R.id.notifTimestamp);
            unreadDot = itemView.findViewById(R.id.unreadDot);
        }
    }

    /**
     * ViewHolder for invitation items.
     * Displays invitation details with Accept and Decline buttons.
     */
    static class InvitationViewHolder extends RecyclerView.ViewHolder {
        /** TextView displaying the invitation title (Lottery Win/Private Event). */
        TextView titleText;

        /** TextView displaying the invitation message. */
        TextView messageText;

        /** TextView displaying the formatted timestamp. */
        TextView timestamp;

        /** Button to accept the invitation. */
        MaterialButton acceptButton;

        /** Button to decline the invitation. */
        MaterialButton declineButton;

        /**
         * Constructs an InvitationViewHolder and binds child views.
         *
         * @param itemView the inflated item view
         */
        public InvitationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.invitationTitle);
            messageText = itemView.findViewById(R.id.invitationMessage);
            timestamp = itemView.findViewById(R.id.invitationTimestamp);
            acceptButton = itemView.findViewById(R.id.btnAcceptInvitation);
            declineButton = itemView.findViewById(R.id.btnDeclineInvitation);
        }
    }
}