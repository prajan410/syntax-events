package com.example.syntaxappproject;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class NotificationTest {

    private Notification notification;

    @Before
    public void setUp() {
        notification = new Notification();
    }

    // ─── Constructor ───────────────────────────────────────────────

    @Test
    public void noArgConstructor_allFieldsAreNull() {
        assertNull(notification.getNotificationId());
        assertNull(notification.getEventId());
        assertNull(notification.getSenderId());
        assertNull(notification.getSenderRole());
        assertNull(notification.getEventName());
        assertNull(notification.getTitle());
        assertNull(notification.getBody());
        assertNull(notification.getTargetGroup());
        assertNull(notification.getTargetEntrantIds());
        assertNull(notification.getStatus());
        assertEquals(0L, notification.getTimestamp());
    }

    // ─── notificationId ────────────────────────────────────────────

    @Test
    public void setNotificationId_getReturnsValue() {
        notification.setNotificationId("notif-001");
        assertEquals("notif-001", notification.getNotificationId());
    }

    @Test
    public void setNotificationId_null_getReturnsNull() {
        notification.setNotificationId("notif-001");
        notification.setNotificationId(null);
        assertNull(notification.getNotificationId());
    }

    // ─── eventId ───────────────────────────────────────────────────

    @Test
    public void setEventId_getReturnsValue() {
        notification.setEventId("event-abc");
        assertEquals("event-abc", notification.getEventId());
    }

    @Test
    public void setEventId_administrationConstant() {
        notification.setEventId("ADMINISTRATION");
        assertEquals("ADMINISTRATION", notification.getEventId());
    }

    // ─── senderId ──────────────────────────────────────────────────

    @Test
    public void setSenderId_getReturnsValue() {
        notification.setSenderId("user-xyz");
        assertEquals("user-xyz", notification.getSenderId());
    }

    // ─── senderRole ────────────────────────────────────────────────

    @Test
    public void setSenderRole_organizer() {
        notification.setSenderRole("ORGANIZER");
        assertEquals("ORGANIZER", notification.getSenderRole());
    }

    @Test
    public void setSenderRole_admin() {
        notification.setSenderRole("ADMIN");
        assertEquals("ADMIN", notification.getSenderRole());
    }

    @Test
    public void setSenderRole_invalidValue_stillStored() {
        // Model does not validate — this documents that behaviour
        notification.setSenderRole("SUPERUSER");
        assertEquals("SUPERUSER", notification.getSenderRole());
    }

    // ─── eventName ─────────────────────────────────────────────────

    @Test
    public void setEventName_getReturnsValue() {
        notification.setEventName("Spring Hackathon");
        assertEquals("Spring Hackathon", notification.getEventName());
    }

    @Test
    public void setEventName_overwrite() {
        notification.setEventName("Old Name");
        notification.setEventName("New Name");
        assertEquals("New Name", notification.getEventName());
    }

    // ─── title ─────────────────────────────────────────────────────

    @Test
    public void setTitle_getReturnsValue() {
        notification.setTitle("You have been selected!");
        assertEquals("You have been selected!", notification.getTitle());
    }

    @Test
    public void setTitle_emptyString() {
        notification.setTitle("");
        assertEquals("", notification.getTitle());
    }

    // ─── body ──────────────────────────────────────────────────────

    @Test
    public void setBody_getReturnsValue() {
        notification.setBody("Please confirm your registration by Friday.");
        assertEquals("Please confirm your registration by Friday.", notification.getBody());
    }

    @Test
    public void setBody_longMessage() {
        String longMsg = "A".repeat(5000);
        notification.setBody(longMsg);
        assertEquals(longMsg, notification.getBody());
    }

    // ─── timestamp ─────────────────────────────────────────────────

    @Test
    public void setTimestamp_getReturnsValue() {
        long now = System.currentTimeMillis();
        notification.setTimestamp(now);
        assertEquals(now, notification.getTimestamp());
    }

    @Test
    public void setTimestamp_zero() {
        notification.setTimestamp(0L);
        assertEquals(0L, notification.getTimestamp());
    }

    @Test
    public void setTimestamp_maxLong() {
        notification.setTimestamp(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, notification.getTimestamp());
    }

    // ─── targetGroup ───────────────────────────────────────────────

    @Test
    public void setTargetGroup_all() {
        notification.setTargetGroup("ALL");
        assertEquals("ALL", notification.getTargetGroup());
    }

    @Test
    public void setTargetGroup_waitlist() {
        notification.setTargetGroup("WAITLIST");
        assertEquals("WAITLIST", notification.getTargetGroup());
    }

    @Test
    public void setTargetGroup_selected() {
        notification.setTargetGroup("SELECTED");
        assertEquals("SELECTED", notification.getTargetGroup());
    }

    @Test
    public void setTargetGroup_cancelled() {
        notification.setTargetGroup("CANCELLED");
        assertEquals("CANCELLED", notification.getTargetGroup());
    }

    // ─── targetEntrantIds ──────────────────────────────────────────

    @Test
    public void setTargetEntrantIds_getReturnsSameList() {
        List<String> ids = Arrays.asList("user-1", "user-2", "user-3");
        notification.setTargetEntrantIds(ids);
        assertEquals(ids, notification.getTargetEntrantIds());
    }

    @Test
    public void setTargetEntrantIds_emptyList() {
        notification.setTargetEntrantIds(Arrays.asList());
        assertNotNull(notification.getTargetEntrantIds());
        assertTrue(notification.getTargetEntrantIds().isEmpty());
    }

    @Test
    public void setTargetEntrantIds_null() {
        notification.setTargetEntrantIds(Arrays.asList("user-1"));
        notification.setTargetEntrantIds(null);
        assertNull(notification.getTargetEntrantIds());
    }

    @Test
    public void setTargetEntrantIds_listSizePreserved() {
        List<String> ids = Arrays.asList("a", "b", "c", "d", "e");
        notification.setTargetEntrantIds(ids);
        assertEquals(5, notification.getTargetEntrantIds().size());
    }

    // ─── status ────────────────────────────────────────────────────

    @Test
    public void setStatus_sent() {
        notification.setStatus("SENT");
        assertEquals("SENT", notification.getStatus());
    }

    @Test
    public void setStatus_failed() {
        notification.setStatus("FAILED");
        assertEquals("FAILED", notification.getStatus());
    }

    @Test
    public void setStatus_pending() {
        notification.setStatus("PENDING");
        assertEquals("PENDING", notification.getStatus());
    }

    // ─── Independence between instances ────────────────────────────

    @Test
    public void twoInstances_areIndependent() {
        Notification a = new Notification();
        Notification b = new Notification();

        a.setTitle("Title A");
        b.setTitle("Title B");

        assertEquals("Title A", a.getTitle());
        assertEquals("Title B", b.getTitle());
    }

    // ─── Full object population ─────────────────────────────────────

    @Test
    public void fullyPopulatedNotification_allGettersCorrect() {
        List<String> ids = Arrays.asList("u1", "u2");

        notification.setNotificationId("notif-999");
        notification.setEventId("event-42");
        notification.setSenderId("sender-7");
        notification.setSenderRole("ORGANIZER");
        notification.setEventName("Tech Fair");
        notification.setTitle("Lottery Results");
        notification.setBody("You have been selected.");
        notification.setTimestamp(1_700_000_000_000L);
        notification.setTargetGroup("SELECTED");
        notification.setTargetEntrantIds(ids);
        notification.setStatus("SENT");

        assertEquals("notif-999",           notification.getNotificationId());
        assertEquals("event-42",            notification.getEventId());
        assertEquals("sender-7",            notification.getSenderId());
        assertEquals("ORGANIZER",           notification.getSenderRole());
        assertEquals("Tech Fair",           notification.getEventName());
        assertEquals("Lottery Results",     notification.getTitle());
        assertEquals("You have been selected.", notification.getBody());
        assertEquals(1_700_000_000_000L,    notification.getTimestamp());
        assertEquals("SELECTED",            notification.getTargetGroup());
        assertEquals(ids,                   notification.getTargetEntrantIds());
        assertEquals("SENT",                notification.getStatus());
    }
}