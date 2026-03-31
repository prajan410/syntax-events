package com.example.syntaxappproject;

import android.text.format.DateUtils;
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
 * RecyclerView adapter for displaying comments in the event detail screen.
 *
 * <p>This adapter is used in {@link com.example.syntaxappproject.ui.EventDetailFragment}
 * to display comments for a specific event. It supports:</p>
 * <ul>
 *   <li>Displaying user name, comment text, and timestamp</li>
 *   <li>Timestamps formatted as "h:mm a" for today's comments, "MMM dd" for older comments</li>
 *   <li>Context-sensitive action buttons based on user role:</li>
 *   <ul>
 *     <li>Admins and organizers can delete any comment</li>
 *     <li>Users can delete their own comments</li>
 *     <li>Users can report other users' comments</li>
 *     <li>Reported comments show "Reported" and become disabled</li>
 *   </ul>
 * </ul>
 *
 * <p>The adapter dynamically updates button states when user roles change
 * via {@link #updateRoles(boolean, boolean)}.</p>
 *
 * @see Comment
 * @see com.example.syntaxappproject.ui.EventDetailFragment
 */
public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    /** List of comments to display. */
    private List<Comment> comments = new ArrayList<>();

    /** ID of the currently authenticated user. */
    private final String currentUserId;

    /** Whether the current user is an organizer for this event. */
    private boolean isOrganizer;

    /** Whether the current user has admin privileges. */
    private boolean isAdmin;

    /** Listener for comment deletion events. */
    private final OnCommentDeleteListener deleteListener;

    /** Listener for comment report events. */
    private final OnCommentReportListener reportListener;

    /**
     * Callback interface for comment deletion events.
     */
    public interface OnCommentDeleteListener {
        /**
         * Called when a user requests to delete a comment.
         *
         * @param comment the comment to delete
         */
        void onDeleteComment(Comment comment);
    }

    /**
     * Callback interface for comment report events.
     */
    public interface OnCommentReportListener {
        /**
         * Called when a user requests to report a comment.
         *
         * @param comment the comment to report
         */
        void onReportComment(Comment comment);
    }

    /**
     * Constructs a new CommentAdapter with the specified parameters.
     *
     * @param currentUserId    the ID of the currently authenticated user
     * @param isOrganizer      whether the user is an organizer for this event
     * @param isAdmin          whether the user has admin privileges
     * @param deleteListener   listener for comment deletion
     * @param reportListener   listener for comment reporting
     */
    public CommentAdapter(String currentUserId, boolean isOrganizer, boolean isAdmin,
                          OnCommentDeleteListener deleteListener, OnCommentReportListener reportListener) {
        this.currentUserId = currentUserId;
        this.isOrganizer = isOrganizer;
        this.isAdmin = isAdmin;
        this.deleteListener = deleteListener;
        this.reportListener = reportListener;
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
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    /**
     * Binds comment data to the view holder at the given position.
     *
     * <p>Sets user name, comment text, and formatted timestamp. Also configures
     * the action button (Delete or Report) based on user permissions and comment status.</p>
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
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                holder.timestampText.setText(dateFormat.format(date));
            }
        }
        boolean canDelete = isAdmin || isOrganizer || (currentUserId != null && currentUserId.equals(comment.getUserId()));
        boolean hasReported = comment.isReportedByCurrentUser(currentUserId);

        if (canDelete) {
            holder.actionButton.setVisibility(View.VISIBLE);
            holder.actionButton.setText("Delete");
            holder.actionButton.setTextColor(0xFFE74C3C);
            holder.actionButton.setEnabled(true);
            holder.actionButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteComment(comment);
                }
            });
        } else if (currentUserId != null && !currentUserId.equals(comment.getUserId())) {
            holder.actionButton.setVisibility(View.VISIBLE);
            if (hasReported) {
                holder.actionButton.setText("Reported");
                holder.actionButton.setTextColor(0xFFAAAAAA);
                holder.actionButton.setEnabled(false);
                holder.actionButton.setOnClickListener(null);
            } else {
                holder.actionButton.setText("Report");
                holder.actionButton.setTextColor(0xFFE74C3C);
                holder.actionButton.setEnabled(true);
                holder.actionButton.setOnClickListener(v -> {
                    if (reportListener != null) {
                        reportListener.onReportComment(comment);
                    }
                });
            }
        } else {
            holder.actionButton.setVisibility(View.GONE);
        }
    }

    /**
     * Returns the total number of comments in the list.
     *
     * @return the number of comments
     */
    @Override
    public int getItemCount() {
        return comments.size();
    }

    /**
     * Updates the adapter with a new list of comments and refreshes the view.
     *
     * @param comments the new list of comments to display
     */
    public void setComments(List<Comment> comments) {
        this.comments = comments;
        notifyDataSetChanged();
    }

    /**
     * Updates the organizer and admin flags and refreshes the adapter.
     *
     * <p>Called when the user's role information is loaded asynchronously,
     * allowing the adapter to show the correct action buttons (Delete vs Report)
     * after role data is available.</p>
     *
     * @param isOrganizer whether the user is an organizer for this event
     * @param isAdmin     whether the user has admin privileges
     */
    public void updateRoles(boolean isOrganizer, boolean isAdmin) {
        this.isOrganizer = isOrganizer;
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    /**
     * Adds a single comment to the end of the list and animates the insertion.
     *
     * <p>Used when a new comment is posted in real-time.</p>
     *
     * @param comment the comment to add
     */
    public void addComment(Comment comment) {
        comments.add(comment);
        notifyItemInserted(comments.size() - 1);
    }

    /**
     * Removes a comment from the list by its ID and animates the removal.
     *
     * @param commentId the ID of the comment to remove
     */
    public void removeComment(String commentId) {
        int index = -1;
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getCommentId().equals(commentId)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            comments.remove(index);
            notifyItemRemoved(index);
        }
    }

    /**
     * ViewHolder that caches references to the views in each comment item.
     */
    static class CommentViewHolder extends RecyclerView.ViewHolder {
        /** Displays the comment author's user name. */
        TextView userNameText;

        /** Displays the formatted timestamp of the comment. */
        TextView timestampText;

        /** Displays the comment text content. */
        TextView commentText;

        /** Button for Delete or Report actions (context-sensitive). */
        TextView actionButton;

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
            actionButton = itemView.findViewById(R.id.commentActionButton);
        }
    }
}