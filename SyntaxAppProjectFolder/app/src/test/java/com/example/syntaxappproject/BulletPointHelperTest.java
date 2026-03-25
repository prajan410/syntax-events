package com.example.syntaxappproject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link BulletPointHelper}.
 * Tests bullet point formatting and parsing utility methods.
 *
 * <p>These tests verify the behavior of converting between bullet-formatted text
 * and plain text, as well as parsing bullet points into lists.</p>
 */
@RunWith(JUnit4.class)
public class BulletPointHelperTest {

    @Test
    public void getPlainText_removesBulletSymbols() {
        String bulletText = "• Item 1\n• Item 2\n• Item 3";
        String expected = "Item 1\nItem 2\nItem 3";

        String result = BulletPointHelper.getPlainText(bulletText);

        assertEquals(expected, result);
    }

    @Test
    public void getPlainText_handlesEmptyInput() {
        assertEquals("", BulletPointHelper.getPlainText(null));
        assertEquals("", BulletPointHelper.getPlainText(""));
    }

    @Test
    public void getPlainText_trimsWhitespace() {
        String bulletText = "•   Item 1  \n•  Item 2  \n•  Item 3  ";
        String expected = "Item 1\nItem 2\nItem 3";

        String result = BulletPointHelper.getPlainText(bulletText);

        assertEquals(expected, result);
    }

    @Test
    public void getPlainText_withSingleBullet() {
        String bulletText = "• Single item";
        String expected = "Single item";

        String result = BulletPointHelper.getPlainText(bulletText);

        assertEquals(expected, result);
    }

    @Test
    public void formatWithBullets_addsBulletSymbols() {
        String plainText = "Item 1\nItem 2\nItem 3";
        String expected = "• Item 1\n• Item 2\n• Item 3";

        String result = BulletPointHelper.formatWithBullets(plainText);

        assertEquals(expected, result);
    }

    @Test
    public void formatWithBullets_handlesEmptyInput() {
        assertEquals("", BulletPointHelper.formatWithBullets(null));
        assertEquals("", BulletPointHelper.formatWithBullets(""));
    }

    @Test
    public void formatWithBullets_ignoresEmptyLines() {
        String plainText = "Item 1\n\nItem 2\n\nItem 3";
        String expected = "• Item 1\n• Item 2\n• Item 3";

        String result = BulletPointHelper.formatWithBullets(plainText);

        assertEquals(expected, result);
    }

    @Test
    public void formatWithBullets_withSingleItem() {
        String plainText = "Single item";
        String expected = "• Single item";

        String result = BulletPointHelper.formatWithBullets(plainText);

        assertEquals(expected, result);
    }

    @Test
    public void parseBulletPoints_returnsListOfPoints() {
        String bulletText = "• First criteria\n• Second criteria\n• Third criteria";
        List<String> expected = Arrays.asList("First criteria", "Second criteria", "Third criteria");

        List<String> result = BulletPointHelper.parseBulletPoints(bulletText);

        assertEquals(expected, result);
    }

    @Test
    public void parseBulletPoints_handlesEmptyInput() {
        assertTrue(BulletPointHelper.parseBulletPoints(null).isEmpty());
        assertTrue(BulletPointHelper.parseBulletPoints("").isEmpty());
    }

    @Test
    public void parseBulletPoints_withWhitespace() {
        String bulletText = "•   First\n•  Second with spaces  \n• Third\n";
        List<String> expected = Arrays.asList("First", "Second with spaces", "Third");

        List<String> result = BulletPointHelper.parseBulletPoints(bulletText);

        assertEquals(expected, result);
    }

    @Test
    public void parseBulletPoints_withoutBulletSymbols() {
        String plainText = "First\nSecond\nThird";
        List<String> expected = Arrays.asList("First", "Second", "Third");

        List<String> result = BulletPointHelper.parseBulletPoints(plainText);

        assertEquals(expected, result);
    }

    @Test
    public void roundTripConversion_preservesContent() {
        String originalPlain = "Criteria 1\nCriteria 2\nCriteria 3";

        String bulletFormatted = BulletPointHelper.formatWithBullets(originalPlain);
        String backToPlain = BulletPointHelper.getPlainText(bulletFormatted);

        assertEquals(originalPlain, backToPlain);
    }

    @Test
    public void getPlainText_preservesOrder() {
        String bulletText = "• Third\n• Second\n• First";
        String expected = "Third\nSecond\nFirst";

        String result = BulletPointHelper.getPlainText(bulletText);

        assertEquals(expected, result);
    }

    @Test
    public void formatWithBullets_preservesOrder() {
        String plainText = "Third\nSecond\nFirst";
        String expected = "• Third\n• Second\n• First";

        String result = BulletPointHelper.formatWithBullets(plainText);

        assertEquals(expected, result);
    }

    @Test
    public void parseBulletPoints_preservesOrder() {
        String bulletText = "• Third\n• Second\n• First";
        List<String> expected = Arrays.asList("Third", "Second", "First");

        List<String> result = BulletPointHelper.parseBulletPoints(bulletText);

        assertEquals(expected, result);
    }

    @Test
    public void getPlainText_withTrailingNewline() {
        String bulletText = "• Item 1\n• Item 2\n";
        String expected = "Item 1\nItem 2";

        String result = BulletPointHelper.getPlainText(bulletText);

        assertEquals(expected, result);
    }

    @Test
    public void formatWithBullets_withTrailingNewline() {
        String plainText = "Item 1\nItem 2\n";
        String expected = "• Item 1\n• Item 2";

        String result = BulletPointHelper.formatWithBullets(plainText);

        assertEquals(expected, result);
    }
}