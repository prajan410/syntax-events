package com.example.syntaxappproject;

import android.graphics.Bitmap;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory cache for event poster bitmaps and their Base64 strings,
 * keyed by event ID.
 *
 * Populated when images are loaded from Firestore and updated when new
 * events are created. Allows fragments to display posters instantly
 * without re-decoding or re-fetching on every navigation.
 */
public class ImageCacheManager {

    private static final Map<String, Bitmap> cache = new HashMap<>();
    private static final Map<String, String> base64Cache = new HashMap<>();

    /**
     * Stores a bitmap in the cache.
     *
     * @param eventId the event ID to use as the cache key
     * @param bitmap  the bitmap to cache
     */
    public static void put(String eventId, Bitmap bitmap) { cache.put(eventId, bitmap); }

    /**
     * Retrieves a bitmap from the cache.
     *
     * @param eventId the event ID to look up
     * @return the cached bitmap, or null if not found
     */
    public static Bitmap get(String eventId) { return cache.get(eventId); }

    /**
     * Checks if a bitmap exists in the cache for the given event ID.
     *
     * @param eventId the event ID to check
     * @return true if the bitmap is cached, false otherwise
     */
    public static boolean has(String eventId) { return cache.containsKey(eventId); }

    /**
     * Removes a bitmap from the cache.
     *
     * @param eventId the event ID to remove
     */
    public static void remove(String eventId) { cache.remove(eventId); }

    /**
     * Stores a Base64 string in the cache.
     *
     * @param eventId the event ID to use as the cache key
     * @param base64  the Base64 string to cache
     */
    public static void putBase64(String eventId, String base64) { base64Cache.put(eventId, base64); }

    /**
     * Retrieves a Base64 string from the cache.
     *
     * @param eventId the event ID to look up
     * @return the cached Base64 string, or null if not found
     */
    public static String getBase64(String eventId) { return base64Cache.get(eventId); }

    /**
     * Checks if a Base64 string exists in the cache for the given event ID.
     *
     * @param eventId the event ID to check
     * @return true if the Base64 string is cached, false otherwise
     */
    public static boolean hasBase64(String eventId) { return base64Cache.containsKey(eventId); }

    /**
     * Removes a Base64 string from the cache.
     *
     * @param eventId the event ID to remove
     */
    public static void removeBase64(String eventId) { base64Cache.remove(eventId); }

    /**
     * Returns all cached bitmaps.
     *
     * @return a map of all event IDs to their cached bitmaps
     */
    public static Map<String, Bitmap> getAll() { return cache; }

    /**
     * Clears both the bitmap and Base64 caches.
     * Call on logout or when a full refresh is required.
     */
    public static void clear() {
        cache.clear();
        base64Cache.clear();
    }
}