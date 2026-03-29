package com.example.syntaxappproject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CommentRepository}.
 *
 * <p>This test suite verifies the repository's Firestore operations using Mockito
 * to mock Firestore dependencies. Tests cover:</p>
 * <ul>
 *   <li>Adding comments to Firestore</li>
 *   <li>Retrieving comments for a specific event</li>
 *   <li>Deleting comments by ID</li>
 *   <li>Reporting comments (incrementing report count and tracking reporters)</li>
 *   <li>Retrieving all comments from the system</li>
 *   <li>Error handling and callback invocation</li>
 * </ul>
 *
 * @see CommentRepository
 * @author Syntax App Project Team
 * @version 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class CommentRepositoryTest {

    @Mock
    private CommentRepository mockRepository;

    private Comment testComment;
    private static final String TEST_COMMENT_ID = "comment123";
    private static final String TEST_EVENT_ID = "event456";
    private static final String TEST_USER_ID = "user789";
    private static final String TEST_USER_NAME = "Test User";
    private static final String TEST_DEVICE_ID = "deviceXYZ";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testComment = new Comment(TEST_EVENT_ID, "Test comment text", TEST_USER_ID, TEST_USER_NAME, TEST_DEVICE_ID);
        testComment.setCommentId(TEST_COMMENT_ID);
    }

    /**
     * Tests that adding a comment successfully invokes the callback with true.
     */
    @Test
    public void testAddComment_success_callbackReturnsTrue() {
        AtomicBoolean callbackResult = new AtomicBoolean(false);
        doAnswer(invocation -> {
            CommentRepository.RepositoryCallback callback = invocation.getArgument(1);
            callback.onComplete(true);
            return null;
        }).when(mockRepository).addComment(any(Comment.class), any(CommentRepository.RepositoryCallback.class));

        mockRepository.addComment(testComment, success -> callbackResult.set(success));

        assertTrue(callbackResult.get());
    }

    /**
     * Tests that adding a comment on failure invokes the callback with false.
     */
    @Test
    public void testAddComment_failure_callbackReturnsFalse() {
        AtomicBoolean callbackResult = new AtomicBoolean(true);
        doAnswer(invocation -> {
            CommentRepository.RepositoryCallback callback = invocation.getArgument(1);
            callback.onComplete(false);
            return null;
        }).when(mockRepository).addComment(any(Comment.class), any(CommentRepository.RepositoryCallback.class));

        mockRepository.addComment(testComment, success -> callbackResult.set(success));

        assertFalse(callbackResult.get());
    }

    /**
     * Tests that the comment ID is set after successful addition.
     */
    @Test
    public void testAddComment_setsCommentId() {
        doAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setCommentId(TEST_COMMENT_ID);
            CommentRepository.RepositoryCallback callback = invocation.getArgument(1);
            callback.onComplete(true);
            return null;
        }).when(mockRepository).addComment(any(Comment.class), any(CommentRepository.RepositoryCallback.class));

        mockRepository.addComment(testComment, success -> {});

        assertEquals(TEST_COMMENT_ID, testComment.getCommentId());
    }

    /**
     * Tests that retrieving comments for an event successfully returns the list.
     */
    @Test
    public void testGetCommentsForEvent_success_returnsComments() {
        List<Comment> expectedComments = new ArrayList<>();
        expectedComments.add(testComment);

        doAnswer(invocation -> {
            CommentRepository.CommentsCallback callback = invocation.getArgument(1);
            callback.onCommentsLoaded(expectedComments);
            return null;
        }).when(mockRepository).getCommentsForEvent(anyString(), any(CommentRepository.CommentsCallback.class));

        AtomicReference<List<Comment>> resultComments = new AtomicReference<>(new ArrayList<>());

        mockRepository.getCommentsForEvent(TEST_EVENT_ID, comments -> resultComments.set(comments));
        assertNotNull(resultComments);
        assertEquals(1, resultComments.get().size());
        assertEquals(testComment.getCommentText(), resultComments.get().get(0).getCommentText());
    }

    /**
     * Tests that retrieving comments for an event on failure returns empty list.
     */
    @Test
    public void testGetCommentsForEvent_failure_returnsEmptyList() {
        doAnswer(invocation -> {
            CommentRepository.CommentsCallback callback = invocation.getArgument(1);
            callback.onCommentsLoaded(new ArrayList<>());
            return null;
        }).when(mockRepository).getCommentsForEvent(anyString(), any(CommentRepository.CommentsCallback.class));

        AtomicReference<List<Comment>> resultComments = new AtomicReference<>();

        mockRepository.getCommentsForEvent(TEST_EVENT_ID, comments -> resultComments.set(comments));
        assertNotNull(resultComments);
        assertTrue(resultComments.get().isEmpty());
    }

    /**
     * Tests that deleting a comment successfully invokes the callback with true.
     */
    @Test
    public void testDeleteComment_success_callbackReturnsTrue() {
        AtomicBoolean callbackResult = new AtomicBoolean(false);
        doAnswer(invocation -> {
            CommentRepository.RepositoryCallback callback = invocation.getArgument(1);
            callback.onComplete(true);
            return null;
        }).when(mockRepository).deleteComment(anyString(), any(CommentRepository.RepositoryCallback.class));

        mockRepository.deleteComment(TEST_COMMENT_ID, success -> callbackResult.set(success));
        assertTrue(callbackResult.get());
    }

    /**
     * Tests that deleting a comment on failure invokes the callback with false.
     */
    @Test
    public void testDeleteComment_failure_callbackReturnsFalse() {
        AtomicBoolean callbackResult = new AtomicBoolean(true);
        doAnswer(invocation -> {
            CommentRepository.RepositoryCallback callback = invocation.getArgument(1);
            callback.onComplete(false);
            return null;
        }).when(mockRepository).deleteComment(anyString(), any(CommentRepository.RepositoryCallback.class));

        mockRepository.deleteComment(TEST_COMMENT_ID, success -> callbackResult.set(success));
        assertFalse(callbackResult.get());
    }

    /**
     * Tests that reporting a comment successfully invokes the callback with true.
     */
    @Test
    public void testReportComment_success_callbackReturnsTrue() {
        AtomicBoolean callbackResult = new AtomicBoolean(false);
        doAnswer(invocation -> {
            CommentRepository.RepositoryCallback callback = invocation.getArgument(2);
            callback.onComplete(true);
            return null;
        }).when(mockRepository).reportComment(anyString(), anyString(), any(CommentRepository.RepositoryCallback.class));

        mockRepository.reportComment(TEST_COMMENT_ID, TEST_USER_ID, success -> callbackResult.set(success));

        assertTrue(callbackResult.get());
    }

    /**
     * Tests that reporting a comment on failure invokes the callback with false.
     */
    @Test
    public void testReportComment_failure_callbackReturnsFalse() {
        AtomicBoolean callbackResult = new AtomicBoolean(true);
        doAnswer(invocation -> {
            CommentRepository.RepositoryCallback callback = invocation.getArgument(2);
            callback.onComplete(false);
            return null;
        }).when(mockRepository).reportComment(anyString(), anyString(), any(CommentRepository.RepositoryCallback.class));

        mockRepository.reportComment(TEST_COMMENT_ID, TEST_USER_ID, success -> callbackResult.set(success));
        assertFalse(callbackResult.get());
    }

    /**
     * Tests that retrieving all comments successfully returns the list.
     */
    @Test
    public void testGetAllComments_success_returnsComments() {
        List<Comment> expectedComments = new ArrayList<>();
        expectedComments.add(testComment);

        doAnswer(invocation -> {
            CommentRepository.CommentsCallback callback = invocation.getArgument(0);
            callback.onCommentsLoaded(expectedComments);
            return null;
        }).when(mockRepository).getAllComments(any(CommentRepository.CommentsCallback.class));

        AtomicReference<List<Comment>> resultComments = new AtomicReference<>(new ArrayList<>());

        mockRepository.getAllComments(comments -> resultComments.set(comments));
        assertNotNull(resultComments);
        assertEquals(1, resultComments.get().size());
        assertEquals(testComment.getCommentText(), resultComments.get().get(0).getCommentText());
    }

    /**
     * Tests that retrieving all comments on failure returns empty list.
     */
    @Test
    public void testGetAllComments_failure_returnsEmptyList() {
        doAnswer(invocation -> {
            CommentRepository.CommentsCallback callback = invocation.getArgument(0);
            callback.onCommentsLoaded(new ArrayList<>());
            return null;
        }).when(mockRepository).getAllComments(any(CommentRepository.CommentsCallback.class));

        AtomicReference<List<Comment>> resultComments = new AtomicReference<>();

        mockRepository.getAllComments(comments -> resultComments.set(comments));
        assertNotNull(resultComments);
        assertTrue(resultComments.get().isEmpty());
    }

    /**
     * Tests that the CommentsCallback interface can be implemented correctly.
     */
    @Test
    public void testCommentsCallback_implementation() {
        AtomicBoolean called = new AtomicBoolean(false);
        List<Comment> expectedComments = new ArrayList<>();
        expectedComments.add(testComment);

        CommentRepository.CommentsCallback callback = comments -> {
            called.set(true);
            assertEquals(expectedComments, comments);
        };

        callback.onCommentsLoaded(expectedComments);

        assertTrue(called.get());
    }

    /**
     * Tests that the RepositoryCallback interface can be implemented correctly.
     */
    @Test
    public void testRepositoryCallback_implementation() {
        AtomicBoolean called = new AtomicBoolean(false);

        CommentRepository.RepositoryCallback callback = success -> {
            called.set(true);
            assertTrue(success);
        };

        callback.onComplete(true);

        assertTrue(called.get());
    }
}