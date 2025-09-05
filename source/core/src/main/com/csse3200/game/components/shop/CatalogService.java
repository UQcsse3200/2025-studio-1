package com.csse3200.game.components.shop;

import com.csse3200.game.entities.Entity;
import java.util.Map;
import java.util.function.Function;

public class CatalogService {
    private final Map<String, CatalogEntry> entries;
    private final Function<String, Entity> factory;

    public CatalogService(Map<String, CatalogEntry> entries,
                           Function<String, Entity> factory) {
        this.entries = entries;
        this.factory = factory;
    }

    public CatalogEntry get(String itemId) {
        return entries.get(itemId);
    }

    public Entity spawnEntity(String itemId) {
        return factory.apply(itemId);
    }
}