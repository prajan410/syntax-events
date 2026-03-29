package com.example.syntaxappproject;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class representing a comment on an event in the SyntaxEvents application.
 *
 * <p>This class is used as a Firestore data model and is serialized/deserialized
 * directly via {@code DocumentSnapshot.toObject(Comment.class)}. Each comment
 * is associated with a specific event and user.</p>
 *
 * <p>Comments support a reporting system where users can report inappropriate
 * content. Each report is tracked by storing the reporting user's ID in the
 * {@code reportedBy} list, allowing the system to prevent duplicate reports
 * from the same user.</p>
 *
 * <p>Fields:</p>
 * <ul>
 *   <li><b>commentId</b> - Firestore document ID (set after creation)</li>
 *   <li><b>eventId</b> - ID of the event this comment belongs to</li>
 *   <li><b>commentText</b> - The actual content of the comment</li>
 *   <li><b>userId</b> - UID of the user who posted the comment</li>
 *   <li><b>userName</b> - Display name of the comment author</li>
 *   <li><b>userDeviceId</b> - Device ID of the comment author (for tracking)</li>
 *   <li><b>timestamp</b> - Auto-generated timestamp when the comment was created</li>
 *   <li><b>reportCount</b> - Number of reports this comment has received</li>
 *   <li><b>reportedBy</b> - List of user IDs who have reported this comment</li>
 * </ul>
 *
 * @see CommentRepository
 * @see EventDetail
 * @see com.example.syntaxappproject.ui.EventDetailFragment
 * @see com.example.syntaxappproject.ui.AdminBrowseCommentsFragment
 */
public class Comment {

    /** Firestore document ID of this comment. Set after successful creation. */
    private String commentId;

    /** ID of the event this comment belongs to. */
    private String eventId;

    /** The content/text of the comment. */
    private String commentText;

    /** UID of the user who posted this comment. */
    private String userId;

    /** Display name of the user who posted this comment. */
    private String userName;

    /** Device ID of the user who posted this comment (for tracking purposes). */
    private String userDeviceId;

    /** Timestamp when the comment was created. Automatically set by Firestore. */
    @ServerTimestamp
    private Timestamp timestamp;

    /** Number of reports this comment has received. */
    private int reportCount = 0;

    /** List of user IDs who have reported this comment (prevents duplicate reports). */
    private List<String> reportedBy = new ArrayList<>();

    /**
     * Required no-argument constructor for Firestore deserialization.
     */
    public Comment() {}

    /**
     * Constructs a new comment with the specified data.
     *
     * <p>Initializes {@code reportCount} to 0 and {@code reportedBy} to an empty list.</p>
     *
     * @param eventId        the ID of the event this comment belongs to
     * @param commentText    the content of the comment
     * @param userId         the UID of the user posting the comment
     * @param userName       the display name of the user posting the comment
     * @param userDeviceId   the device ID of the user posting the comment
     */
    public Comment(String eventId, String commentText, String userId, String userName, String userDeviceId) {
        this.eventId = eventId;
        this.commentText = commentText;
        this.userId = userId;
        this.userName = userName;
        this.userDeviceId = userDeviceId;
        this.reportCount = 0;
        this.reportedBy = new ArrayList<>();
    }


    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserDeviceId() { return userDeviceId; }
    public void setUserDeviceId(String userDeviceId) { this.userDeviceId = userDeviceId; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public int getReportCount() { return reportCount; }
    public void setReportCount(int reportCount) { this.reportCount = reportCount; }

    public List<String> getReportedBy() { return reportedBy; }
    public void setReportedBy(List<String> reportedBy) { this.reportedBy = reportedBy; }

    /**
     * Checks whether a specific user has already reported this comment.
     *
     * <p>Used to prevent duplicate reports and to determine whether to show
     * "Report" or "Reported" in the UI.</p>
     *
     * @param userId the ID of the user to check
     * @return {@code true} if the user has already reported this comment,
     *         {@code false} otherwise
     */
    public boolean isReportedByCurrentUser(String userId) {
        return reportedBy != null && userId != null && reportedBy.contains(userId);
    }

    /**
     * Adds a user to the list of users who have reported this comment.
     *
     * <p>This method prevents duplicate entries by checking if the user is
     * already in the list before adding. Should be called when a user reports
     * a comment, along with incrementing {@code reportCount}.</p>
     *
     * @param userId the ID of the user reporting the comment
     */
    public void addReportedByUser(String userId) {
        if (reportedBy == null) {
            reportedBy = new ArrayList<>();
        }
        if (!reportedBy.contains(userId)) {
            reportedBy.add(userId);
        }
    }
}