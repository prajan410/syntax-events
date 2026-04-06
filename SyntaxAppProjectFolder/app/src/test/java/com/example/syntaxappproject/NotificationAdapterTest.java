package com.example.syntaxappproject;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
/**
 * Unit tests for {@link NotificationAdapter}.
 *
 * <p>This test suite verifies the correctness of data handling and display logic
 * used by the NotificationAdapter.
 */
public class NotificationAdapterTest {




    private NotificationAdapter adapter;
    /**
     * Initializes a fresh {@link NotificationAdapter} before each test.
     */
    @Before
    public void setUp() {
        adapter = new NotificationAdapter();
    }

    // ─── Helper ────────────────────────────────────────────────────
    /**
     * Creates a test {@link Notification} with specified fields.
     *
     * @param role       sender role (e.g., ADMIN, ORGANIZER)
     * @param eventName  associated event name
     * @param title      notification title
     * @param body       notification body
     * @param timestamp  timestamp in milliseconds
     * @return configured Notification object
     */
    private Notification makeNotification(String role, String eventName,
                                          String title, String body, long timestamp) {
        Notification n = new Notification();
        n.setSenderRole(role);
        n.setEventName(eventName);
        n.setTitle(title);
        n.setBody(body);
        n.setTimestamp(timestamp);
        return n;
    }


    // ─── getItemCount ──────────────────────────────────────────────
    /** Verifies adapter is empty by default. */

    @Test
    public void getItemCount_emptyByDefault() {
        assertEquals(0, adapter.getNotificationsForTesting().size());
    }
    /** Verifies item count reflects number of notifications set. */

    @Test
    public void getItemCount_afterSettingList() {
        adapter.setNotifications(Arrays.asList(
                makeNotification("ADMIN", null, "Title", "Body", 0),
                makeNotification("ORGANIZER", "Event", "Title", "Body", 0)
        ));
        assertEquals(2, adapter.getNotificationsForTesting().size());
    }
    /** Verifies single-item list count. */

    @Test
    public void getItemCount_singleItem() {
        adapter.setNotifications(Arrays.asList(
                makeNotification("ADMIN", null, "Title", "Body", 0)
        ));
        assertEquals(1, adapter.getNotificationsForTesting().size());
    }

    // ─── setNotifications ──────────────────────────────────────────
    /** Verifies null input is treated as an empty list. */

    @Test
    public void setNotifications_null_treatedAsEmpty() {
        adapter.setNotifications(null);
        assertEquals(0, adapter.getItemCount());
    }
    /** Verifies empty list results in zero items. */

    @Test
    public void setNotifications_emptyList_countIsZero() {
        adapter.setNotifications(new ArrayList<>());
        assertEquals(0, adapter.getItemCount());
    }
    /** Verifies new data replaces existing list. */

    @Test
    public void setNotifications_replacesExistingList() {
        adapter.setNotifications(Arrays.asList(
                makeNotification("ADMIN", null, "Old", "Old", 0)
        ));
        adapter.setNotifications(Arrays.asList(
                makeNotification("ADMIN", null, "New1", "Body", 0),
                makeNotification("ADMIN", null, "New2", "Body", 0),
                makeNotification("ADMIN", null, "New3", "Body", 0)
        ));
        assertEquals(3, adapter.getNotificationsForTesting().size());
    }
    /** Verifies repeated null updates result in empty adapter. */

    @Test
    public void setNotifications_calledTwiceWithNull_countIsZero() {
        adapter.setNotifications(Arrays.asList(
                makeNotification("ADMIN", null, "Title", "Body", 0)
        ));
        adapter.setNotifications(null);
        assertEquals(0, adapter.getItemCount());
    }
    /** Verifies adapter handles large lists correctly. */

    @Test
    public void setNotifications_largeList_countIsCorrect() {
        List<Notification> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(makeNotification("ORGANIZER", "Event " + i, "Title", "Body", 0));
        }
        adapter.setNotifications(list);
        assertEquals(100, adapter.getNotificationsForTesting().size());
    }

    // ─── Notification data integrity ───────────────────────────────
    /** Verifies admin role and null event name are stored correctly. */

    @Test
    public void notification_adminRole_isStoredCorrectly() {
        Notification n = makeNotification("ADMIN", null, "Alert", "Body", 0);
        assertEquals("ADMIN", n.getSenderRole());
        assertNull(n.getEventName());
    }
    /** Verifies organizer role preserves event name. */

    @Test
    public void notification_organizerRole_eventNamePreserved() {
        Notification n = makeNotification("ORGANIZER", "Tech Fair", "Title", "Body", 0);
        assertEquals("ORGANIZER", n.getSenderRole());
        assertEquals("Tech Fair", n.getEventName());
    }
    /** Verifies null event name is handled. */

    @Test
    public void notification_organizerRole_nullEventName() {
        Notification n = makeNotification("ORGANIZER", null, "Title", "Body", 0);
        assertNull(n.getEventName());
    }
    /** Verifies empty event name is preserved. */

    @Test
    public void notification_organizerRole_emptyEventName() {
        Notification n = makeNotification("ORGANIZER", "", "Title", "Body", 0);
        assertTrue(n.getEventName().isEmpty());
    }
    /** Verifies title and body are stored correctly. */

    @Test
    public void notification_titleAndBodyStoredCorrectly() {
        Notification n = makeNotification("ADMIN", null, "My Title", "My Body", 0);
        assertEquals("My Title", n.getTitle());
        assertEquals("My Body", n.getBody());
    }

    // ─── Sender label logic ────────────────────────────────────────
    /** Verifies ADMIN role maps to "ADMINISTRATION". */

    @Test
    public void senderLabel_adminRole_returnsAdministration() {
        Notification n = makeNotification("ADMIN", null, "Title", "Body", 0);
        assertEquals("From: ADMINISTRATION", resolveSenderLabel(n));
    }
    /** Verifies organizer displays event name when available. */

    @Test
    public void senderLabel_organizerWithEventName_returnsEventName() {
        Notification n = makeNotification("ORGANIZER", "Spring Hackathon", "Title", "Body", 0);
        assertEquals("From: Spring Hackathon", resolveSenderLabel(n));
    }
    /** Verifies null event name falls back to "Unknown Event". */

    @Test
    public void senderLabel_organizerNullEventName_returnsUnknown() {
        Notification n = makeNotification("ORGANIZER", null, "Title", "Body", 0);
        assertEquals("From: Unknown Event", resolveSenderLabel(n));
    }
    /** Verifies empty event name falls back to "Unknown Event". */

    @Test
    public void senderLabel_organizerEmptyEventName_returnsUnknown() {
        Notification n = makeNotification("ORGANIZER", "", "Title", "Body", 0);
        assertEquals("From: Unknown Event", resolveSenderLabel(n));
    }

    // ─── Timestamp logic ───────────────────────────────────────────
    /** Verifies zero timestamp returns empty string. */

    @Test
    public void timestamp_zero_returnsEmpty() {
        assertEquals("", resolveTimestamp(0));
    }
    /** Verifies timestamps under one minute show "Just now". */

    @Test
    public void timestamp_underOneMinute_returnsJustNow() {
        assertEquals("Just now", resolveTimestamp(System.currentTimeMillis() - 30_000));
    }
    /** Verifies minute-level formatting. */

    @Test
    public void timestamp_fiveMinutesAgo_returnsMinuteFormat() {
        assertEquals("5m ago", resolveTimestamp(System.currentTimeMillis() - 5 * 60_000L));
    }
    /** Verifies hour-level formatting. */

    @Test
    public void timestamp_oneHourAgo_returnsHourFormat() {
        assertEquals("1h ago", resolveTimestamp(System.currentTimeMillis() - 3_600_000L));
    }
    /** Verifies multiple hours formatting. */

    @Test
    public void timestamp_threeHoursAgo_returnsHourFormat() {
        assertEquals("3h ago", resolveTimestamp(System.currentTimeMillis() - 3 * 3_600_000L));
    }
    /** Verifies dates older than 24 hours use date format. */

    @Test
    public void timestamp_olderThan24h_returnsDateFormat() {
        String result = resolveTimestamp(System.currentTimeMillis() - 2 * 86_400_000L);
        assertTrue(result.matches("[A-Z][a-z]{2} \\d{2}"));
    }
    /** Verifies exactly one minute is formatted correctly. */

    @Test
    public void timestamp_exactlyOneMinute_returnsMinuteFormat() {
        assertEquals("1m ago", resolveTimestamp(System.currentTimeMillis() - 60_000L));
    }

    // ─── Mirrors of adapter private logic ─────────────────────────
    /**
     * Resolves sender label based on role and event name.
     */
    private String resolveSenderLabel(Notification n) {
        if ("ADMIN".equals(n.getSenderRole())) return "From: ADMINISTRATION";
        String eventName = n.getEventName();
        return (eventName != null && !eventName.isEmpty())
                ? "From: " + eventName
                : "From: Unknown Event";
    }
    /**
     * Formats timestamp into a human-readable relative time string.
     */
    private String resolveTimestamp(long timestamp) {
        if (timestamp == 0) return "";
        long diff = System.currentTimeMillis() - timestamp;
        if (diff < 60_000)     return "Just now";
        if (diff < 3_600_000)  return (diff / 60_000) + "m ago";
        if (diff < 86_400_000) return (diff / 3_600_000) + "h ago";
        return new java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
                .format(new java.util.Date(timestamp));
    }
}