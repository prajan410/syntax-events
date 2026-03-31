package com.example.syntaxappproject;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for the filter in home page.
 * Allows to keep the filter setting cross fragments.
 * Provide the function to set filter.
 */
public class EventFilterViewModel extends ViewModel {
    private final MutableLiveData<String> startDate = new MutableLiveData<>(null);
    private final MutableLiveData<String> endDate = new MutableLiveData<>(null);
    private final MutableLiveData<Long> capacity = new MutableLiveData<>(-1L);
    /**
     * Returns the live data of start date
     *
     * @return the start date
     */
    public LiveData<String> getStartDate() {
        return startDate;
    }
    /**
     * Returns the live data of end date
     *
     * @return the end date
     */
    public LiveData<String> getEndDate() {
        return endDate;
    }
    /**
     * Returns the live data of capacity
     *
     * @return the capacity
     */
    public LiveData<Long> getCapacity() {
        return capacity;
    }

    /**
     * Sets the filters
     *
     * @param start the start day to filter
     * @param end the end day to filter
     * @param cap the capacity to filter
     */
    public void setFilters(String start, String end, long cap) {
        startDate.setValue(start);
        endDate.setValue(end);
        capacity.setValue(cap);
    }

    /**
     * Clear the filter, set all data to default.
     */
    public void clearFilters() {
        startDate.setValue(null);
        endDate.setValue(null);
        capacity.setValue(-1L);
    }
    /**
     * Returns the value of start date
     *
     * @return the value of start date
     */
    public String getStartValue() {
        return startDate.getValue();
    }
    /**
     * Returns the value of end date
     *
     * @return the value of end date
     */
    public String getEndValue() {
        return endDate.getValue();
    }
    /**
     * Returns the value of capacity
     *
     * @return the value of capacity
     */
    public long getCapacityValue() {
        Long val = capacity.getValue();
        return val != null ? val : -1L;
    }

    /**
     * Define if the filter is on
     * @return true if one of the filters contain values
     */
    public boolean hasFilter() {
        return (getStartValue() != null && !getStartValue().isEmpty()) ||
                (getEndValue() != null && !getEndValue().isEmpty()) ||
                getCapacityValue() != -1L;
    }

}
