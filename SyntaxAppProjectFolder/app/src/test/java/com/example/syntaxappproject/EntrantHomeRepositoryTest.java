package com.example.syntaxappproject;

import static org.junit.Assert.assertEquals;

import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Unit Test for the {@link EntrantHomeRepository}.
 */
public class EntrantHomeRepositoryTest {

    /**
     * In-memory fake subclass of {@link EntrantHomeRepository} that simulates
     * the Firestore event collection using a {@link List<EventDetail>}.
     */
    static class FakeEntrantHomeRepository extends EntrantHomeRepository {

        private EventDetail event1 = new EventDetail(
                        "event1",
                        "Test Event1",
                        "Test Description",
                        "Test Location",
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
        private EventDetail event2 = new EventDetail(
                "event2",
                "Test Event2",
                "Test Description",
                "Test Location",
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
        private final List<EventDetail> database = new ArrayList<EventDetail>();

        public void setup() {
            database.clear();
            database.add(event1);
            database.add(event2);
        }
        /**
         * Constructs the fake repository using the test-mode constructor
         * to bypass Firebase initialization.
         */
        public FakeEntrantHomeRepository(){super(true);}

        /**
         * Override getEvents, get events from a List of events
         * @param callback the {@link EntrantHomeRepository.EventCallback} invoked with the result,
         */
        @Override
        public void getEvents(EventCallback callback) {

                List<EventDetail> events = new ArrayList<>();

                for (EventDetail doc : database) {

                    EventDetail event = doc;

                    if (event != null) {
                        event.setEventId(doc.getEventId());
                        events.add(event);
                    }
                }

                callback.onSuccess(events);
        }
    }

    private FakeEntrantHomeRepository repo;

    /**
     * Initializes a fake repository and set a fake database.
     */
    @Before
    public void setUP(){
        repo = new FakeEntrantHomeRepository();
        repo.setup();
    }

    /**
     * Test of getEvents.
     */
    @Test
    public void testGetEvents(){
        repo.getEvents(events -> {
            assertEquals("event1", events.get(0).getEventId());
            assertEquals("event2", events.get(1).getEventId());
        });
    }
}
