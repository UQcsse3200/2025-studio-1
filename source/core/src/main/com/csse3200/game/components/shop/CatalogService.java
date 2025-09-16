package com.csse3200.game.components.shop;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides access to a collection of {@link CatalogEntry} objects.
 * <p>
 * The {@code CatalogService} acts as a container for catalog entries,
 * allowing lookup and retrieval of items available in the shop.
 * </p>
 */
public class CatalogService {
    private final ArrayList<CatalogEntry> entries;

    /**
     * Constructs a {@code CatalogService} with a given list of entries.
     *
     * @param entries The catalog entries to manage. Must not be {@code null}.
     */
    public CatalogService(List<CatalogEntry> entries) {
        this.entries = (ArrayList<CatalogEntry>) entries;
    }

    /**
     * Retrieves a catalog entry that matches the specified item.
     * <p>
     * The method performs an equality check against all entries in the service.
     * </p>
     *
     * @param item The catalog entry to search for.
     * @return The matching {@link CatalogEntry}, or {@code null} if not found.
     */
    public CatalogEntry get(CatalogEntry item) {
        for (CatalogEntry entry : entries) {
            if (entry.equals(item)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Returns the complete list of catalog entries managed by this service.
     *
     * @return A list of {@link CatalogEntry} objects.
     */
    public List<CatalogEntry> list() {
        return entries;
    }
}