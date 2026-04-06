package com.example.syntaxappproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * {@link RecyclerView.Adapter} for displaying {@link Notification} objects
 * in the admin browse notifications screen and the user notification inbox.
 *
 * <p>Each row inflates {@code item_notification.xml} and displays:</p>
 * <ul>
 *   <li>The sender label — {@code "From: ADMINISTRATION"} for admin notifications,
 *       or the event name for organizer notifications.</li>
 *   <li>The notification title.</li>
 *   <li>The message body.</li>
 *   <li>A human-readable relative timestamp (e.g. "5m ago", "3h ago", "Mar 28").</li>
 *   <li>An unread dot indicator, always visible.</li>
 * </ul>
 *
 * <p>The adapter does not handle click events — it is a display-only component.
 * Call {@link #setNotifications(List)} to replace the full list and trigger a
 * full refresh via {@link #notifyDataSetChanged()}.</p>
 *
 * @see Notification
 * @see NotificationViewHolder
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    /** The current list of notifications being displayed. */
    private List<Notification> notifications = new ArrayList<>();

    /**
     * Required empty public constructor.
     */
    public NotificationAdapter() {}

    /**
     * Inflates the {@code item_notification} layout and returns a new
     * {@link NotificationViewHolder}.
     *
     * @param parent   the parent {@link ViewGroup}
     * @param viewType unused — only one view type is supported
     * @return a new {@link NotificationViewHolder} wrapping the inflated view
     */
    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    /**
     * Binds a {@link Notification} to the given {@link NotificationViewHolder}.
     *
     * <p>If the notification's {@code senderRole} is {@code "ADMIN"}, the sender
     * label is set to {@code "From: ADMINISTRATION"}. Otherwise the event name is
     * used, falling back to {@code "From: Unknown Event"} if it is null or empty.</p>
     *
     * @param holder   the view holder to bind data into
     * @param position the index of the notification in the list
     */
    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        if ("ADMIN".equals(notification.getSenderRole())) {
            holder.senderEvent.setText("From: ADMINISTRATION");
        } else {
            String eventName = notification.getEventName();
            holder.senderEvent.setText(
                    eventName != null && !eventName.isEmpty()
                            ? "From: " + eventName
                            : "From: Unknown Event"
            );
        }

        holder.notifTitle.setText(notification.getTitle());
        holder.body.setText(notification.getBody());
        holder.timestamp.setText(formatTimestamp(notification.getTimestamp()));
        holder.unreadDot.setVisibility(View.VISIBLE);
    }

    /**
     * Returns the total number of notifications currently held by the adapter.
     *
     * @return the size of the notifications list
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Replaces the current list of notifications with a new one and triggers
     * a full RecyclerView refresh. Passing {@code null} clears the list.
     *
     * @param notifications the new list to display, or {@code null} to clear
     */
    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        try {
            notifyDataSetChanged();
        } catch (Exception e) {
            // No RecyclerView attached — safe to ignore in tests
        }
    }
    List<Notification> getNotificationsForTesting() { //For testing only
        return notifications;
    }
    /**
     * Formats a Unix epoch millisecond timestamp into a human-readable string.
     *
     * <ul>
     *   <li>Less than 1 minute ago → {@code "Just now"}</li>
     *   <li>Less than 1 hour ago → {@code "Xm ago"}</li>
     *   <li>Less than 24 hours ago → {@code "Xh ago"}</li>
     *   <li>Older → {@code "MMM dd"} (e.g. {@code "Mar 28"})</li>
     * </ul>
     *
     * @param timestamp epoch milliseconds
     * @return a formatted human-readable time string, or {@code ""} if timestamp is 0
     */
    private String formatTimestamp(long timestamp) {
        if (timestamp == 0) return "";

        long diff = System.currentTimeMillis() - timestamp;

        if (diff < 60_000)     return "Just now";
        if (diff < 3_600_000)  return (diff / 60_000) + "m ago";
        if (diff < 86_400_000) return (diff / 3_600_000) + "h ago";

        return new SimpleDateFormat("MMM dd", Locale.getDefault())
                .format(new Date(timestamp));
    }

    /**
     * ViewHolder that caches view references for a single notification row.
     *
     * <p>View IDs must match those declared in {@code item_notification.xml}.</p>
     */
    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        /** Displays the notification title. */
        TextView notifTitle;

        /** Displays the sender label (event name or "ADMINISTRATION"). */
        TextView senderEvent;

        /** Displays the notification message body. */
        TextView body;

        /** Displays the formatted relative timestamp. */
        TextView timestamp;

        /** Dot indicator shown for unread notifications. */
        View unreadDot;

        /**
         * Constructs a {@code NotificationViewHolder} and binds child views
         * from the inflated item layout.
         *
         * @param itemView the inflated {@code item_notification} view
         */
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notifTitle  = itemView.findViewById(R.id.notifTitle);
            senderEvent = itemView.findViewById(R.id.notifSender);
            body        = itemView.findViewById(R.id.notifBody);
            timestamp   = itemView.findViewById(R.id.notifTimestamp);
            unreadDot   = itemView.findViewById(R.id.unreadDot);
        }
    }
}