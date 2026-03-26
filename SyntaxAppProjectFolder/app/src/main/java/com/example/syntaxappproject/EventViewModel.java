package com.example.syntaxappproject;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;


/**
 * The purpose of this class is so that the 3 different fragments (to create an event) can share the same information of the event.
 */
public class EventViewModel extends ViewModel {

    // Identity
    private final MutableLiveData<String> eventId = new MutableLiveData<>();

    //Event Info
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> description = new MutableLiveData<>();
    private final MutableLiveData<String> location = new MutableLiveData<>();
    private final MutableLiveData<Integer> capacity = new MutableLiveData<>();
    private final MutableLiveData<Boolean> geoReq = new MutableLiveData<>();

    //Event Dates
    private final MutableLiveData<String> startingEventDate = new MutableLiveData<>();
    private final MutableLiveData<String> endingEventDate = new MutableLiveData<>();

    //Registration Period
    private final MutableLiveData<String> startingRegistrationPeriod = new MutableLiveData<>();
    private final MutableLiveData<String> endingRegistrationPeriod = new MutableLiveData<>();

    //Media
    private final MutableLiveData<Uri> imageUri = new MutableLiveData<>();
    private final MutableLiveData<String> qrCodeData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> privateEvent = new MutableLiveData<>(false);
    private final MutableLiveData<ArrayList<String>> invitedUserIds = new MutableLiveData<>(new ArrayList<>());
    // Identity

    // Event ID
    public LiveData<String> getEventId() { return eventId; }
    public void setEventId(String id) { eventId.setValue(id); }


    //Event Info

    // Name
    public LiveData<String> getName() { return name; }
    public void setName(String n) { name.setValue(n); }

    // Description
    public LiveData<String> getDescription() { return description; }
    public void setDescription(String d) { description.setValue(d); }

    // Location
    public LiveData<String> getLocation() { return location; }
    public void setLocation(String loc) { location.setValue(loc); }

    // Capacity
    public LiveData<Integer> getCapacity() { return capacity; }
    public void setCapacity(int i) { capacity.setValue(i); }

    // Geolocation Requirement
    public LiveData<Boolean> getGeoReq() { return geoReq; }
    public void setGeoReq(Boolean geo) { geoReq.setValue(geo); }


    // ─── Event Dates ───

    // Starting Event Date
    public LiveData<String> getStartingEventDate() { return startingEventDate; }
    public void setStartingEventDate(String date) { startingEventDate.setValue(date); }

    // Ending Event Date
    public LiveData<String> getEndingEventDate() { return endingEventDate; }
    public void setEndingEventDate(String date) { endingEventDate.setValue(date); }


    // ─── Registration Period ───

    // Starting Registration Period
    public LiveData<String> getStartingRegistrationPeriod() { return startingRegistrationPeriod; }
    public void setStartingRegistrationPeriod(String date) { startingRegistrationPeriod.setValue(date); }

    // Ending Registration Period
    public LiveData<String> getEndingRegistrationPeriod() { return endingRegistrationPeriod; }
    public void setEndingRegistrationPeriod(String date) { endingRegistrationPeriod.setValue(date); }


    // ─── Media ───

    // Image URI
    public LiveData<Uri> getImageUri() { return imageUri; }
    public void setImageUri(Uri uri) { imageUri.setValue(uri); }

    // QR Code Data
    public LiveData<String> getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qr) { qrCodeData.setValue(qr); }
    //Private event

    public LiveData<Boolean> getPrivateEvent() { return privateEvent; }
    public void setPrivateEvent(Boolean value) { privateEvent.setValue(value); }

    public LiveData<ArrayList<String>> getInvitedUserIds() { return invitedUserIds; }
    public void setInvitedUserIds(ArrayList<String> ids) { invitedUserIds.setValue(ids); }

}
