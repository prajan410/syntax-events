package com.example.syntaxappproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Unit tests for the {@link EventFilterViewModel} model class.
 * <p>
 * Test the setting and getting filter value
 * </p>
 */
public class EventFilterViewModelTest {
    private EventFilterViewModel filter;
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Before
    public void setUp(){
        filter = new EventFilterViewModel();
    }

    /**
     * Test set value of filters
     */
    @Test
    public void testSetFilters(){
        filter.setFilters("2026-03-29", "2026-03-30", 10L);
        assertEquals("2026-03-29",filter.getStartValue());
        assertEquals("2026-03-30", filter.getEndValue());
        assertEquals(10L, filter.getCapacityValue());
    }

    /**
     * Test get start date
     */
    @Test
    public void testGetStartDate(){
        filter.setFilters("2026-03-29", "2026-03-30", 10L);
        assertEquals("2026-03-29",filter.getStartValue());
    }
    /**
     * Test get end date
     */
    @Test
    public void testGetEndDate(){
        filter.setFilters("2026-03-29", "2026-03-30", 10L);
        assertEquals("2026-03-30",filter.getEndValue());
    }
    /**
     * Test get capacity
     */
    @Test
    public void testGetCapacity(){
        filter.setFilters("2026-03-29", "2026-03-30", 10L);
        assertEquals(10L,filter.getCapacityValue());
    }
    /**
     * Test clear filter
     */
    @Test
    public void testClearFilter(){
        filter.setFilters("2026-03-29", "2026-03-30", 10L);
        filter.clearFilters();
        assertNull(filter.getStartValue());
        assertNull(filter.getEndValue());
        assertEquals(-1L,filter.getCapacityValue());
    }
    /**
     * Test has filter return ture if has value in filter
     */
    @Test
    public void testHasFilter(){
        filter.setFilters("2026-03-29", "2026-03-30", 10L);
        assertTrue(filter.hasFilter());
    }
}
