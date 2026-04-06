package com.example.syntaxappproject;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
/**
 * Unit tests for {@link Notification}.
 *
 * <p>This test suite verifies the correctness of the Notification data model,
 * ensuring all getters and setters behave as expected.<p/>
 */
public class NotificationTest {

    private Notification notification;
    /**
     * Initializes a fresh {@link Notification} instance before each test.
     */
    @Before
    public void setUp() {
        notification = new Notification();
    }

    // ─── Constructor ───────────────────────────────────────────────
    /**
     * Verifies that all fields are null (or default values) after using
     * the no-argument constructor.
     */
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
    /** Verifies that notificationId is stored and retrieved correctly. */

    @Test
    public void setNotificationId_getReturnsValue() {
        notification.setNotificationId("notif-001");
        assertEquals("notif-001", notification.getNotificationId());
    }
    /** Verifies that notificationId can be reset to null. */

    @Test
    public void setNotificationId_null_getReturnsNull() {
        notification.setNotificationId("notif-001");
        notification.setNotificationId(null);
        assertNull(notification.getNotificationId());
    }

    // ─── eventId ───────────────────────────────────────────────────
    /** Verifies eventId is stored correctly. */

    @Test
    public void setEventId_getReturnsValue() {
        notification.setEventId("event-abc");
        assertEquals("event-abc", notification.getEventId());
    }
    /** Verifies special constant values (e.g., ADMINISTRATION) are accepted. */

    @Test
    public void setEventId_administrationConstant() {
        notification.setEventId("ADMINISTRATION");
        assertEquals("ADMINISTRATION", notification.getEventId());
    }

    // ─── senderId ──────────────────────────────────────────────────
    /** Verifies senderId is stored correctly. */

    @Test
    public void setSenderId_getReturnsValue() {
        notification.setSenderId("user-xyz");
        assertEquals("user-xyz", notification.getSenderId());
    }

    // ─── senderRole ────────────────────────────────────────────────
    /** Verifies organizer role is stored correctly. */

    @Test
    public void setSenderRole_organizer() {
        notification.setSenderRole("ORGANIZER");
        assertEquals("ORGANIZER", notification.getSenderRole());
    }
    /** Verifies admin role is stored correctly. */
    @Test
    public void setSenderRole_admin() {
        notification.setSenderRole("ADMIN");
        assertEquals("ADMIN", notification.getSenderRole());
    }
    /**
     * Verifies that invalid or unexpected role values are still stored.
     * (No validation is enforced in the model.)
     */
    @Test
    public void setSenderRole_invalidValue_stillStored() {
        // Model does not validate — this documents that behaviour
        notification.setSenderRole("SUPERUSER");
        assertEquals("SUPERUSER", notification.getSenderRole());
    }

    // ─── eventName ─────────────────────────────────────────────────
    /** Verifies eventName is stored correctly. */

    @Test
    public void setEventName_getReturnsValue() {
        notification.setEventName("Spring Hackathon");
        assertEquals("Spring Hackathon", notification.getEventName());
    }
    /** Verifies eventName can be overwritten. */

    @Test
    public void setEventName_overwrite() {
        notification.setEventName("Old Name");
        notification.setEventName("New Name");
        assertEquals("New Name", notification.getEventName());
    }

    // ─── title ─────────────────────────────────────────────────────
    /** Verifies title is stored correctly. */
    @Test
    public void setTitle_getReturnsValue() {
        notification.setTitle("You have been selected!");
        assertEquals("You have been selected!", notification.getTitle());
    }
    /** Verifies empty string title is handled correctly. */

    @Test
    public void setTitle_emptyString() {
        notification.setTitle("");
        assertEquals("", notification.getTitle());
    }

    // ─── body ──────────────────────────────────────────────────────
    /** Verifies body message is stored correctly. */

    @Test
    public void setBody_getReturnsValue() {
        notification.setBody("Please confirm your registration by Friday.");
        assertEquals("Please confirm your registration by Friday.", notification.getBody());
    }
    /** Verifies very large message bodies are handled correctly. */

    @Test
    public void setBody_longMessage() {
        String longMsg = "A".repeat(5000);
        notification.setBody(longMsg);
        assertEquals(longMsg, notification.getBody());
    }

    // ─── timestamp ─────────────────────────────────────────────────
    /** Verifies timestamp is stored correctly. */

    @Test
    public void setTimestamp_getReturnsValue() {
        long now = System.currentTimeMillis();
        notification.setTimestamp(now);
        assertEquals(now, notification.getTimestamp());
    }
    /** Verifies timestamp supports zero value. */

    @Test
    public void setTimestamp_zero() {
        notification.setTimestamp(0L);
        assertEquals(0L, notification.getTimestamp());
    }
    /** Verifies timestamp supports maximum long value. */

    @Test
    public void setTimestamp_maxLong() {
        notification.setTimestamp(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, notification.getTimestamp());
    }

    // ─── targetGroup ───────────────────────────────────────────────
    /** Verifies "ALL" target group. */

    @Test
    public void setTargetGroup_all() {
        notification.setTargetGroup("ALL");
        assertEquals("ALL", notification.getTargetGroup());
    }
    /** Verifies "WAITLIST" target group. */

    @Test
    public void setTargetGroup_waitlist() {
        notification.setTargetGroup("WAITLIST");
        assertEquals("WAITLIST", notification.getTargetGroup());
    }
    /** Verifies "SELECTED" target group. */

    @Test
    public void setTargetGroup_selected() {
        notification.setTargetGroup("SELECTED");
        assertEquals("SELECTED", notification.getTargetGroup());
    }
    /** Verifies "CANCELLED" target group. */

    @Test
    public void setTargetGroup_cancelled() {
        notification.setTargetGroup("CANCELLED");
        assertEquals("CANCELLED", notification.getTargetGroup());
    }

    // ─── targetEntrantIds ──────────────────────────────────────────
    /** Verifies list of entrant IDs is stored correctly. */

    @Test
    public void setTargetEntrantIds_getReturnsSameList() {
        List<String> ids = Arrays.asList("user-1", "user-2", "user-3");
        notification.setTargetEntrantIds(ids);
        assertEquals(ids, notification.getTargetEntrantIds());
    }
    /** Verifies empty list is handled correctly. */

    @Test
    public void setTargetEntrantIds_emptyList() {
        notification.setTargetEntrantIds(Arrays.asList());
        assertNotNull(notification.getTargetEntrantIds());
        assertTrue(notification.getTargetEntrantIds().isEmpty());
    }
    /** Verifies list can be reset to null. */
    @Test
    public void setTargetEntrantIds_null() {
        notification.setTargetEntrantIds(Arrays.asList("user-1"));
        notification.setTargetEntrantIds(null);
        assertNull(notification.getTargetEntrantIds());
    }
    /** Verifies list size is preserved after setting. */

    @Test
    public void setTargetEntrantIds_listSizePreserved() {
        List<String> ids = Arrays.asList("a", "b", "c", "d", "e");
        notification.setTargetEntrantIds(ids);
        assertEquals(5, notification.getTargetEntrantIds().size());
    }

    // ─── status ────────────────────────────────────────────────────
    /** Verifies SENT status. */

    @Test
    public void setStatus_sent() {
        notification.setStatus("SENT");
        assertEquals("SENT", notification.getStatus());
    }
    /** Verifies FAILED status. */

    @Test
    public void setStatus_failed() {
        notification.setStatus("FAILED");
        assertEquals("FAILED", notification.getStatus());
    }
    /** Verifies PENDING status. */

    @Test
    public void setStatus_pending() {
        notification.setStatus("PENDING");
        assertEquals("PENDING", notification.getStatus());
    }

    // ─── Independence between instances ────────────────────────────
    /** Verifies that multiple Notification instances do not share state. */

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
    /** Verifies all fields are correctly stored and retrieved when fully populated. */

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