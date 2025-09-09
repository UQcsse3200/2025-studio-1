package com.csse3200.game.entities.configs;


import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.entities.spawner.ItemSpawner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemSpawnConfig {

    // Forest Map
    public static Map<String, List<ItemSpawner.ItemSpawnInfo>> FOREST_MAP() {
        Map<String, List<ItemSpawner.ItemSpawnInfo>> config = new HashMap<>();

        // Weapons
        config.put("dagger", Arrays.asList(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(5, 5), 1),
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(10, 8), 1)
        ));
        config.put("pistol", Arrays.asList(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(3, 7), 1),
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(12, 12), 1)
        ));
        config.put("rifle", Arrays.asList(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(8, 2), 1)
        ));
        config.put("lightsaber", Arrays.asList(
                new ItemSpawner.ItemSpawnInfo(new GridPoint2(12, 10), 1)
        ));


        return config;
    }
    //  for a new map just add more methods

}
