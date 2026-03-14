package Sync.Intent.Utils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PathCache {

    private static final Map<String, String> pathMap = new ConcurrentHashMap<>();

    /**
     * Stores a full path and returns a short, unique ID for it.
     */
    public static String put(String fullPath) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        pathMap.put(id, fullPath);
        return id;
    }

    /**
     * Retrieves a full path using its short ID.
     */
    public static String get(String id) {
        return pathMap.get(id);
    }

    /**
     * Removes a path from the cache using its short ID.
     */
    public static void remove(String id) {
        pathMap.remove(id);
    }
}