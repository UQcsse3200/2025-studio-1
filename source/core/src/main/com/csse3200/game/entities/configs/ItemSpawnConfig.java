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
        config.put(Weapons.RIFLE.name(), Arrays.asList(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(16, 9), 1)
        ));
        config.put(Weapons.LIGHTSABER.name(), Arrays.asList(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(14, 7), 1)
        ));


        return config;
    }

    /** Server Room spawning. Spawns a rifle on the purple spawn pad */
    public static Map<String, List<ItemSpawner.ItemSpawnInfo>> servermap() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();

        config.put(Weapons.RIFLE.name(), Arrays.asList(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(15, 15), 1)
        ));

        return config;
    }
    //  for a new map just add more methods

}
