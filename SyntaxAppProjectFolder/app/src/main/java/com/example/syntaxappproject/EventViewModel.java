package com.example.syntaxappproject;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

/**
 * Shared ViewModel for the event creation flow.
 * Allows the three event creation fragments to share event data across navigation steps.
 * Data persists across fragment transitions until the event is successfully created.
 */
public class EventViewModel extends ViewModel {

    private final MutableLiveData<String> eventId = new MutableLiveData<>();

    private final MutableLiveData<String> lotteryCriteria = new MutableLiveData<>("");
    private final MutableLiveData<String> name = new MutableLiveData<>();
    private final MutableLiveData<String> description = new MutableLiveData<>();
    private final MutableLiveData<String> location = new MutableLiveData<>();
    private final MutableLiveData<Integer> capacity = new MutableLiveData<>();
    private final MutableLiveData<Boolean> geoReq = new MutableLiveData<>();
    private final MutableLiveData<String> startingEventDate = new MutableLiveData<>();
    private final MutableLiveData<String> endingEventDate = new MutableLiveData<>();
    private final MutableLiveData<String> startingRegistrationPeriod = new MutableLiveData<>();
    private final MutableLiveData<String> endingRegistrationPeriod = new MutableLiveData<>();
    private final MutableLiveData<Uri> imageUri = new MutableLiveData<>();
    private final MutableLiveData<String> qrCodeData = new MutableLiveData<>();

    private final MutableLiveData<Boolean> privateEvent = new MutableLiveData<>(false);
    private final MutableLiveData<ArrayList<String>> invitedUserIds = new MutableLiveData<>(new ArrayList<>());

    public LiveData<String> getEventId() { return eventId; }
    public void setEventId(String id) { eventId.setValue(id); }

    public LiveData<String> getName() { return name; }
    public void setName(String n) { name.setValue(n); }

    public LiveData<String> getDescription() { return description; }
    public void setDescription(String d) { description.setValue(d); }

    public LiveData<String> getLocation() { return location; }
    public void setLocation(String loc) { location.setValue(loc); }

    public LiveData<String> getLotteryCriteria() { return lotteryCriteria; }
    public void setLotteryCriteria(String criteria) { lotteryCriteria.setValue(criteria); }

    public LiveData<Integer> getCapacity() { return capacity; }
    public void setCapacity(int i) { capacity.setValue(i); }

    public LiveData<Boolean> getGeoReq() { return geoReq; }
    public void setGeoReq(Boolean geo) { geoReq.setValue(geo); }

    public LiveData<String> getStartingEventDate() { return startingEventDate; }
    public void setStartingEventDate(String date) { startingEventDate.setValue(date); }

    public LiveData<String> getEndingEventDate() { return endingEventDate; }
    public void setEndingEventDate(String date) { endingEventDate.setValue(date); }

    public LiveData<String> getStartingRegistrationPeriod() { return startingRegistrationPeriod; }
    public void setStartingRegistrationPeriod(String date) { startingRegistrationPeriod.setValue(date); }

    public LiveData<String> getEndingRegistrationPeriod() { return endingRegistrationPeriod; }
    public void setEndingRegistrationPeriod(String date) { endingRegistrationPeriod.setValue(date); }

    public LiveData<Uri> getImageUri() { return imageUri; }
    public void setImageUri(Uri uri) { imageUri.setValue(uri); }

    public LiveData<String> getQrCodeData() { return qrCodeData; }
    public void setQrCodeData(String qr) { qrCodeData.setValue(qr); }

    public LiveData<Boolean> getPrivateEvent() { return privateEvent; }
    public void setPrivateEvent(Boolean value) { privateEvent.setValue(value); }

    public LiveData<ArrayList<String>> getInvitedUserIds() { return invitedUserIds; }
    public void setInvitedUserIds(ArrayList<String> ids) { invitedUserIds.setValue(ids); }
}