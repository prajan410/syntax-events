package com.example.syntaxappproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;


@RunWith(AndroidJUnit4.class)
public class AdminEventAdapterTest {

    private Context context;

    /**
     * Sets up the test context before each test runs.
     */
    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /**
     * Creates a themed parent layout for inflating adapter item views.
     *
     * @return a {@link FrameLayout} using a Material Components theme
     */
    private FrameLayout createThemedParent() {
        Context themedContext = new ContextThemeWrapper(
                context,
                com.google.android.material.R.style.Theme_MaterialComponents_Light_NoActionBar
        );
        return new FrameLayout(themedContext);
    }

    /**
     * Creates a sample {@link EventDetail} object with preset test data.
     *
     * @param name the name of the event
     * @param location the location of the event
     * @param organizerUid the organizer UID for the event
     * @return a populated {@link EventDetail} instance
     */
    private EventDetail makeEvent(String name, String location, String organizerUid) {
        EventDetail event = new EventDetail();
        event.setName(name);
        event.setDescription("Test description");
        event.setLocation(location);
        event.setOrganizerUid(organizerUid);
        event.setCapacity(50);
        event.setGeoReq(false);
        event.setStartingEventDate("2026-04-01");
        event.setEndingEventDate("2026-04-02");
        event.setStartingRegistrationPeriod("2026-03-20");
        event.setEndingRegistrationPeriod("2026-03-30");
        event.setWaitlistCount(3);
        event.setLotteryCriteria("Random draw");
        event.setPoster("poster-data");
        return event;
    }

    /**
     * Verifies that {@code getItemCount()} returns the correct number of events.
     */
    @Test
    public void testGetItemCount_returnsCorrectSize() {
        ArrayList<EventDetail> events = new ArrayList<>();
        events.add(makeEvent("Event A", "Edmonton", ""));
        events.add(makeEvent("Event B", "Calgary", ""));

        ArrayList<String> ids = new ArrayList<>(Arrays.asList("id1", "id2"));

        AdminEventAdapter adapter = new AdminEventAdapter(events, ids);

        assertEquals(2, adapter.getItemCount());
    }

    /**
     * Verifies that binding a view holder sets the expected event title,
     * location, and default organizer name in the item view.
     */
    @Test
    public void testOnBindViewHolder_setsBasicTexts() {
        ArrayList<EventDetail> events = new ArrayList<>();
        events.add(makeEvent("Swimming Lessons", "Edmonton", ""));

        ArrayList<String> ids = new ArrayList<>(Arrays.asList("event-1"));

        AdminEventAdapter adapter = new AdminEventAdapter(events, ids);

        FrameLayout parent = createThemedParent();
        AdminEventAdapter.EventViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        assertEquals("Swimming Lessons",
                ((TextView) holder.itemView.findViewById(R.id.tv_event_title)).getText().toString());
        assertEquals("Edmonton",
                ((TextView) holder.itemView.findViewById(R.id.tv_event_location)).getText().toString());
        assertEquals("Unknown",
                ((TextView) holder.itemView.findViewById(R.id.tv_organizer_name)).getText().toString());
    }

    /**
     * Verifies that {@code updateData()} replaces the adapter's event list
     * and that the updated data is reflected when binding a view holder.
     */
    @Test
    public void testUpdateData_replacesAdapterData() {
        ArrayList<EventDetail> oldEvents = new ArrayList<>();
        oldEvents.add(makeEvent("Old Event", "Old Location", ""));
        ArrayList<String> oldIds = new ArrayList<>(Arrays.asList("old-id"));

        AdminEventAdapter adapter = new AdminEventAdapter(oldEvents, oldIds);

        ArrayList<EventDetail> newEvents = new ArrayList<>();
        newEvents.add(makeEvent("New Event", "New Location", ""));
        newEvents.add(makeEvent("Another Event", "Toronto", ""));
        ArrayList<String> newIds = new ArrayList<>(Arrays.asList("new-id-1", "new-id-2"));

        adapter.updateData(newEvents, newIds);

        assertEquals(2, adapter.getItemCount());

        FrameLayout parent = createThemedParent();
        AdminEventAdapter.EventViewHolder holder = adapter.onCreateViewHolder(parent, 0);
        adapter.onBindViewHolder(holder, 0);

        assertEquals("New Event",
                ((TextView) holder.itemView.findViewById(R.id.tv_event_title)).getText().toString());
        assertEquals("New Location",
                ((TextView) holder.itemView.findViewById(R.id.tv_event_location)).getText().toString());
    }

    /**
     * Verifies that clicking the details button triggers navigation to
     * the admin event details screen with the correct bundle contents.
     */
    @Test
    public void testDetailsButton_navigatesWithBundle() {
        ArrayList<EventDetail> events = new ArrayList<>();
        events.add(makeEvent("Hackathon", "Edmonton", ""));

        ArrayList<String> ids = new ArrayList<>(Arrays.asList("event-123"));

        AdminEventAdapter adapter = new AdminEventAdapter(events, ids);

        FrameLayout parent = createThemedParent();
        AdminEventAdapter.EventViewHolder holder = adapter.onCreateViewHolder(parent, 0);

        NavController navController = mock(NavController.class);
        Navigation.setViewNavController(holder.itemView, navController);

        adapter.onBindViewHolder(holder, 0);

        holder.itemView.findViewById(R.id.btn_event_details).performClick();

        ArgumentCaptor<Bundle> bundleCaptor = ArgumentCaptor.forClass(Bundle.class);
        verify(navController).navigate(eq(R.id.adminEventDetails), bundleCaptor.capture());

        Bundle bundle = bundleCaptor.getValue();
        assertNotNull(bundle);
        assertEquals("event-123", bundle.getString("eventId"));
        assertEquals("Hackathon", bundle.getString("name"));
        assertEquals("Edmonton", bundle.getString("location"));
        assertEquals("Test description", bundle.getString("description"));
        assertEquals(50L, bundle.getLong("capacity"));
    }
}
