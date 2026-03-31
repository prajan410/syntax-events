package com.example.syntaxappproject;

import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for the {@link Invitation} model class.
 * <p>
 * Verifies that invitation information is correctly stored and retrieved
 * through constructors, getters, and setters.
 * </p>
 */
public class InvitationTest {

    private Invitation invitation;
    private Timestamp invitedAt;
    private Timestamp responseAt;

    /**
     * Initializes common test data before each test.
     */
    @Before
    public void setUp() {
        invitedAt = Timestamp.now();
        responseAt = Timestamp.now();

        invitation = new Invitation(
                "inv1",
                "event1",
                "Swim Lessons",
                "user1",
                "pending",
                invitedAt,
                responseAt
        );
    }

    /**
     * Verifies that the empty constructor creates a valid {@link Invitation} object.
     */
    @Test
    public void testEmptyConstructor() {
        Invitation emptyInvitation = new Invitation();
        assertNotNull(emptyInvitation);
    }

    /**
     * Verifies that the parameterized constructor initializes invitation fields correctly.
     */
    @Test
    public void testConstructor() {
        assertEquals("inv1", invitation.getInvitationId());
        assertEquals("event1", invitation.getEventId());
        assertEquals("Swim Lessons", invitation.getEventName());
        assertEquals("user1", invitation.getUserId());
        assertEquals("pending", invitation.getStatus());
        assertEquals(invitedAt, invitation.getInvitedAt());
        assertEquals(responseAt, invitation.getResponseAt());
    }

    /**
     * Verifies that the setter and getter of invitation id work correctly.
     */
    @Test
    public void testSetAndGetInvitationId() {
        invitation.setInvitationId("inv2");
        assertEquals("inv2", invitation.getInvitationId());
    }

    /**
     * Verifies that the setter and getter of event id work correctly.
     */
    @Test
    public void testSetAndGetEventId() {
        invitation.setEventId("event2");
        assertEquals("event2", invitation.getEventId());
    }

    /**
     * Verifies that the setter and getter of event name work correctly.
     */
    @Test
    public void testSetAndGetEventName() {
        invitation.setEventName("Basketball Event");
        assertEquals("Basketball Event", invitation.getEventName());
    }

    /**
     * Verifies that the setter and getter of user id work correctly.
     */
    @Test
    public void testSetAndGetUserId() {
        invitation.setUserId("user2");
        assertEquals("user2", invitation.getUserId());
    }

    /**
     * Verifies that the setter and getter of status work correctly.
     */
    @Test
    public void testSetAndGetStatus() {
        invitation.setStatus("accepted");
        assertEquals("accepted", invitation.getStatus());
    }

    /**
     * Verifies that the setter and getter of invitedAt work correctly.
     */
    @Test
    public void testSetAndGetInvitedAt() {
        Timestamp newInvitedAt = Timestamp.now();
        invitation.setInvitedAt(newInvitedAt);
        assertEquals(newInvitedAt, invitation.getInvitedAt());
    }

    /**
     * Verifies that the setter and getter of responseAt work correctly.
     */
    @Test
    public void testSetAndGetResponseAt() {
        Timestamp newResponseAt = Timestamp.now();
        invitation.setResponseAt(newResponseAt);
        assertEquals(newResponseAt, invitation.getResponseAt());
    }
}