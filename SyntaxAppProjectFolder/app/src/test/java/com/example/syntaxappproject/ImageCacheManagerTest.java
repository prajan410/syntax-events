package com.example.syntaxappproject;

import android.graphics.Bitmap;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link ImageCacheManager}.
 *
 * Verifies correct storage, retrieval, existence checks, removal,
 * and clearing of both the bitmap and Base64 caches.
 * {@link Bitmap} instances are mocked since the Android graphics API
 * is unavailable in the local JVM test environment.
 */
public class ImageCacheManagerTest {

    /**
     * Clears both caches before each test to ensure a clean state.
     */
    @Before
    public void setUp() {
        ImageCacheManager.clear();
    }

    /**
     * Verifies that a bitmap stored with {@code put} can be retrieved with {@code get}.
     */
    @Test
    public void testPutAndGetBitmap() {
        Bitmap bitmap = mock(Bitmap.class);
        ImageCacheManager.put("event1", bitmap);
        assertEquals(bitmap, ImageCacheManager.get("event1"));
    }

    /**
     * Verifies that {@code has} returns true after a bitmap is stored
     * and false before any entry exists.
     */
    @Test
    public void testHasBitmap() {
        assertFalse(ImageCacheManager.has("event1"));
        Bitmap bitmap = mock(Bitmap.class);
        ImageCacheManager.put("event1", bitmap);
        assertTrue(ImageCacheManager.has("event1"));
    }

    /**
     * Verifies that {@code remove} deletes the bitmap entry so subsequent
     * {@code has} and {@code get} calls return false and null respectively.
     */
    @Test
    public void testRemoveBitmap() {
        Bitmap bitmap = mock(Bitmap.class);
        ImageCacheManager.put("event1", bitmap);
        ImageCacheManager.remove("event1");
        assertFalse(ImageCacheManager.has("event1"));
        assertNull(ImageCacheManager.get("event1"));
    }

    /**
     * Verifies that a Base64 string stored with {@code putBase64} can be
     * retrieved with {@code getBase64}.
     */
    @Test
    public void testPutAndGetBase64() {
        ImageCacheManager.putBase64("event1", "abc123==");
        assertEquals("abc123==", ImageCacheManager.getBase64("event1"));
    }

    /**
     * Verifies that {@code hasBase64} returns true after a Base64 string is stored
     * and false before any entry exists.
     */
    @Test
    public void testHasBase64() {
        assertFalse(ImageCacheManager.hasBase64("event1"));
        ImageCacheManager.putBase64("event1", "abc123==");
        assertTrue(ImageCacheManager.hasBase64("event1"));
    }

    /**
     * Verifies that {@code removeBase64} deletes the Base64 entry so subsequent
     * {@code hasBase64} and {@code getBase64} calls return false and null respectively.
     */
    @Test
    public void testRemoveBase64() {
        ImageCacheManager.putBase64("event1", "abc123==");
        ImageCacheManager.removeBase64("event1");
        assertFalse(ImageCacheManager.hasBase64("event1"));
        assertNull(ImageCacheManager.getBase64("event1"));
    }

    /**
     * Verifies that {@code clear} removes all entries from both the bitmap
     * and Base64 caches simultaneously.
     */
    @Test
    public void testClearWipesBothCaches() {
        Bitmap bitmap = mock(Bitmap.class);
        ImageCacheManager.put("event1", bitmap);
        ImageCacheManager.putBase64("event1", "abc123==");
        ImageCacheManager.put("event2", bitmap);
        ImageCacheManager.putBase64("event2", "xyz456==");

        ImageCacheManager.clear();

        assertFalse(ImageCacheManager.has("event1"));
        assertFalse(ImageCacheManager.has("event2"));
        assertFalse(ImageCacheManager.hasBase64("event1"));
        assertFalse(ImageCacheManager.hasBase64("event2"));
    }

    /**
     * Verifies that storing a new bitmap under an existing key overwrites
     * the previous entry.
     */
    @Test
    public void testPutOverwritesExistingBitmap() {
        Bitmap bitmap1 = mock(Bitmap.class);
        Bitmap bitmap2 = mock(Bitmap.class);
        ImageCacheManager.put("event1", bitmap1);
        ImageCacheManager.put("event1", bitmap2);
        assertEquals(bitmap2, ImageCacheManager.get("event1"));
    }

    /**
     * Verifies that storing a new Base64 string under an existing key overwrites
     * the previous entry.
     */
    @Test
    public void testPutBase64OverwritesExisting() {
        ImageCacheManager.putBase64("event1", "first==");
        ImageCacheManager.putBase64("event1", "second==");
        assertEquals("second==", ImageCacheManager.getBase64("event1"));
    }

    /**
     * Verifies that bitmap and Base64 caches are independent — removing
     * a bitmap entry does not affect the corresponding Base64 entry.
     */
    @Test
    public void testBitmapAndBase64CachesAreIndependent() {
        Bitmap bitmap = mock(Bitmap.class);
        ImageCacheManager.put("event1", bitmap);
        ImageCacheManager.putBase64("event1", "abc123==");

        ImageCacheManager.remove("event1");

        assertFalse(ImageCacheManager.has("event1"));
        assertTrue(ImageCacheManager.hasBase64("event1"));
    }
}