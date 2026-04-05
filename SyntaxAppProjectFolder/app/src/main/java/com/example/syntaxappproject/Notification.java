package com.example.syntaxappproject;

import java.util.List;

public class Notification {

    private String notificationId;
    private String eventId;
    private String senderId;
    private String senderRole;   // "ORGANIZER" or "ADMIN"



    private String eventName;

    private String title;
    private String body;
    private long timestamp;
    private String targetGroup;  // "ALL", "WAITLIST", "CANCELLED", "SELECTED"
    private List<String> targetEntrantIds; // populated when targetGroup = "SELECTED"
    private String status;       // "SENT", "FAILED", "PENDING"

    public Notification() {}

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) { this.notificationId = notificationId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getTargetGroup() { return targetGroup; }
    public void setTargetGroup(String targetGroup) { this.targetGroup = targetGroup; }

    public List<String> getTargetEntrantIds() { return targetEntrantIds; }
    public void setTargetEntrantIds(List<String> targetEntrantIds) { this.targetEntrantIds = targetEntrantIds; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }


}