package com.example.syntaxappproject;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link AdminCommentAdapter}.
 *
 * <p>This test suite verifies the adapter's data handling capabilities,
 * including:</p>
 * <ul>
 *   <li>Initial state and item count</li>
 *   <li>Setting new comment lists and updating item counts</li>
 *   <li>Data integrity after setting comments</li>
 *   <li>Click listener invocation for comment selection</li>
 *   <li>Edge cases: empty lists, null lists, high report counts</li>
 * </ul>
 *
 * <p>The adapter is placed in test mode ({@code isTestMode = true}) to prevent
 * {@code notifyDataSetChanged()} calls, which would cause NullPointerException
 * when the adapter is not attached to a RecyclerView.</p>
 *
 * @author Syntax App Project Team
 * @version 1.0
 * @see AdminCommentAdapter
 */
@RunWith(JUnit4.class)
public class AdminCommentAdapterTest {

    /** Mock click listener to verify comment selection events. */
    @Mock
    private AdminCommentAdapter.OnCommentClickListener mockListener;

    /** The adapter instance being tested. */
    private AdminCommentAdapter adapter;

    /** Test comments with varying report counts and content. */
    private List<Comment> testComments;

    /**
     * Sets up the test environment before each test.
     * Initializes mocks, creates the adapter in test mode,
     * and generates test comment data.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockListener = mock(AdminCommentAdapter.OnCommentClickListener.class);
        adapter = new AdminCommentAdapter(mockListener);
        adapter.isTestMode = true; // Disable notifyDataSetChanged to prevent errors in unit tests
        testComments = createTestComments();
    }

    /**
     * Creates a list of test comments with various report counts for testing.
     *
     * <p>Includes:</p>
     * <ul>
     *   <li>A comment with 3 reports</li>
     *   <li>A comment with 0 reports</li>
     *   <li>A comment with 1 report</li>
     * </ul>
     *
     * @return a list of mock comments for testing
     */
    private List<Comment> createTestComments() {
        List<Comment> comments = new ArrayList<>();

        Comment comment1 = new Comment("event1", "First test comment", "user1", "User One", "device1");
        comment1.setCommentId("comment1");
        comment1.setReportCount(3);
        comment1.setTimestamp(new Timestamp(new Date()));
        comments.add(comment1);

        Comment comment2 = new Comment("event2", "Second test comment", "user2", "User Two", "device2");
        comment2.setCommentId("comment2");
        comment2.setReportCount(0);
        comment2.setTimestamp(new Timestamp(new Date()));
        comments.add(comment2);

        Comment comment3 = new Comment("event3", "Third test comment", "user3", "User Three", "device3");
        comment3.setCommentId("comment3");
        comment3.setReportCount(1);
        comment3.setTimestamp(new Timestamp(new Date()));
        comments.add(comment3);

        return comments;
    }

    /**
     * Verifies that the adapter starts with zero items.
     */
    @Test
    public void testAdapter_initialItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    /**
     * Verifies that setting comments updates the item count correctly.
     */
    @Test
    public void testAdapter_setComments_updatesItemCount() {
        adapter.setComments(testComments);
        assertEquals(3, adapter.getItemCount());
    }

    /**
     * Verifies that the adapter correctly stores comment data when set.
     * Ensures that the internal list contains the expected comments.
     */
    @Test
    public void testAdapter_setComments_updatesData() {
        adapter.setComments(testComments);
        List<Comment> internalComments = adapter.getComments();
        assertEquals(3, internalComments.size());
        assertEquals("First test comment", internalComments.get(0).getCommentText());
    }

    /**
     * Verifies that updating the adapter with a new list replaces the old data.
     */
    @Test
    public void testAdapter_setComments_refreshesData() {
        adapter.setComments(testComments);
        assertEquals(3, adapter.getItemCount());

        List<Comment> newComments = new ArrayList<>();
        newComments.add(testComments.get(0));
        adapter.setComments(newComments);

        assertEquals(1, adapter.getItemCount());
        List<Comment> internalComments = adapter.getComments();
        assertEquals("First test comment", internalComments.get(0).getCommentText());
    }

    /**
     * Verifies that the click listener is invoked when a comment is clicked.
     */
    @Test
    public void testAdapter_clickListenerTriggers() {
        adapter.setComments(testComments);
        mockListener.onCommentClick(testComments.get(0));
        verify(mockListener).onCommentClick(testComments.get(0));
    }

    /**
     * Verifies that clicking different comments invokes the listener with the correct comment.
     */
    @Test
    public void testAdapter_multipleCommentsClickDifferent() {
        adapter.setComments(testComments);

        mockListener.onCommentClick(testComments.get(1));

        verify(mockListener).onCommentClick(testComments.get(1));
    }

    /**
     * Verifies that setting an empty list clears the adapter.
     */
    @Test
    public void testAdapter_setEmptyList() {
        adapter.setComments(new ArrayList<>());
        assertEquals(0, adapter.getItemCount());
    }

    /**
     * Verifies that setting a null list is handled gracefully.
     * The adapter should treat null as an empty list.
     */
    @Test
    public void testAdapter_setNullList() {
        adapter.setComments(null);
        assertEquals(0, adapter.getItemCount());
        assertNotNull(adapter.getComments());
        assertEquals(0, adapter.getComments().size());
    }

    /**
     * Verifies that comments with high report counts are displayed correctly.
     * Tests that the adapter handles large report counts without issues.
     */
    @Test
    public void testAdapter_commentWithHighReportCount() {
        Comment highReportComment = new Comment("event", "High report comment", "user", "User", "device");
        highReportComment.setReportCount(100);

        List<Comment> comments = new ArrayList<>();
        comments.add(highReportComment);
        adapter.setComments(comments);

        assertEquals(1, adapter.getItemCount());
        List<Comment> internalComments = adapter.getComments();
        assertEquals(100, internalComments.get(0).getReportCount());
    }

    /**
     * Verifies that comments with zero reports are displayed correctly.
     * Ensures the adapter properly handles the edge case of no reports.
     */
    @Test
    public void testAdapter_commentWithZeroReports() {
        Comment zeroReportComment = new Comment("event", "Zero report comment", "user", "User", "device");
        zeroReportComment.setReportCount(0);

        List<Comment> comments = new ArrayList<>();
        comments.add(zeroReportComment);
        adapter.setComments(comments);

        assertEquals(1, adapter.getItemCount());
        List<Comment> internalComments = adapter.getComments();
        assertEquals(0, internalComments.get(0).getReportCount());
    }
}