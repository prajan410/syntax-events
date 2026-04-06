package com.example.syntaxappproject;

import android.location.Criteria;
import android.media.Image;

import java.util.List;

/**
 * Model class representing the detail of an event in the SyntaxEvents application.
 * <p>
 * Stores event id, organizer uid, event name, description, location, capacity,
 * geoReq, event start and end date, start and end registration period,
 * wait list count, lottery criteria, and poster. This class is used
 * as a Firestore data model and is serialized/deserialized directly via
 * {@code DocumentSnapshot.toObject(EventDetail.class)}.
 * </p>
 */
public class EventDetail {
    private String eventId;
    private String organizerUid;

    private String name;
    private String description;
    private String location;
    private long capacity;
    private boolean geoReq;

    private String startingEventDate;
    private String endingEventDate;

    private String startingRegistrationPeriod;
    private String endingRegistrationPeriod;

    private long waitlistCount;
    private String lotteryCriteria;
    private String poster;

    // ─── Private Event ────────────────────────────────────────────────────────────────
    private boolean privateEvent;
    private List<String> invitedUserIds;
    /**
     * Required no-argument constructor for Firestore deserialization.
     */
    public EventDetail() {}

    /**
     * Constructs a fully initialized Profile.
     *
     * @param eventId                       the id of the event
     * @param name                          the name of the event
     * @param description                   the description of the event
     * @param location                      the location of the event
     * @param capacity                      the capacity of the event
     * @param geoReq                        {@code true} if the event require geolocation
     * @param startingEventDate             the start date of the event
     * @param endingEventDate               the end date of the event
     * @param startingRegistrationPeriod    the start date of registration
     * @param endingRegistrationPeriod      the end date of registration
     * @param waitlistCount                 the number of people in the wait list
     * @param lotteryCriteria               the criteria of lottery
     * @param poster                        the poster of the event
     */
    public EventDetail(String eventId, String name, String description, String location, long capacity, boolean geoReq,
                       String startingEventDate, String endingEventDate,
                       String startingRegistrationPeriod, String endingRegistrationPeriod,
                       long waitlistCount, String lotteryCriteria, String poster) {
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.location = location;
        this.capacity = capacity;
        this.geoReq = geoReq;
        this.startingEventDate = startingEventDate;
        this.endingEventDate = endingEventDate;
        this.startingRegistrationPeriod = startingRegistrationPeriod;
        this.endingRegistrationPeriod = endingRegistrationPeriod;
        this.waitlistCount = waitlistCount;
        this.lotteryCriteria = lotteryCriteria;
        this.poster = poster;
    }


    /**
     * Returns the event id.
     *
     * @return the event id
     */
    public String getEventId() { return eventId; }
    /**
     * Sets the event id.
     *
     * @param eventId the event id to assign
     */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /**
     * Returns the name of the event.
     *
     * @return the event name
     */
    public String getName() { return name; }
    /**
     * Sets the name of the event.
     *
     * @param name the event name to assign
     */
    public void setName(String name) { this.name = name; }
    /**
     * Returns the uid of the organizer of the event.
     *
     * @return the uid of the organizer
     */
    public String getOrganizerUid() { return organizerUid; }
    /**
     * Sets the uid of the organizer of the event.
     *
     * @param organizerUid the uid of the organizer to assign
     */
    public void setOrganizerUid(String organizerUid) { this.organizerUid = organizerUid; }

    /**
     * Returns the description of the event.
     *
     * @return the event description
     */
    public String getDescription() { return description; }
    /**
     * Sets the description of the event.
     *
     * @param description the event description to assign
     */
    public void setDescription(String description) { this.description = description; }

    /**
     * Returns the location of the event.
     *
     * @return the event location
     */
    public String getLocation() { return location; }
    /**
     * Sets the location of the event.
     *
     * @param location the event location to assign
     */
    public void setLocation(String location) { this.location = location; }

    /**
     * Returns the capacity of the event.
     *
     * @return the event capacity
     */
    public long getCapacity() { return capacity; }
    /**
     * Sets the capacity of the event.
     *
     * @param capacity the event capacity to assign
     */
    public void setCapacity(long capacity) { this.capacity = capacity; }

    /**
     * Returns whether the event require geolocation
     *
     * @return {@code true} if the event require geolocation
     */
    public boolean isGeoReq() { return geoReq; }
    /**
     * Sets the requirement of geolocation of the event.
     *
     * @param geoReq {@code true} to assign geolocation require
     */
    public void setGeoReq(boolean geoReq) { this.geoReq = geoReq; }


    /**
     * Returns the state date of the event.
     *
     * @return the event start date
     */
    public String getStartingEventDate() { return startingEventDate; }
    /**
     * Sets the state date of the event.
     *
     * @param startingEventDate the event start date to assign
     */
    public void setStartingEventDate(String startingEventDate) { this.startingEventDate = startingEventDate; }

    /**
     * Returns the end date of the event.
     *
     * @return the event end date
     */
    public String getEndingEventDate() { return endingEventDate; }
    /**
     * Sets the end date of the event.
     *
     * @param endingEventDate the event end date to assign
     */
    public void setEndingEventDate(String endingEventDate) { this.endingEventDate = endingEventDate; }

    /**
     * Returns the start date of the registration.
     *
     * @return the registration start date
     */
    public String getStartingRegistrationPeriod() { return startingRegistrationPeriod; }
    /**
     * Sets the start date of the registration.
     *
     * @param startingRegistrationPeriod the registration start date to assign
     */
    public void setStartingRegistrationPeriod(String startingRegistrationPeriod) { this.startingRegistrationPeriod = startingRegistrationPeriod; }

    /**
     * Returns the end date of the registration.
     *
     * @return the registration end date
     */
    public String getEndingRegistrationPeriod() { return endingRegistrationPeriod; }
    /**
     * Sets the end date of the registration.
     *
     * @param endingRegistrationPeriod the registration end date to assign
     */
    public void setEndingRegistrationPeriod(String endingRegistrationPeriod) { this.endingRegistrationPeriod = endingRegistrationPeriod; }

    /**
     * Returns the count of people in the event wait list
     *
     * @return the count of people
     */
    public long getWaitlistCount() { return waitlistCount; }
    /**
get     * Sets the count of people in the event wait list
     *
     * @param waitlistCount the count of people to assign
     */
    public void setWaitlistCount(long waitlistCount) { this.waitlistCount = waitlistCount; }

    /**
     * Returns the criteria of lottery
     *
     * @return the lottery criteria
     */
    public String getLotteryCriteria() { return lotteryCriteria; }
    /**
     * Sets the criteria of lottery
     *
     * @param lotteryCriteria the lottery criteria to assign
     */
    public void setLotteryCriteria(String lotteryCriteria) { this.lotteryCriteria = lotteryCriteria; }

    /**
     * Returns the poster uri of the event
     *
     * @return the poster uri
     */
    public String getPoster() { return poster; }
    /**
     * Sets the poster uri of the event
     *
     * @param poster the poster uri to assign
     */
    //Private Event
    public void setPoster(String poster) { this.poster = poster; }

    /**
     * Return if the event is a private events.
     * @return true if the event is a private events
     */
    public boolean isPrivateEvent() {
        return privateEvent;
    }

    /**
     * Set the event as private event
     * @param privateEvent
     */

    public void setPrivateEvent(boolean privateEvent) {
        this.privateEvent = privateEvent;
    }

    public List<String> getInvitedUserIds() {
        return invitedUserIds;
    }

    public void setInvitedUserIds(List<String> invitedUserIds) {
        this.invitedUserIds = invitedUserIds;
    }
}
