package com.csse3200.game.components.shop;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.items.ConsumableFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;

import java.util.ArrayList;

import static com.csse3200.game.entities.configs.Consumables.GENERIC_HEAL_ITEM;
import static com.csse3200.game.entities.configs.Consumables.GENERIC_PROJECTILE_CONSUMABLE;

public class ShopDemo {
    public static CatalogService makeDemoCatalog() {

      // NOTE: temporary solution to add items. In the future, will be determined by room, player level, etc.

      ArrayList<CatalogEntry> demoEntries = new ArrayList<>();
        Entity healthPotion = ConsumableFactory.createConsumable(GENERIC_HEAL_ITEM);
        Entity bomb = ConsumableFactory.createConsumable(GENERIC_PROJECTILE_CONSUMABLE);
        Entity weapon = WeaponsFactory.createWeapon(Weapons.RIFLE);
        Entity lightsaber = WeaponsFactory.createWeapon(Weapons.LIGHTSABER);
        Entity pistol = WeaponsFactory.createWeapon(Weapons.PISTOL);
        Entity dagger = WeaponsFactory.createWeapon(Weapons.DAGGER);

        demoEntries.add(new CatalogEntry(
                healthPotion,
                1,
                true,
                10,
                1
        ));

        demoEntries.add(new CatalogEntry(
                bomb,
                1,
                true,
                10,
                1
        ));

        demoEntries.add(new CatalogEntry(
                weapon,
                10,
                true,
                1,
                1
        ));

        demoEntries.add(new CatalogEntry(
                lightsaber,
                10,
                true,
                1,
                1
        ));

        demoEntries.add(new CatalogEntry(
                pistol,
                10,
                true,
                1,
                1
        ));

        demoEntries.add(new CatalogEntry(
                dagger,
                10,
                true,
                1,
                1
        ));

        return new CatalogService(demoEntries);
    }
}

