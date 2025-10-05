package com.csse3200.game.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Tracks which game areas (rooms) the player has discovered/visited.
 * Stores normalized names: lower-case, no spaces (e.g., "Main Hall" -> "mainhall").
 */
public class DiscoveryService {
    private final Set<String> discovered = new HashSet<>();

    /** Normalize area names consistently across the codebase. */
    public static String normalize(String name) {
        if (name == null) return "";
        return name.replace(" ", "").toLowerCase(Locale.ROOT);
    }

    /** Mark an area as discovered. */
    public void discover(String areaName) {
        discovered.add(normalize(areaName));
    }

    /** Returns true if an area has been discovered. */
    public boolean isDiscovered(String areaName) {
        return discovered.contains(normalize(areaName));
    }

    /** Read-only view of discovered area keys. */
    public Set<String> getDiscovered() {
        return Collections.unmodifiableSet(discovered);
    }

    /** Clear tracked discoveries (e.g., on new game). */
    public void clear() {
        discovered.clear();
    }
}
