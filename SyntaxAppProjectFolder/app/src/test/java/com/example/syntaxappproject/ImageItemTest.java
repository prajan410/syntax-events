package com.example.syntaxappproject;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ImageItemTest {

    /**
     * Verifies that the no-argument constructor creates a non-null ImageItem object.
     */
    @Test
    public void testEmptyConstructorCreatesObject() {
        ImageItem item = new ImageItem();
        assertNotNull(item);
    }

    /**
     * Verifies that the parameterized constructor correctly assigns
     * the image URL and uploader name fields.
     */
    @Test
    public void testConstructorSetsFieldsCorrectly() {
        ImageItem item = new ImageItem(
                "https://example.com/poster.jpg",
                "Yixing Li"
        );
        assertEquals("https://example.com/poster.jpg", item.imageUrl);
        assertEquals("Yixing Li", item.uploadedBy);
    }
}