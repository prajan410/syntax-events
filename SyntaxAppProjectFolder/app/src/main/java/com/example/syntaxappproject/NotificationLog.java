package com.example.syntaxappproject;



/**
 * Model class representing a single delivery record for a notification.
 * One log is written per recipient per notification, allowing admins
 * to audit exactly who received what and when (US 03.08.01).
 */
public class NotificationLog {

    private String logId;
    private String notificationId;
    private String recipientId;
    private String eventId;
    private boolean delivered;
    private long deliveredAt;

    /** Required no-arg constructor for Firestore deserialization. */
    public NotificationLog() {}

    public NotificationLog(String notificationId, String recipientId,
                           String eventId, boolean delivered, long deliveredAt) {
        this.notificationId = notificationId;
        this.recipientId    = recipientId;
        this.eventId        = eventId;
        this.delivered      = delivered;
        this.deliveredAt    = deliveredAt;
    }

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public boolean isDelivered() { return delivered; }
    public void setDelivered(boolean delivered) { this.delivered = delivered; }

    public long getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(long deliveredAt) { this.deliveredAt = deliveredAt; }
}