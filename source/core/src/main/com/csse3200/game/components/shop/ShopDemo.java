package com.csse3200.game.components.shop;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.items.ConsumableFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;

import java.util.HashMap;
import java.util.Map;

import static com.csse3200.game.entities.configs.Consumables.GENERIC_HEAL_ITEM;

public class ShopDemo {
    public static CatalogService makeDemoCatalog() {
        Map<String, CatalogEntry> demoEntries = new HashMap<>();
        Entity healthPotion = ConsumableFactory.createConsumable(GENERIC_HEAL_ITEM);
        Entity weapon = WeaponsFactory.createWeapon(Weapons.PISTOL);

        // Add one simple item (no icon -> null)
        demoEntries.put("HealthPotion", new CatalogEntry(
                healthPotion,   // itemKey
                50,               // price
                true,             // enabled
                true,             // stackable
                10,               // maxStack
                1                // bundleQuantity
        ));

        demoEntries.put("Dagger", new CatalogEntry(
                weapon,
                200,               // price
                false,             // enabled
                true,             // stackable
                10,               // maxStack
                1               // bundleQuantity
        ));

        return new CatalogService(demoEntries, itemKey -> {
            // Simple factory: create a blank Entity for now
            return new com.csse3200.game.entities.Entity();
        });
    }
}

