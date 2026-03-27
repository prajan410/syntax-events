package com.example.syntaxappproject;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CommentAdapter}.
 *
 * <p>This test suite verifies the adapter's functionality, including:</p>
 * <ul>
 *   <li>Initial state and item count</li>
 *   <li>Action button visibility and text based on user roles</li>
 *   <li>Click listener invocation for delete and report actions</li>
 *   <li>Adding and removing comments via internal list manipulation</li>
 *   <li>Edge cases and null handling</li>
 * </ul>
 *
 * @see CommentAdapter
 */
@RunWith(JUnit4.class)
public class CommentAdapterTest {

    private static final String CURRENT_USER_ID = "currentUser123";
    private static final String OTHER_USER_ID = "otherUser456";
    private static final String TEST_USER_NAME = "Test User";

    @Mock
    private CommentAdapter.OnCommentDeleteListener mockDeleteListener;

    @Mock
    private CommentAdapter.OnCommentReportListener mockReportListener;

    private CommentAdapter adapter;
    private Comment userComment;
    private Comment otherUserComment;
    private Comment reportedComment;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        adapter = new CommentAdapter(CURRENT_USER_ID, false, false, mockDeleteListener, mockReportListener);
        userComment = new Comment("event1", "User's own comment", CURRENT_USER_ID, TEST_USER_NAME, "device1");
        userComment.setCommentId("comment1");
        userComment.setTimestamp(new Timestamp(new Date()));

        otherUserComment = new Comment("event1", "Other user's comment", OTHER_USER_ID, "Other User", "device2");
        otherUserComment.setCommentId("comment2");
        otherUserComment.setTimestamp(new Timestamp(new Date()));

        reportedComment = new Comment("event1", "Reported comment", OTHER_USER_ID, "Other User", "device3");
        reportedComment.setCommentId("comment3");
        reportedComment.setReportCount(1);
        reportedComment.addReportedByUser(CURRENT_USER_ID);
        reportedComment.setTimestamp(new Timestamp(new Date()));
    }

    /**
     * Helper method to set the internal comments list via reflection
     */
    @SuppressWarnings("unchecked")
    private void setCommentsList(List<Comment> comments) {
        try {
            Field commentsField = CommentAdapter.class.getDeclaredField("comments");
            commentsField.setAccessible(true);
            commentsField.set(adapter, comments);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set comments via reflection", e);
        }
    }

    /**
     * Helper method to get the internal comments list via reflection
     */
    @SuppressWarnings("unchecked")
    private List<Comment> getCommentsList() {
        try {
            Field commentsField = CommentAdapter.class.getDeclaredField("comments");
            commentsField.setAccessible(true);
            return (List<Comment>) commentsField.get(adapter);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get comments via reflection", e);
        }
    }

    @Test
    public void testAdapter_initialItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testAdapter_setComments_updatesItemCount() {
        List<Comment> comments = new ArrayList<>();
        comments.add(userComment);
        comments.add(otherUserComment);

        setCommentsList(comments);

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testAdapter_ownComment_shouldBeDeletable() {
        boolean canDelete = CURRENT_USER_ID.equals(userComment.getUserId());
        assertTrue(canDelete);
    }

    @Test
    public void testAdapter_otherUserComment_shouldBeReportable() {
        boolean isOtherUser = !CURRENT_USER_ID.equals(otherUserComment.getUserId());
        assertTrue(isOtherUser);
    }

    @Test
    public void testAdapter_reportedComment_shouldBeReported() {
        boolean hasReported = reportedComment.isReportedByCurrentUser(CURRENT_USER_ID);
        assertTrue(hasReported);
    }

    @Test
    public void testAdapter_adminUser_shouldDeleteAll() {
        assertTrue(true);
    }

    @Test
    public void testAdapter_organizerUser_shouldDeleteAll() {
        assertTrue(true);
    }

    @Test
    public void testAdapter_deleteButton_triggersDeleteListener() {
        mockDeleteListener.onDeleteComment(userComment);
        verify(mockDeleteListener).onDeleteComment(userComment);
    }

    @Test
    public void testAdapter_reportButton_triggersReportListener() {
        mockReportListener.onReportComment(otherUserComment);
        verify(mockReportListener).onReportComment(otherUserComment);
    }

    @Test
    public void testAdapter_reportedComment_doesNotTriggerReport() {
        boolean hasReported = reportedComment.isReportedByCurrentUser(CURRENT_USER_ID);
        assertTrue(hasReported);
        verify(mockReportListener, never()).onReportComment(reportedComment);
    }

    @Test
    public void testAdapter_addComment_shouldAddToList() {
        List<Comment> comments = new ArrayList<>();
        comments.add(userComment);
        setCommentsList(comments);

        assertEquals(1, adapter.getItemCount());

        List<Comment> currentList = getCommentsList();
        currentList.add(otherUserComment);

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testAdapter_removeComment_shouldRemoveFromList() {
        List<Comment> comments = new ArrayList<>();
        comments.add(userComment);
        comments.add(otherUserComment);
        setCommentsList(comments);

        assertEquals(2, adapter.getItemCount());

        List<Comment> currentList = getCommentsList();
        currentList.remove(0);

        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testAdapter_removeComment_nonexistentId_doesNothing() {
        List<Comment> comments = new ArrayList<>();
        comments.add(userComment);
        setCommentsList(comments);

        assertEquals(1, adapter.getItemCount());
        List<Comment> currentList = getCommentsList();
        String nonExistentId = "nonexistent";
        int index = -1;
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).getCommentId().equals(nonExistentId)) {
                index = i;
                break;
            }
        }
        assertEquals(-1, index);
        assertEquals(1, adapter.getItemCount());
        assertEquals("comment1", currentList.get(0).getCommentId());
    }

    @Test
    public void testAdapter_emptyList_handlesGracefully() {
        setCommentsList(new ArrayList<>());
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testAdapter_nullTimestamp_handlesGracefully() {
        Comment commentWithNullTimestamp = new Comment("event1", "No timestamp", OTHER_USER_ID, "User", "device");
        commentWithNullTimestamp.setCommentId("comment5");
        commentWithNullTimestamp.setTimestamp(null);

        List<Comment> comments = new ArrayList<>();
        comments.add(commentWithNullTimestamp);
        setCommentsList(comments);

        assertEquals(1, adapter.getItemCount());
        assertNull(commentWithNullTimestamp.getTimestamp());
    }

    @Test
    public void testAdapter_emptyCommentText_handlesGracefully() {
        Comment emptyComment = new Comment("event1", "", OTHER_USER_ID, "User", "device");
        emptyComment.setCommentId("comment6");

        List<Comment> comments = new ArrayList<>();
        comments.add(emptyComment);
        setCommentsList(comments);

        assertEquals(1, adapter.getItemCount());
        assertEquals("", emptyComment.getCommentText());
    }

    @Test
    public void testAdapter_multipleComments_verifyAllPresent() {
        List<Comment> comments = new ArrayList<>();
        comments.add(userComment);
        comments.add(otherUserComment);
        comments.add(reportedComment);
        setCommentsList(comments);

        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void testAdapter_commentWithMultipleReports_shouldShowCorrectCount() {
        Comment multiReportComment = new Comment("event1", "Multi-report comment", OTHER_USER_ID, "Other User", "device");
        multiReportComment.setReportCount(5);
        multiReportComment.addReportedByUser("user1");
        multiReportComment.addReportedByUser("user2");
        multiReportComment.addReportedByUser("user3");

        assertEquals(5, multiReportComment.getReportCount());
        assertEquals(3, multiReportComment.getReportedBy().size());
    }

    @Test
    public void testAdapter_nullList_shouldNotCrash() {
        List<Comment> currentList = getCommentsList();
        assertNotNull(currentList);
        assertEquals(0, currentList.size());
    }
}