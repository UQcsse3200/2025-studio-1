package com.csse3200.game.components.shop;

import java.util.HashMap;
import java.util.Map;

public class ShopDemo {
    public static CatalogService makeDemoCatalog() {
        Map<String, CatalogEntry> demoEntries = new HashMap<>();

        // Add one simple item (no icon -> null)
        demoEntries.put("HealthPotion", new CatalogEntry(
                "HealthPotion",   // itemKey
                50,               // price
                true,             // enabled
                true,             // stackable
                10,               // maxStack
                1,                // bundleQuantity
                CatalogEntry.loadIcon("images/box_boy.png")              // icon (null so ShopScreenDisplay shows text)
        ));

        demoEntries.put("Dagger", new CatalogEntry(
                "Dagger",   // itemKey
                200,               // price
                false,             // enabled
                true,             // stackable
                10,               // maxStack
                1,                // bundleQuantity
                CatalogEntry.loadIcon("images/dagger.png")              // icon (null so ShopScreenDisplay shows text)
        ));

        return new CatalogService(demoEntries, itemKey -> {
            // Simple factory: create a blank Entity for now
            return new com.csse3200.game.entities.Entity();
        });
    }
}

