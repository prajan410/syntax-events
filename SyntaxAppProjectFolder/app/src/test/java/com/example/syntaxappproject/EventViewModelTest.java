package com.example.syntaxappproject;

import android.net.Uri;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventViewModelTest {

    /**
     * Swaps the background executor used by Architecture Components
     * with one that executes each task synchronously.
     * Required for testing LiveData without an Android device.
     */
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private EventViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new EventViewModel();
    }


    @Test
    public void testSetAndGetName() {
        viewModel.setName("Hackathon");
        assertEquals("Hackathon", viewModel.getName().getValue());
    }

    @Test
    public void testSetAndGetDescription() {
        viewModel.setDescription("A 24-hour coding event");
        assertEquals("A 24-hour coding event", viewModel.getDescription().getValue());
    }

    @Test
    public void testSetAndGetLocation() {
        viewModel.setLocation("Edmonton");
        assertEquals("Edmonton", viewModel.getLocation().getValue());
    }

    @Test
    public void testSetAndGetCapacity() {
        viewModel.setCapacity(100);
        assertEquals(Integer.valueOf(100), viewModel.getCapacity().getValue());
    }

    @Test
    public void testCapacityZero() {
        viewModel.setCapacity(0);
        assertEquals(Integer.valueOf(0), viewModel.getCapacity().getValue());
    }

    @Test
    public void testSetAndGetGeoReq() {
        viewModel.setGeoReq(true);
        assertEquals(Boolean.TRUE, viewModel.getGeoReq().getValue());
    }
    @Test
    public void testGeoReqFalse() {
        viewModel.setGeoReq(false);
        assertEquals(Boolean.FALSE, viewModel.getGeoReq().getValue());
    }
    @Test
    public void testSetAndGetStartingEventDate() {
        viewModel.setStartingEventDate("2025-06-01");
        assertEquals("2025-06-01", viewModel.getStartingEventDate().getValue());
    }
    @Test
    public void testSetAndGetEndingEventDate() {
        viewModel.setEndingEventDate("2025-06-02");
        assertEquals("2025-06-02", viewModel.getEndingEventDate().getValue());
    }
    @Test
    public void testSetAndGetStartingRegistrationPeriod() {
        viewModel.setStartingRegistrationPeriod("2025-05-01");
        assertEquals("2025-05-01", viewModel.getStartingRegistrationPeriod().getValue());
    }
    @Test
    public void testSetAndGetEndingRegistrationPeriod() {
        viewModel.setEndingRegistrationPeriod("2025-05-31");
        assertEquals("2025-05-31", viewModel.getEndingRegistrationPeriod().getValue());
    }
    @Test
    public void testSetAndGetQrCodeData() {
        viewModel.setQrCodeData("syntaxappproject://event/abc123");
        assertEquals("syntaxappproject://event/abc123", viewModel.getQrCodeData().getValue());
    }
    @Test
    public void testInitialValuesAreNull() {
        EventViewModel fresh = new EventViewModel();
        assertNull(fresh.getName().getValue());
        assertNull(fresh.getDescription().getValue());
        assertNull(fresh.getLocation().getValue());
        assertNull(fresh.getCapacity().getValue());
        assertNull(fresh.getGeoReq().getValue());
        assertNull(fresh.getStartingEventDate().getValue());
        assertNull(fresh.getEndingEventDate().getValue());
        assertNull(fresh.getStartingRegistrationPeriod().getValue());
        assertNull(fresh.getEndingRegistrationPeriod().getValue());
        assertNull(fresh.getImageUri().getValue());
        assertNull(fresh.getQrCodeData().getValue());
    }
    @Test
    public void testOverwritingValueUpdatesLiveData() {
        viewModel.setName("First");
        viewModel.setName("Second");
        assertEquals("Second", viewModel.getName().getValue());
    }
}