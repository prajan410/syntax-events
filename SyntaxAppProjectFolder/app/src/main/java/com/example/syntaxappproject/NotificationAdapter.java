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
 * RecyclerView adapter for displaying notifications in
 * {@link com.example.syntaxappproject.ui.NotificationFragment}.
 *
 * <p>Each row shows the event name, sender role, message body,
 * timestamp, and an unread dot indicator.</p>
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications = new ArrayList<>();

    public NotificationAdapter() {}

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);

        holder.notifTitle.setText(notification.getTitle());
        holder.senderRole.setText(notification.getSenderRole());
        holder.body.setText(notification.getBody());
        holder.timestamp.setText(formatTimestamp(notification.getTimestamp()));

        // Unread dot — always visible for now
        // TODO: track read/unread state on the Notification object
        // and toggle dot visibility: holder.unreadDot.setVisibility(isRead ? View.GONE : View.VISIBLE)
        holder.unreadDot.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Replaces the full list and refreshes the RecyclerView.
     * Called from NotificationFragment after loading from Firestore.
     *
     * @param notifications the new list to display
     */
    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * Formats an epoch millisecond timestamp into a human-readable string.
     * Shows relative time for recent notifications, date for older ones.
     *
     * @param timestamp epoch milliseconds
     * @return formatted string e.g. "2m ago", "3h ago", "Mar 28"
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
     * ViewHolder that caches references to views in each notification row.
     * IDs must match item_notification.xml exactly.
     */
    static class NotificationViewHolder extends RecyclerView.ViewHolder {

        //Init displays
        TextView notifTitle;
        TextView senderRole;
        TextView body;
        TextView timestamp;
        View unreadDot;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            notifTitle  = itemView.findViewById(R.id.notifTitle);
            senderRole = itemView.findViewById(R.id.notifSender);
            body       = itemView.findViewById(R.id.notifBody);
            timestamp  = itemView.findViewById(R.id.notifTimestamp);
            unreadDot  = itemView.findViewById(R.id.unreadDot);
        }
    }
}