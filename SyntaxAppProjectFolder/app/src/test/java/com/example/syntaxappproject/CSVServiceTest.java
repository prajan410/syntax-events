package com.example.syntaxappproject;

import static org.junit.Assert.assertEquals;

import android.util.Pair;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit test for {@link CSVService}.
 * Verifies that the CSV formatting logic correctly converts a list of winners into a CSV string.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {34})
public class CSVServiceTest {

    /**
     * Tests that createCSV correctly formats a list of name-email pairs.
     */
    @Test
    public void testCreateCSVFormatting() {
        List<Pair<String, String>> fakeWinners = new ArrayList<>();
        fakeWinners.add(new Pair<>("John Doe", "john@example.com"));
        fakeWinners.add(new Pair<>("Jane Smith", "jane@example.com"));
        fakeWinners.add(new Pair<>("Bob Brown", "bob@example.com"));

        StringBuilder result = CSVService.createCSV(fakeWinners);

        String expected = "John Doe,john@example.com\n" +
                          "Jane Smith,jane@example.com\n" +
                          "Bob Brown,bob@example.com";

        assertEquals("The CSV format should match the expected string", expected, result.toString());
    }

    /**
     * Tests createCSV with an empty list.
     */
    @Test
    public void testCreateCSVWithEmptyList() {
        List<Pair<String, String>> emptyList = new ArrayList<>();
        StringBuilder result = CSVService.createCSV(emptyList);
        assertEquals("An empty list should result in an empty string", "", result.toString());
    }

    /**
     * Tests createCSV with a single element to ensure no trailing newline.
     */
    @Test
    public void testCreateCSVWithSingleElement() {
        List<Pair<String, String>> singleList = new ArrayList<>();
        singleList.add(new Pair<>("Single User", "single@example.com"));

        StringBuilder result = CSVService.createCSV(singleList);
        assertEquals("A single element should not have a trailing newline", "Single User,single@example.com", result.toString());
    }
}
