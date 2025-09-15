package com.csse3200.game.components.shop;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.items.ConsumableFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.csse3200.game.entities.configs.Consumables.GENERIC_HEAL_ITEM;

public class ShopDemo {
    public static CatalogService makeDemoCatalog() {
      ArrayList<CatalogEntry> demoEntries=  new ArrayList<>();
        Entity healthPotion = ConsumableFactory.createConsumable(GENERIC_HEAL_ITEM);
        Entity weapon = WeaponsFactory.createWeapon(Weapons.RIFLE);
        System.out.println(healthPotion.getComponents());
        System.out.println(healthPotion.getEvents().getListeners());

        System.out.println(weapon.getComponents());
        System.out.println(weapon.getEvents().getListeners());

        // Add one simple item (no icon -> null)
        demoEntries.add(new CatalogEntry(
                healthPotion,   // itemKey
                1,               // price
                true,             // enabled
                10,               // maxStack
                1                // bundleQuantity
        ));

        demoEntries.add(new CatalogEntry(
                weapon,
                1,               // price
                true,             // enabled
                10,               // maxStack
                1               // bundleQuantity
        ));

        return new CatalogService(demoEntries);
    }
}

