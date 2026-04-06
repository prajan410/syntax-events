package com.example.syntaxappproject;

import java.util.List;

/**
 * Model class representing a notification sent by an organizer or admin
 * to a group of event entrants in the SyntaxEvents application.
 *
 * <p>Notifications are written to two locations in Firestore:</p>
 * <ul>
 *   <li>A canonical record in the top-level {@code notifications} collection,
 *       used by admins to audit all sent notifications.</li>
 *   <li>A personal copy in each recipient's {@code users/{userId}/notifications}
 *       subcollection, used to populate the user's notification inbox.</li>
 * </ul>
 *
 * <p>The {@code senderRole} field determines which opt-out preference is checked
 * before delivery: {@code "ORGANIZER"} checks {@code organizerNotificationEnabled},
 * and {@code "ADMIN"} checks {@code adminNotificationEnabled} in the user's profile.</p>
 *
 * <p>Admin notifications use {@code eventId = "ADMINISTRATION"} to indicate they
 * are platform-level messages not tied to any specific event.</p>
 *
 * @see NotificationRepository
 */
public class Notification {

    /**
     * Unique identifier for this notification, set from the Firestore document ID
     * after the document is written. Used to track which notifications have been
     * seen by the user via {@code SharedPreferences}.
     */
    private String notificationId;

    /**
     * Firestore document ID of the event this notification is associated with.
     * Set to {@code "ADMINISTRATION"} for platform-level admin notifications.
     */
    private String eventId;

    /**
     * Firestore UID of the user who sent this notification (organizer or admin).
     */
    private String senderId;

    /**
     * Role of the sender. Must be one of:
     * <ul>
     *   <li>{@code "ORGANIZER"} — sent by an event organizer</li>
     *   <li>{@code "ADMIN"} — sent by a platform administrator</li>
     * </ul>
     */
    private String senderRole;

    /**
     * Display name of the event, resolved from Firestore at read time.
     * Not stored in Firestore — populated in-memory before rendering.
     */
    private String eventName;

    /**
     * Short title of the notification displayed as the heading in the inbox.
     */
    private String title;

    /**
     * Full message body of the notification.
     */
    private String body;

    /**
     * Unix epoch timestamp in milliseconds indicating when the notification was sent.
     */
    private long timestamp;

    /**
     * The group of entrants this notification was addressed to. Must be one of:
     * <ul>
     *   <li>{@code "ALL"} — all entrants (waitlist, selected, and cancelled)</li>
     *   <li>{@code "WAITLIST"} — entrants currently on the waitlist</li>
     *   <li>{@code "SELECTED"} — entrants who won the lottery</li>
     *   <li>{@code "CANCELLED"} — entrants who were not selected</li>
     * </ul>
     */
    private String targetGroup;

    /**
     * Optional list of specific entrant user IDs when {@code targetGroup = "SELECTED"}.
     * Not used in the current fan-out implementation.
     */
    private List<String> targetEntrantIds;

    /**
     * Delivery status of the notification. One of:
     * <ul>
     *   <li>{@code "SENT"} — successfully delivered</li>
     *   <li>{@code "FAILED"} — delivery failed</li>
     *   <li>{@code "PENDING"} — not yet delivered</li>
     * </ul>
     */
    private String status;

    /**
     * Required no-argument constructor for Firestore deserialization.
     */
    public Notification() {}

    /**
     * Returns the unique identifier for this notification.
     *
     * @return the notification ID, or {@code null} if not yet persisted
     */
    public String getNotificationId() { return notificationId; }

    /**
     * Sets the unique identifier for this notification.
     *
     * @param notificationId the Firestore document ID
     */
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    /**
     * Returns the event ID associated with this notification.
     *
     * @return the event ID, or {@code "ADMINISTRATION"} for admin notifications
     */
    public String getEventId() { return eventId; }

    /**
     * Sets the event ID associated with this notification.
     *
     * @param eventId the Firestore document ID of the event
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Returns the UID of the user who sent this notification.
     *
     * @return the sender's user ID
     */
    public String getSenderId() { return senderId; }

    /**
     * Sets the UID of the user who sent this notification.
     *
     * @param senderId the sender's user ID
     */
    public void setSenderId(String senderId) { this.senderId = senderId; }

    /**
     * Returns the role of the sender.
     *
     * @return {@code "ORGANIZER"} or {@code "ADMIN"}
     */
    public String getSenderRole() { return senderRole; }

    /**
     * Sets the role of the sender.
     *
     * @param senderRole {@code "ORGANIZER"} or {@code "ADMIN"}
     */
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    /**
     * Returns the display name of the event, resolved at read time.
     *
     * @return the event name, or {@code null} if not yet resolved
     */
    public String getEventName() { return eventName; }

    /**
     * Sets the display name of the event for rendering purposes.
     *
     * @param eventName the human-readable event name
     */
    public void setEventName(String eventName) { this.eventName = eventName; }

    /**
     * Returns the short title of the notification.
     *
     * @return the notification title
     */
    public String getTitle() { return title; }

    /**
     * Sets the short title of the notification.
     *
     * @param title the notification title
     */
    public void setTitle(String title) { this.title = title; }

    /**
     * Returns the full message body of the notification.
     *
     * @return the notification body
     */
    public String getBody() { return body; }

    /**
     * Sets the full message body of the notification.
     *
     * @param body the notification body
     */
    public void setBody(String body) { this.body = body; }

    /**
     * Returns the Unix epoch timestamp in milliseconds when the notification was sent.
     *
     * @return the timestamp in milliseconds
     */
    public long getTimestamp() { return timestamp; }

    /**
     * Sets the Unix epoch timestamp in milliseconds when the notification was sent.
     *
     * @param timestamp the timestamp in milliseconds
     */
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /**
     * Returns the target group for this notification.
     *
     * @return one of {@code "ALL"}, {@code "WAITLIST"}, {@code "SELECTED"}, {@code "CANCELLED"}
     */
    public String getTargetGroup() { return targetGroup; }

    /**
     * Sets the target group for this notification.
     *
     * @param targetGroup one of {@code "ALL"}, {@code "WAITLIST"}, {@code "SELECTED"}, {@code "CANCELLED"}
     */
    public void setTargetGroup(String targetGroup) { this.targetGroup = targetGroup; }

    /**
     * Returns the list of specific entrant IDs targeted by this notification.
     *
     * @return list of user IDs, or {@code null} if not applicable
     */
    public List<String> getTargetEntrantIds() { return targetEntrantIds; }

    /**
     * Sets the list of specific entrant IDs targeted by this notification.
     *
     * @param targetEntrantIds list of user IDs
     */
    public void setTargetEntrantIds(List<String> targetEntrantIds) { this.targetEntrantIds = targetEntrantIds; }

    /**
     * Returns the delivery status of this notification.
     *
     * @return one of {@code "SENT"}, {@code "FAILED"}, {@code "PENDING"}
     */
    public String getStatus() { return status; }

    /**
     * Sets the delivery status of this notification.
     *
     * @param status one of {@code "SENT"}, {@code "FAILED"}, {@code "PENDING"}
     */
    public void setStatus(String status) { this.status = status; }
}