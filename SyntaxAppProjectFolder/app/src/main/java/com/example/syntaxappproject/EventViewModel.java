package com.example.syntaxappproject;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

/**
 * Shared ViewModel for the event creation flow.
 *
 * <p>This ViewModel acts as a central data store for the multi-step event creation process.
 * It allows the three event creation fragments ({@link ui.CreateEventFragment},
 * {@link ui.CreateEventUploadPosterFragment}, and {@link ui.CreateEventQRFragment})
 * to share event data across navigation steps without losing state.</p>
 *
 * <p>Data persists across fragment transitions and configuration changes (such as screen
 * rotation) until the event is successfully created and the user navigates away.</p>
 *
 * <p>The ViewModel stores all event-related data including:</p>
 * <ul>
 *   <li>Basic event information (name, description, location, capacity)</li>
 *   <li>Date information (event dates, registration period dates)</li>
 *   <li>Lottery criteria and geolocation requirements</li>
 *   <li>Media references (poster image URI, QR code data)</li>
 *   <li>Private event settings and invited user IDs</li>
 * </ul>
 *
 * <p>All fields are exposed as {@link LiveData} to allow fragments to observe changes
 * and react accordingly, while setters use {@link MutableLiveData#setValue(Object)} to
 * update the data.</p>
 *
 * @see androidx.lifecycle.ViewModel
 * @see ui.CreateEventFragment
 * @see ui.CreateEventUploadPosterFragment
 * @see ui.CreateEventQRFragment
 */
public class EventViewModel extends ViewModel {

    /** Firestore document ID of the event being created. Set after successful creation. */
    private final MutableLiveData<String> eventId = new MutableLiveData<>();

    /** Lottery criteria text (user-defined requirements for lottery selection). */
    private final MutableLiveData<String> lotteryCriteria = new MutableLiveData<>("");

    /** Geographic latitude of the event location (used for geolocation validation). */
    private final MutableLiveData<Double> eventLatitude = new MutableLiveData<>(0.0);

    /** Geographic longitude of the event location (used for geolocation validation). */
    private final MutableLiveData<Double> eventLongitude = new MutableLiveData<>(0.0);

    /** Name/title of the event. */
    private final MutableLiveData<String> name = new MutableLiveData<>();

    /** Detailed description of the event. */
    private final MutableLiveData<String> description = new MutableLiveData<>();

    /** Physical location/address of the event (city name or address). */
    private final MutableLiveData<String> location = new MutableLiveData<>();

    /** Maximum number of entrants allowed on the waitlist. */
    private final MutableLiveData<Integer> capacity = new MutableLiveData<>();

    /** Whether geolocation is required to join this event (users must be within 50km). */
    private final MutableLiveData<Boolean> geoReq = new MutableLiveData<>();

    /** Start date of the event (format: YYYY-MM-DD). */
    private final MutableLiveData<String> startingEventDate = new MutableLiveData<>();

    /** End date of the event (format: YYYY-MM-DD). */
    private final MutableLiveData<String> endingEventDate = new MutableLiveData<>();

    /** Start date of the registration period (format: YYYY-MM-DD). */
    private final MutableLiveData<String> startingRegistrationPeriod = new MutableLiveData<>();

    /** End date of the registration period (format: YYYY-MM-DD). */
    private final MutableLiveData<String> endingRegistrationPeriod = new MutableLiveData<>();

    /** URI of the selected event poster image (local file reference before upload). */
    private final MutableLiveData<Uri> imageUri = new MutableLiveData<>();

    /** Base64-encoded QR code data generated for the event. */
    private final MutableLiveData<String> qrCodeData = new MutableLiveData<>();

    /** Whether this is a private event (not visible in public listings, requires invitation). */
    private final MutableLiveData<Boolean> privateEvent = new MutableLiveData<>(false);

    /** List of user IDs invited to this private event. */
    private final MutableLiveData<ArrayList<String>> invitedUserIds = new MutableLiveData<>(new ArrayList<>());

    /**
     * Returns the Firestore document ID of the event being created.
     *
     * @return LiveData containing the event ID
     */
    public LiveData<String> getEventId() { return eventId; }

    /**
     * Sets the Firestore document ID of the event being created.
     *
     * @param id the Firestore document ID
     */
    public void setEventId(String id) { eventId.setValue(id); }

    /**
     * Returns the name/title of the event.
     *
     * @return LiveData containing the event name
     */
    public LiveData<String> getName() { return name; }

    /**
     * Sets the name/title of the event.
     *
     * @param n the event name
     */
    public void setName(String n) { name.setValue(n); }

    /**
     * Returns the detailed description of the event.
     *
     * @return LiveData containing the event description
     */
    public LiveData<String> getDescription() { return description; }

    /**
     * Sets the detailed description of the event.
     *
     * @param d the event description
     */
    public void setDescription(String d) { description.setValue(d); }

    /**
     * Returns the physical location/address of the event.
     *
     * @return LiveData containing the event location
     */
    public LiveData<String> getLocation() { return location; }

    /**
     * Sets the physical location/address of the event.
     *
     * @param loc the event location (city name or address)
     */
    public void setLocation(String loc) { location.setValue(loc); }

    /**
     * Returns the lottery criteria text.
     *
     * @return LiveData containing the lottery criteria
     */
    public LiveData<String> getLotteryCriteria() { return lotteryCriteria; }

    /**
     * Sets the lottery criteria text.
     *
     * @param criteria the lottery criteria (user-defined requirements)
     */
    public void setLotteryCriteria(String criteria) { lotteryCriteria.setValue(criteria); }

    /**
     * Returns the maximum waitlist capacity.
     *
     * @return LiveData containing the capacity
     */
    public LiveData<Integer> getCapacity() { return capacity; }

    /**
     * Sets the maximum waitlist capacity.
     *
     * @param i the capacity value (number of entrants allowed)
     */
    public void setCapacity(int i) { capacity.setValue(i); }

    /**
     * Returns whether geolocation is required for this event.
     *
     * @return LiveData containing the geolocation requirement flag
     */
    public LiveData<Boolean> getGeoReq() { return geoReq; }

    /**
     * Sets whether geolocation is required for this event.
     *
     * @param geo true if users must be within 50km of event location to join
     */
    public void setGeoReq(Boolean geo) { geoReq.setValue(geo); }

    /**
     * Returns the geographic latitude of the event location.
     *
     * @return LiveData containing the event latitude
     */
    public LiveData<Double> getEventLatitude() { return eventLatitude; }

    /**
     * Sets the geographic latitude of the event location.
     *
     * @param lat the latitude coordinate (used for distance validation)
     */
    public void setEventLatitude(Double lat) { eventLatitude.setValue(lat); }

    /**
     * Returns the geographic longitude of the event location.
     *
     * @return LiveData containing the event longitude
     */
    public LiveData<Double> getEventLongitude() { return eventLongitude; }

    /**
     * Sets the geographic longitude of the event location.
     *
     * @param lng the longitude coordinate (used for distance validation)
     */
    public void setEventLongitude(Double lng) { eventLongitude.setValue(lng); }

    /**
     * Returns the event start date.
     *
     * @return LiveData containing the start date (YYYY-MM-DD format)
     */
    public LiveData<String> getStartingEventDate() { return startingEventDate; }

    /**
     * Sets the event start date.
     *
     * @param date the start date in YYYY-MM-DD format
     */
    public void setStartingEventDate(String date) { startingEventDate.setValue(date); }

    /**
     * Returns the event end date.
     *
     * @return LiveData containing the end date (YYYY-MM-DD format)
     */
    public LiveData<String> getEndingEventDate() { return endingEventDate; }

    /**
     * Sets the event end date.
     *
     * @param date the end date in YYYY-MM-DD format
     */
    public void setEndingEventDate(String date) { endingEventDate.setValue(date); }

    /**
     * Returns the registration period start date.
     *
     * @return LiveData containing the registration start date (YYYY-MM-DD format)
     */
    public LiveData<String> getStartingRegistrationPeriod() { return startingRegistrationPeriod; }

    /**
     * Sets the registration period start date.
     *
     * @param date the registration start date in YYYY-MM-DD format
     */
    public void setStartingRegistrationPeriod(String date) { startingRegistrationPeriod.setValue(date); }

    /**
     * Returns the registration period end date.
     *
     * @return LiveData containing the registration end date (YYYY-MM-DD format)
     */
    public LiveData<String> getEndingRegistrationPeriod() { return endingRegistrationPeriod; }

    /**
     * Sets the registration period end date.
     *
     * @param date the registration end date in YYYY-MM-DD format
     */
    public void setEndingRegistrationPeriod(String date) { endingRegistrationPeriod.setValue(date); }

    /**
     * Returns the URI of the selected event poster image.
     *
     * @return LiveData containing the image URI (local file reference)
     */
    public LiveData<Uri> getImageUri() { return imageUri; }

    /**
     * Sets the URI of the selected event poster image.
     *
     * @param uri the local URI of the selected image
     */
    public void setImageUri(Uri uri) { imageUri.setValue(uri); }

    /**
     * Returns the Base64-encoded QR code data.
     *
     * @return LiveData containing the QR code data string
     */
    public LiveData<String> getQrCodeData() { return qrCodeData; }

    /**
     * Sets the Base64-encoded QR code data.
     *
     * @param qr the Base64-encoded QR code string
     */
    public void setQrCodeData(String qr) { qrCodeData.setValue(qr); }

    /**
     * Returns whether this is a private event.
     *
     * @return LiveData containing the private event flag
     */
    public LiveData<Boolean> getPrivateEvent() { return privateEvent; }

    /**
     * Sets whether this is a private event.
     * <p>
     * Private events are not visible in public event listings and require
     * invitations to join the waiting list.
     * </p>
     *
     * @param value true for private event, false for public event
     */
    public void setPrivateEvent(Boolean value) { privateEvent.setValue(value); }

    /**
     * Returns the list of user IDs invited to this private event.
     *
     * @return LiveData containing the list of invited user IDs
     */
    public LiveData<ArrayList<String>> getInvitedUserIds() { return invitedUserIds; }

    /**
     * Sets the list of user IDs invited to this private event.
     *
     * @param ids the list of user IDs to invite
     */
    public void setInvitedUserIds(ArrayList<String> ids) { invitedUserIds.setValue(ids); }
}