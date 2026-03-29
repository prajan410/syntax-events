package com.example.syntaxappproject;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Comment} model class.
 *
 * <p>This test suite verifies the functionality of the Comment model class,
 * including:</p>
 * <ul>
 *   <li>Constructor initialization and field setting</li>
 *   <li>Getter and setter methods</li>
 *   <li>Reporting functionality ({@link Comment#isReportedByCurrentUser(String)} and {@link Comment#addReportedByUser(String)})</li>
 *   <li>Edge cases (null values, duplicate reports)</li>
 * </ul>
 *
 * @see Comment
 * @author Syntax App Project Team
 * @version 1.0
 */
@RunWith(JUnit4.class)
public class CommentTest {

    private static final String TEST_COMMENT_ID = "comment123";
    private static final String TEST_EVENT_ID = "event456";
    private static final String TEST_COMMENT_TEXT = "This is a test comment";
    private static final String TEST_USER_ID = "user789";
    private static final String TEST_USER_NAME = "Test User";
    private static final String TEST_DEVICE_ID = "deviceXYZ";
    private static final String ANOTHER_USER_ID = "anotherUser456";

    private Comment comment;

    @Before
    public void setUp() {
        comment = new Comment(TEST_EVENT_ID, TEST_COMMENT_TEXT, TEST_USER_ID, TEST_USER_NAME, TEST_DEVICE_ID);
    }


    /**
     * Tests that the constructor properly initializes a new comment.
     * Verifies all fields are set correctly and default values are applied.
     */
    @Test
    public void testConstructor_initializesFieldsCorrectly() {
        assertEquals(TEST_EVENT_ID, comment.getEventId());
        assertEquals(TEST_COMMENT_TEXT, comment.getCommentText());
        assertEquals(TEST_USER_ID, comment.getUserId());
        assertEquals(TEST_USER_NAME, comment.getUserName());
        assertEquals(TEST_DEVICE_ID, comment.getUserDeviceId());
        assertEquals(0, comment.getReportCount());
        assertNotNull(comment.getReportedBy());
        assertTrue(comment.getReportedBy().isEmpty());
    }

    /**
     * Tests that the no-argument constructor creates an empty comment with default values.
     * Note: reportedBy is initialized to empty list, not null.
     */
    @Test
    public void testNoArgsConstructor_createsEmptyComment() {
        Comment emptyComment = new Comment();

        assertNull(emptyComment.getCommentId());
        assertNull(emptyComment.getEventId());
        assertNull(emptyComment.getCommentText());
        assertNull(emptyComment.getUserId());
        assertNull(emptyComment.getUserName());
        assertNull(emptyComment.getUserDeviceId());
        assertNull(emptyComment.getTimestamp());
        assertEquals(0, emptyComment.getReportCount());
        assertNotNull(emptyComment.getReportedBy());
        assertTrue(emptyComment.getReportedBy().isEmpty());
    }

    /**
     * Tests setting and getting the comment ID.
     */
    @Test
    public void testSetAndGetCommentId() {
        comment.setCommentId(TEST_COMMENT_ID);
        assertEquals(TEST_COMMENT_ID, comment.getCommentId());
    }

    /**
     * Tests setting and getting the event ID.
     */
    @Test
    public void testSetAndGetEventId() {
        String newEventId = "newEvent789";
        comment.setEventId(newEventId);
        assertEquals(newEventId, comment.getEventId());
    }

    /**
     * Tests setting and getting the comment text.
     */
    @Test
    public void testSetAndGetCommentText() {
        String newText = "Updated comment text";
        comment.setCommentText(newText);
        assertEquals(newText, comment.getCommentText());
    }

    /**
     * Tests setting and getting the user ID.
     */
    @Test
    public void testSetAndGetUserId() {
        String newUserId = "newUser123";
        comment.setUserId(newUserId);
        assertEquals(newUserId, comment.getUserId());
    }

    /**
     * Tests setting and getting the user name.
     */
    @Test
    public void testSetAndGetUserName() {
        String newUserName = "New User Name";
        comment.setUserName(newUserName);
        assertEquals(newUserName, comment.getUserName());
    }

    /**
     * Tests setting and getting the device ID.
     */
    @Test
    public void testSetAndGetUserDeviceId() {
        String newDeviceId = "newDeviceABC";
        comment.setUserDeviceId(newDeviceId);
        assertEquals(newDeviceId, comment.getUserDeviceId());
    }

    /**
     * Tests setting and getting the timestamp.
     */
    @Test
    public void testSetAndGetTimestamp() {
        Timestamp timestamp = new Timestamp(new Date());
        comment.setTimestamp(timestamp);
        assertEquals(timestamp, comment.getTimestamp());
    }

    /**
     * Tests setting and getting the report count.
     */
    @Test
    public void testSetAndGetReportCount() {
        comment.setReportCount(5);
        assertEquals(5, comment.getReportCount());

        comment.setReportCount(0);
        assertEquals(0, comment.getReportCount());
    }

    /**
     * Tests setting and getting the reportedBy list.
     */
    @Test
    public void testSetAndGetReportedBy() {
        List<String> reportedByList = new ArrayList<>();
        reportedByList.add(TEST_USER_ID);
        reportedByList.add(ANOTHER_USER_ID);

        comment.setReportedBy(reportedByList);
        assertEquals(reportedByList, comment.getReportedBy());
        assertEquals(2, comment.getReportedBy().size());
    }

    /**
     * Tests that adding a user to the reportedBy list works correctly.
     */
    @Test
    public void testAddReportedByUser_addsUserToList() {
        comment.addReportedByUser(ANOTHER_USER_ID);

        assertNotNull(comment.getReportedBy());
        assertTrue(comment.getReportedBy().contains(ANOTHER_USER_ID));
        assertEquals(1, comment.getReportedBy().size());
    }

    /**
     * Tests that adding the same user twice does not create duplicates.
     */
    @Test
    public void testAddReportedByUser_preventsDuplicates() {
        comment.addReportedByUser(ANOTHER_USER_ID);
        comment.addReportedByUser(ANOTHER_USER_ID);

        assertEquals(1, comment.getReportedBy().size());
        assertTrue(comment.getReportedBy().contains(ANOTHER_USER_ID));
    }

    /**
     * Tests that multiple different users can be added to the reportedBy list.
     */
    @Test
    public void testAddReportedByUser_addsMultipleUsers() {
        comment.addReportedByUser(ANOTHER_USER_ID);
        comment.addReportedByUser("user3");
        comment.addReportedByUser("user4");

        assertEquals(3, comment.getReportedBy().size());
        assertTrue(comment.getReportedBy().contains(ANOTHER_USER_ID));
        assertTrue(comment.getReportedBy().contains("user3"));
        assertTrue(comment.getReportedBy().contains("user4"));
    }

    /**
     * Tests that addReportedByUser handles null reportedBy list gracefully.
     * When reportedBy is set to null, addReportedByUser should initialize it.
     */
    @Test
    public void testAddReportedByUser_handlesNullList() {
        comment.setReportedBy(null);
        comment.addReportedByUser(ANOTHER_USER_ID);

        assertNotNull(comment.getReportedBy());
        assertTrue(comment.getReportedBy().contains(ANOTHER_USER_ID));
        assertEquals(1, comment.getReportedBy().size());
    }

    /**
     * Tests that isReportedByCurrentUser returns true when user has reported.
     */
    @Test
    public void testIsReportedByCurrentUser_returnsTrueWhenUserReported() {
        comment.addReportedByUser(ANOTHER_USER_ID);

        assertTrue(comment.isReportedByCurrentUser(ANOTHER_USER_ID));
    }

    /**
     * Tests that isReportedByCurrentUser returns false when user has not reported.
     */
    @Test
    public void testIsReportedByCurrentUser_returnsFalseWhenUserNotReported() {
        comment.addReportedByUser(ANOTHER_USER_ID);

        assertFalse(comment.isReportedByCurrentUser("nonReportingUser"));
    }

    /**
     * Tests that isReportedByCurrentUser returns false when reportedBy is null.
     */
    @Test
    public void testIsReportedByCurrentUser_returnsFalseWhenReportedByIsNull() {
        comment.setReportedBy(null);

        assertFalse(comment.isReportedByCurrentUser(ANOTHER_USER_ID));
    }

    /**
     * Tests that isReportedByCurrentUser returns false when userId is null.
     */
    @Test
    public void testIsReportedByCurrentUser_returnsFalseWhenUserIdIsNull() {
        comment.addReportedByUser(ANOTHER_USER_ID);

        assertFalse(comment.isReportedByCurrentUser(null));
    }

    /**
     * Tests that isReportedByCurrentUser handles null values gracefully.
     */
    @Test
    public void testIsReportedByCurrentUser_handlesNullInput() {
        assertFalse(comment.isReportedByCurrentUser(null));
    }

    /**
     * Tests that comment can handle null values in text fields.
     */
    @Test
    public void testComment_handlesNullValues() {
        Comment nullComment = new Comment(null, null, null, null, null);

        assertNull(nullComment.getEventId());
        assertNull(nullComment.getCommentText());
        assertNull(nullComment.getUserId());
        assertNull(nullComment.getUserName());
        assertNull(nullComment.getUserDeviceId());
        assertEquals(0, nullComment.getReportCount());
        assertNotNull(nullComment.getReportedBy());
        assertTrue(nullComment.getReportedBy().isEmpty());
    }

    /**
     * Tests that comment can handle empty strings in text fields.
     */
    @Test
    public void testComment_handlesEmptyStrings() {
        Comment emptyComment = new Comment("", "", "", "", "");

        assertEquals("", emptyComment.getEventId());
        assertEquals("", emptyComment.getCommentText());
        assertEquals("", emptyComment.getUserId());
        assertEquals("", emptyComment.getUserName());
        assertEquals("", emptyComment.getUserDeviceId());
    }

    /**
     * Tests that adding a user to reportedBy list works even when reportedBy is initialized empty.
     */
    @Test
    public void testAddReportedByUser_worksWithEmptyList() {
        assertNotNull(comment.getReportedBy());
        assertTrue(comment.getReportedBy().isEmpty());

        comment.addReportedByUser(ANOTHER_USER_ID);

        assertEquals(1, comment.getReportedBy().size());
        assertTrue(comment.getReportedBy().contains(ANOTHER_USER_ID));
    }

    /**
     * Tests that report count can be incremented and decremented.
     */
    @Test
    public void testReportCount_incrementAndDecrement() {
        comment.setReportCount(0);
        comment.setReportCount(comment.getReportCount() + 1);
        assertEquals(1, comment.getReportCount());

        comment.setReportCount(comment.getReportCount() + 2);
        assertEquals(3, comment.getReportCount());

        comment.setReportCount(comment.getReportCount() - 1);
        assertEquals(2, comment.getReportCount());
    }
}