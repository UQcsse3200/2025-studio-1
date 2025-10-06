package com.csse3200.game.entities.configs;


import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.entities.spawner.ItemSpawner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sets up item spawn configurations for different maps
 * To add items for a new map make a new method like FOREST_MAP
 */
public class ItemSpawnConfig {

    /**
     * Gives a list of items(including weapons) and their spawn locations for the forest map
     * Each item can have multiple spawn locations and each location can have multiple items
     *
     * @return a map of item names to a list of spawn info (location and quantity)
     */
    // Forest Map
    public static Map<String, List<ItemSpawner.ItemSpawnInfo>> forestmap() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();

        // Weapons
        config.put(Weapons.DAGGER.name(), Arrays.asList(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(9, 7), 1),
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(5, 15), 1)
        ));
        config.put(Weapons.PISTOL.name(), Arrays.asList(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(11, 7), 1),
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(11, 12), 1)
        ));
        config.put(Weapons.RIFLE.name(), List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(16, 9), 1)
        ));
        config.put(Weapons.LIGHTSABER.name(), List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(14, 7), 1)
        ));


        return config;
    }

    /**
     * Gives a list of items(including weapons) and their spawn locations for the shipping map
     * Each item can have multiple spawn locations and each location can have multiple items
     *
     * @return a map of item names to a list of spawn info (location and quantity)
     */
    public static Map<String, List<ItemSpawner.ItemSpawnInfo>> shippingmap() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();

        config.put(Weapons.RIFLE.name(), List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(5, 7), 1)
        ));

        return config;
    }

    /**
     * Gives a list of items(including weapons) and their spawn locations for the storage map
     * Each item can have multiple spawn locations and each location can have multiple items
     *
     * @return a map of item names to a list of spawn info (location and quantity)
     */
    public static Map<String, List<ItemSpawner.ItemSpawnInfo>> storage1map() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();

        config.put(Weapons.RIFLE.name(), List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(4, 17), 1)
        ));

        return config;
    }

    /**
     * Server Room spawning. Spawns a rifle on the purple spawn pad
     */
    public static Map<String, List<ItemSpawner.ItemSpawnInfo>> servermap() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();

        config.put(Weapons.RIFLE.name(), List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(25, 16), 1)
        ));

        return config;
    }

    /**
     * Server Room spawning. Spawns a rifle on the purple spawn pad
     */
    public static Map<String, List<ItemSpawner.ItemSpawnInfo>> tunnelmap() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();

        config.put(Weapons.PISTOL.name(), List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(8, 18), 1)
        ));

        return config;
    }

    /**
     * Research Room spawning. Spawns a pistol, lightsaber, rifle in the map on various location
     */
    public static Map<String, List<ItemSpawner.ItemSpawnInfo>> researchmap() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();

        config.put(Weapons.PISTOL.name(), List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(1, 12), 1)
        ));
        config.put(Weapons.RIFLE.name(), List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(25, 16), 1)
        ));
        config.put(Weapons.LIGHTSABER.name(), List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(22, 6), 1)
        ));


        return config;
    }

    /**
     * Security Room spawning. Spawns a pistol, lightsaber, rifle in the map on various location
     */
    public static Map<String, List<ItemSpawner.ItemSpawnInfo>> securitymap() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();

        config.put(Weapons.PISTOL.name(), List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(24, 11), 1)
        ));
        config.put(Weapons.LIGHTSABER.name(), List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(22, 6), 1)
        ));


        return config;
    }

    /**
     * Boss room Spawning. Spawns a rifle next to the player
     */
    public static Map<String, List<ItemSpawner.ItemSpawnInfo>> bossmap() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();

        config.put(Weapons.RIFLE.name(), List.of(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(5, 7), 1)
        ));

        return config;
    }
    //  for a new map just add more methods

}
