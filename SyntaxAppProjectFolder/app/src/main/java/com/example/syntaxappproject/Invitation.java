package com.example.syntaxappproject;

import com.google.firebase.Timestamp;

/**
 * Model class that represents an invitation sent to an entrant for an event.
 *
 * <p>Invitations are used for two main scenarios:</p>
 * <ul>
 *   <li><b>Lottery Wins:</b> When an entrant is selected through the lottery system,
 *       they receive a "lottery_win" invitation to sign up for the event.</li>
 *   <li><b>Private Events:</b> When an organizer invites specific entrants to a
 *       private event, they receive a "private_event" invitation to join the waitlist.</li>
 * </ul>
 *
 * <p>Each invitation tracks the entrant's response (accept/decline) and maintains
 * a status field that can be one of:</p>
 * <ul>
 *   <li><b>pending</b> - Invitation sent, awaiting user response</li>
 *   <li><b>accepted</b> - User accepted the invitation</li>
 *   <li><b>declined</b> - User declined the invitation</li>
 *   <li><b>not_chosen</b> - Used for lottery losers (notification only)</li>
 * </ul>
 *
 * <p>Outstanding issues:</p>
 * <ul>
 *   <li>Currently only one pending invitation is shown in the UI at a time</li>
 *   <li>Organizer-side invitation creation is handled separately in
 *       {@link PrivateEventInvitationRepository} and {@link EventLotteryRepository}</li>
 * </ul>
 *
 * @see PrivateEventInvitationRepository
 * @see EventLotteryRepository
 * @see ui.NotificationFragment
 */
public class Invitation {

    /** Firestore document ID of this invitation. Set after successful creation. */
    private String invitationId;

    /** Firestore document ID of the event this invitation is for. */
    private String eventId;

    /** Name of the event (denormalized for display purposes). */
    private String eventName;

    /** ID of the user receiving this invitation. */
    private String userId;

    /** Current status of the invitation (pending, accepted, declined, not_chosen). */
    private String status;

    /** Timestamp when the invitation was sent. */
    private Timestamp invitedAt;

    /** Timestamp when the user responded (null if no response yet). */
    private Timestamp responseAt;

    /** Type of invitation: "lottery_win" or "private_event". */
    private String type;

    /**
     * Empty constructor required by Firestore for deserialization.
     * Firestore uses this constructor to create Invitation objects from document data.
     */
    public Invitation() {
    }

    /**
     * Constructor used when creating an invitation object manually.
     *
     * @param invitationId the Firestore document ID (can be null, will be set after save)
     * @param eventId      the ID of the event this invitation is for
     * @param eventName    the name of the event (denormalized for display)
     * @param userId       the ID of the invited user
     * @param status       the invitation status (pending, accepted, declined)
     * @param invitedAt    the timestamp when the invitation was sent
     * @param responseAt   the timestamp when the user responded (null if no response)
     */
    public Invitation(String invitationId, String eventId, String eventName,
                      String userId, String status,
                      Timestamp invitedAt, Timestamp responseAt) {
        this.invitationId = invitationId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.userId = userId;
        this.status = status;
        this.invitedAt = invitedAt;
        this.responseAt = responseAt;
    }


    /**
     * Returns the Firestore document ID of this invitation.
     *
     * @return the invitation ID
     */
    public String getInvitationId() {
        return invitationId;
    }

    /**
     * Sets the Firestore document ID of this invitation.
     * Typically called after the invitation is saved to Firestore.
     *
     * @param invitationId the invitation ID to set
     */
    public void setInvitationId(String invitationId) {
        this.invitationId = invitationId;
    }

    /**
     * Returns the Firestore document ID of the event this invitation is for.
     *
     * @return the event ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the Firestore document ID of the event this invitation is for.
     *
     * @param eventId the event ID to set
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Returns the name of the event (denormalized for display purposes).
     *
     * @return the event name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * Sets the name of the event.
     *
     * @param eventName the event name to set
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    /**
     * Returns the ID of the user receiving this invitation.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Sets the ID of the user receiving this invitation.
     *
     * @param userId the user ID to set
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Returns the current status of the invitation.
     * Possible values: "pending", "accepted", "declined", "not_chosen".
     *
     * @return the invitation status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the current status of the invitation.
     *
     * @param status the invitation status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the timestamp when the invitation was sent.
     *
     * @return the invitation timestamp
     */
    public Timestamp getInvitedAt() {
        return invitedAt;
    }

    /**
     * Sets the timestamp when the invitation was sent.
     *
     * @param invitedAt the invitation timestamp to set
     */
    public void setInvitedAt(Timestamp invitedAt) {
        this.invitedAt = invitedAt;
    }

    /**
     * Returns the type of invitation.
     * Possible values: "lottery_win" for lottery winner invitations,
     *                  "private_event" for private event invitations.
     *
     * @return the invitation type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of invitation.
     *
     * @param type the invitation type to set ("lottery_win" or "private_event")
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the timestamp when the user responded to the invitation.
     * Returns null if the user has not responded yet.
     *
     * @return the response timestamp, or null if no response
     */
    public Timestamp getResponseAt() {
        return responseAt;
    }

    /**
     * Sets the timestamp when the user responded to the invitation.
     *
     * @param responseAt the response timestamp to set
     */
    public void setResponseAt(Timestamp responseAt) {
        this.responseAt = responseAt;
    }
}