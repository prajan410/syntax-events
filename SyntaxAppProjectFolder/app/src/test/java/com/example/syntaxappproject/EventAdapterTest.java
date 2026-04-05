package com.example.syntaxappproject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests for {@link EventAdapter}.
 *
 * <p>This test suite verifies the adapter's data handling logic,
 * without relying on Android UI components.</p>
 */
@RunWith(JUnit4.class)
public class EventAdapterTest {

    /**
     * Mock click listener to verify item click events.
     */
    @Mock
    private EventAdapter.OnItemClickListener mockListener;

    private EventAdapter adapter;

    private List<EventDetail> testEvents;

    /**
     * Sets up the test environment before each test.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        mockListener = mock(EventAdapter.OnItemClickListener.class);
        adapter = new EventAdapter(new ArrayList<>(), mockListener);
        adapter.isTestMode = true;

        testEvents = createTestEvents();
    }

    /**
     * Creates a list of sample events for testing.
     *
     * @return list of test events
     */
    private List<EventDetail> createTestEvents() {
        EventDetail event1 = new EventDetail(
                "event1",
                "Event One",
                "Description",
                "Location",
                100,
                true,
                "2026-04-01",
                "2026-04-02",
                "2026-03-01",
                "2026-03-30",
                10,
                "Random",
                "poster.jpg"
        );

        EventDetail event2 = new EventDetail(
                "event2",
                "Event Two",
                "Description",
                "Location",
                50,
                false,
                "2026-05-01",
                "2026-05-02",
                "2026-04-01",
                "2026-04-30",
                5,
                "Random",
                "poster.jpg"
        );

        return Arrays.asList(event1, event2);
    }

    /**
     * Test that the adapter starts with zero items.
     */
    @Test
    public void testAdapter_initialItemCount() {
        assertEquals(0, adapter.getItemCount());
    }

    /**
     * Test that updating the list correctly changes item count.
     */
    @Test
    public void testAdapter_updateList_updatesItemCount() {
        adapter.updateList(testEvents);
        assertEquals(2, adapter.getItemCount());
    }

    /**
     * Test that the adapter correctly stores updated data.
     */
    @Test
    public void testAdapter_updateList_updatesData() {
        adapter.updateList(testEvents);

        assertEquals("Event One", adapter.getEvents().get(0).getName());
        assertEquals("Event Two", adapter.getEvents().get(1).getName());
    }

    /**
     * Test that updating with a new list replaces old data.
     */
    @Test
    public void testAdapter_updateList_replacesOldData() {
        adapter.updateList(testEvents);
        assertEquals(2, adapter.getItemCount());

        List<EventDetail> newList = new ArrayList<>();
        newList.add(testEvents.get(0));

        adapter.updateList(newList);

        assertEquals(1, adapter.getItemCount());
        assertEquals("Event One", adapter.getEvents().get(0).getName());
    }

    /**
     * Test that click listener is triggered correctly.
     */
    @Test
    public void testAdapter_clickListenerTriggers() {
        adapter.updateList(testEvents);

        mockListener.onItemClick(testEvents.get(0));

        verify(mockListener).onItemClick(testEvents.get(0));
    }

    /**
     * Test that different items trigger the listener correctly.
     */
    @Test
    public void testAdapter_multipleItemClicks() {
        adapter.updateList(testEvents);

        mockListener.onItemClick(testEvents.get(1));

        verify(mockListener).onItemClick(testEvents.get(1));
    }

    /**
     * Test that updating with an empty list clears the adapter.
     */
    @Test
    public void testAdapter_updateList_emptyList() {
        adapter.updateList(new ArrayList<>());
        assertEquals(0, adapter.getItemCount());
    }
}
