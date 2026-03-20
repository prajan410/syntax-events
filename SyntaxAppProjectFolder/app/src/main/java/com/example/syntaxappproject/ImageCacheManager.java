package com.example.syntaxappproject;

import android.graphics.Bitmap;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory cache for event poster bitmaps, keyed by event ID.
 *
 * <p>Populated at splash screen load and updated when new events are created.
 * Allows fragments to display posters instantly without re-fetching from
 * Firebase Realtime Database on every navigation.</p>
 */
public class ImageCacheManager {

    /** Backing store mapping event IDs to their decoded poster bitmaps. */
    private static final Map<String, Bitmap> cache = new HashMap<>();

    /**
     * Inserts or replaces a bitmap in the cache.
     *
     * @param eventId the event ID to use as the cache key
     * @param bitmap  the decoded poster bitmap to store
     */
    public static void put(String eventId, Bitmap bitmap) { cache.put(eventId, bitmap); }

    /**
     * Retrieves a cached bitmap by event ID.
     *
     * @param eventId the event ID to look up
     * @return the cached {@link Bitmap}, or {@code null} if not present
     */
    public static Bitmap get(String eventId) { return cache.get(eventId); }

    /**
     * Returns whether a bitmap is currently cached for the given event ID.
     *
     * @param eventId the event ID to check
     * @return {@code true} if a bitmap exists in the cache for this ID
     */
    public static boolean has(String eventId) { return cache.containsKey(eventId); }

    /**
     * Removes the cached bitmap for the given event ID, if present.
     * Called when an event is deleted to free memory and prevent stale entries.
     *
     * @param eventId the event ID whose cached bitmap should be removed
     */
    public static void remove(String eventId) { cache.remove(eventId); }

    /**
     * Clears all entries from the cache.
     * Useful on logout or when a full refresh is required.
     */
    public static void clear() { cache.clear(); }
}
