package com.example.syntaxappproject;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.syntaxappproject.R;
import com.example.syntaxappproject.ui.AdminBrowseCommentsFragment;
import com.example.syntaxappproject.ui.AdminCommentDetailsFragment;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for displaying a list of comments to admin users.
 *
 * <p>Each comment item displays:</p>
 * <ul>
 *   <li>User name of the comment author</li>
 *   <li>Timestamp (formatted as time if today, date if older)</li>
 *   <li>Comment text preview</li>
 *   <li>Report count badge (only visible if comment has reports)</li>
 *   <li>View Details button to navigate to full comment moderation screen</li>
 * </ul>
 *
 * <p>This adapter is used in {@link AdminBrowseCommentsFragment} to display
 * the list of all comments in the system for admin moderation.</p>
 *
 * @author Syntax App Project Team
 * @version 1.0
 * @see Comment
 * @see AdminBrowseCommentsFragment
 * @see AdminCommentDetailsFragment
 */
public class AdminCommentAdapter extends RecyclerView.Adapter<AdminCommentAdapter.CommentViewHolder> {

    /** List of comments to display in the RecyclerView. */
    private List<Comment> comments = new ArrayList<>();

    /** Click listener for comment item clicks. Package-private for testing. */
    final OnCommentClickListener clickListener;

    /** For testing only - prevents notifyDataSetChanged from being called. */
    boolean isTestMode = false;

    /**
     * Callback interface for comment click events.
     * Implemented by {@link AdminBrowseCommentsFragment} to handle navigation
     * to the comment details screen.
     */
    public interface OnCommentClickListener {
        /**
         * Called when a comment item is clicked.
         *
         * @param comment the comment that was clicked
         */
        void onCommentClick(Comment comment);
    }

    /**
     * Constructs a new AdminCommentAdapter with the specified click listener.
     *
     * @param clickListener listener for comment click events
     */
    public AdminCommentAdapter(OnCommentClickListener clickListener) {
        this.clickListener = clickListener;
    }

    /**
     * Creates and inflates the view holder for a comment item.
     *
     * @param parent   the parent view group
     * @param viewType the view type (unused, single type)
     * @return a new CommentViewHolder
     */
    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_comment, parent, false);
        return new CommentViewHolder(view);
    }

    /**
     * Binds comment data to the view holder at the given position.
     * Sets user name, comment text, timestamp formatting, and report badge.
     *
     * @param holder   the view holder to bind data to
     * @param position the position of the item in the list
     */
    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);

        holder.userNameText.setText(comment.getUserName());
        holder.commentText.setText(comment.getCommentText());

        if (comment.getTimestamp() != null) {
            Date date = comment.getTimestamp().toDate();
            long now = System.currentTimeMillis();
            long then = date.getTime();
            if (DateUtils.isToday(then)) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                holder.timestampText.setText(timeFormat.format(date));
            } else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                holder.timestampText.setText(dateFormat.format(date));
            }
        }
        if (comment.getReportCount() > 0) {
            holder.reportChip.setVisibility(View.VISIBLE);
            holder.reportChip.setText(comment.getReportCount() + " report" +
                    (comment.getReportCount() > 1 ? "s" : ""));
        } else {
            holder.reportChip.setVisibility(View.GONE);
        }
        holder.viewDetailsButton.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCommentClick(comment);
            }
        });
    }

    /**
     * Returns the total number of comments in the list.
     * Handles null list safely to prevent NullPointerException.
     *
     * @return the number of comments, or 0 if list is null
     */
    @Override
    public int getItemCount() {
        return comments == null ? 0 : comments.size();
    }

    /**
     * Updates the adapter with a new list of comments and refreshes the view.
     * If in test mode, skips notifying the RecyclerView to prevent
     * NullPointerException in unit tests.
     *
     * @param comments the new list of comments to display, or null to clear
     */
    public void setComments(List<Comment> comments) {
        this.comments = comments != null ? comments : new ArrayList<>();
        if (!isTestMode) {
            notifyDataSetChanged();
        }
    }

    /**
     * Returns the current list of comments.
     * Package-private for testing purposes.
     *
     * @return the current list of comments
     */
    List<Comment> getComments() {
        return comments;
    }

    /**
     * ViewHolder that caches references to the views in each comment item.
     * Provides efficient access to UI elements without repeated findViewById calls.
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        /** Displays the comment author's user name. */
        TextView userNameText;

        /** Displays the formatted timestamp of the comment. */
        TextView timestampText;

        /** Displays the comment text preview. */
        TextView commentText;

        /** Displays the report count badge (shows number of reports). */
        TextView reportChip;

        /** Button to navigate to full comment details for moderation. */
        MaterialButton viewDetailsButton;

        /**
         * Constructs the ViewHolder and binds child views.
         *
         * @param itemView the inflated item view
         */
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.commentUserName);
            timestampText = itemView.findViewById(R.id.commentTimestamp);
            commentText = itemView.findViewById(R.id.commentText);
            reportChip = itemView.findViewById(R.id.reportChip);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }
    }
}