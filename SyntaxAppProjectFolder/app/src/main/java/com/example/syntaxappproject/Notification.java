package com.example.syntaxappproject;

import java.util.List;

public class Notification {
    private String notificationId;    // UUID
    private String eventId;           // which event this relates to
    private String senderId;          // organizer or admin userId
    private String senderRole;        // "ORGANIZER" or "ADMIN"
    private String title;
    private String body;
    private long timestamp;           // epoch ms
    private String targetGroup;       // "ALL", "WAITLIST", "CANCELLED", "SELECTED"
    private List<String> targetEntrantIds; // populated when targetGroup = "SELECTED"
    private String status;            // "SENT", "FAILED", "PENDING"
}