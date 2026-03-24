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

    public static void put(String eventId, Bitmap bitmap) { cache.put(eventId, bitmap); }
    public static Bitmap get(String eventId) { return cache.get(eventId); }
    public static boolean has(String eventId) { return cache.containsKey(eventId); }
    public static void remove(String eventId) { cache.remove(eventId); }

    public static void putBase64(String eventId, String base64) { base64Cache.put(eventId, base64); }
    public static String getBase64(String eventId) { return base64Cache.get(eventId); }
    public static boolean hasBase64(String eventId) { return base64Cache.containsKey(eventId); }
    public static void removeBase64(String eventId) { base64Cache.remove(eventId); }

    /**
     * Clears both the bitmap and Base64 caches.
     * Call on logout or when a full refresh is required.
     */
    public static void clear() {
        cache.clear();
        base64Cache.clear();
    }
}