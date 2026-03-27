package com.example.syntaxappproject;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository class for handling comment operations in Firestore.
 *
 * <p>This repository acts as the data access layer between the application
 * and the Firestore backend. It provides methods for:</p>
 * <ul>
 *   <li>Adding new comments to an event</li>
 *   <li>Retrieving comments for a specific event (ordered newest first)</li>
 *   <li>Deleting comments (admin/organizer functionality)</li>
 *   <li>Reporting comments (increments report count and tracks reporting users)</li>
 *   <li>Retrieving all comments in the system (admin functionality)</li>
 * </ul>
 *
 * <p>All operations use Firestore's real-time capabilities and return results
 * via callback interfaces to handle asynchronous operations.</p>
 *
 * @see Comment
 * @see com.example.syntaxappproject.ui.EventDetailFragment
 * @see com.example.syntaxappproject.ui.AdminBrowseCommentsFragment
 */
public class CommentRepository {

    /** Log tag for debugging. */
    private static final String TAG = "CommentRepository";

    /** Firestore instance for database operations. */
    private final FirebaseFirestore db;

    /** Reference to the "comments" collection in Firestore. */
    private final CollectionReference commentsRef;

    /**
     * Constructs a new CommentRepository and initializes Firestore.
     * The comments are stored in the {@code comments} collection.
     */
    public CommentRepository() {
        db = FirebaseFirestore.getInstance();
        commentsRef = db.collection("comments");
    }

    /**
     * Adds a new comment to the Firestore database.
     *
     * <p>After successful addition, the comment's document ID is set
     * on the Comment object for future reference.</p>
     *
     * @param comment  the comment to add (should have eventId, commentText, userId, userName, userDeviceId set)
     * @param callback callback invoked with success/failure result
     */
    public void addComment(Comment comment, RepositoryCallback callback) {
        commentsRef.add(comment)
                .addOnSuccessListener(documentReference -> {
                    comment.setCommentId(documentReference.getId());
                    Log.d(TAG, "Comment added successfully with ID: " + documentReference.getId());
                    callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to add comment", e);
                    callback.onComplete(false);
                });
    }

    /**
     * Retrieves all comments for a specific event.
     *
     * <p>Comments are ordered by timestamp in descending order (newest first)
     * to show the most recent comments at the top.</p>
     *
     * @param eventId  the ID of the event to fetch comments for
     * @param callback callback invoked with the list of comments (empty list if none found)
     */
    public void getCommentsForEvent(String eventId, CommentsCallback callback) {
        Log.d(TAG, "Fetching comments for event: " + eventId);
        commentsRef.whereEqualTo("eventId", eventId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Comment comment = doc.toObject(Comment.class);
                        comment.setCommentId(doc.getId());
                        comments.add(comment);
                        Log.d(TAG, "Found comment: " + comment.getCommentText());
                    }
                    Log.d(TAG, "Total comments found: " + comments.size());
                    callback.onCommentsLoaded(comments);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get comments", e);
                    callback.onCommentsLoaded(new ArrayList<>());
                });
    }

    /**
     * Deletes a comment from Firestore by its document ID.
     *
     * <p>This method is typically called by administrators or event organizers
     * to remove inappropriate content.</p>
     *
     * @param commentId the Firestore document ID of the comment to delete
     * @param callback  callback invoked with success/failure result
     */
    public void deleteComment(String commentId, RepositoryCallback callback) {
        commentsRef.document(commentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Comment deleted: " + commentId);
                    callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to delete comment", e);
                    callback.onComplete(false);
                });
    }

    /**
     * Reports a comment, incrementing its report count and adding the reporting user.
     *
     * <p>Uses Firestore's {@code FieldValue.increment(1)} to atomically increase
     * the report count and {@code FieldValue.arrayUnion(userId)} to add the user
     * to the reportedBy list without duplicates.</p>
     *
     * @param commentId the Firestore document ID of the comment to report
     * @param userId    the ID of the user reporting the comment
     * @param callback  callback invoked with success/failure result
     */
    public void reportComment(String commentId, String userId, RepositoryCallback callback) {
        commentsRef.document(commentId)
                .update(
                        "reportCount", FieldValue.increment(1),
                        "reportedBy", FieldValue.arrayUnion(userId)
                )
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Comment reported: " + commentId);
                    callback.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to report comment", e);
                    callback.onComplete(false);
                });
    }

    /**
     * Retrieves all comments from the entire system.
     *
     * <p>Comments are ordered by timestamp in descending order (newest first).
     * This method is primarily used by administrators in {@code AdminBrowseCommentsFragment}
     * to view and moderate all comments in the system.</p>
     *
     * @param callback callback invoked with the list of all comments (empty list if none found)
     */
    public void getAllComments(CommentsCallback callback) {
        commentsRef.orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Comment comment = doc.toObject(Comment.class);
                        comment.setCommentId(doc.getId());
                        comments.add(comment);
                    }
                    Log.d(TAG, "Total comments found in system: " + comments.size());
                    callback.onCommentsLoaded(comments);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get all comments", e);
                    callback.onCommentsLoaded(new ArrayList<>());
                });
    }

    /**
     * Callback interface for operations that return a list of comments.
     * Used by {@link #getCommentsForEvent(String, CommentsCallback)} and
     * {@link #getAllComments(CommentsCallback)}.
     */
    public interface CommentsCallback {
        /**
         * Called when comments are successfully loaded from Firestore.
         *
         * @param comments the list of comments (may be empty if none found)
         */
        void onCommentsLoaded(List<Comment> comments);
    }

    /**
     * Callback interface for simple repository operations (add, delete, report).
     * Used by {@link #addComment(Comment, RepositoryCallback)},
     * {@link #deleteComment(String, RepositoryCallback)}, and
     * {@link #reportComment(String, String, RepositoryCallback)}.
     */
    public interface RepositoryCallback {
        /**
         * Called when the operation completes.
         *
         * @param success {@code true} if the operation succeeded, {@code false} otherwise
         */
        void onComplete(boolean success);
    }
}