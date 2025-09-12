package com.csse3200.game.entities.spawner;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.badlogic.gdx.physics.box2d.BodyDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

/**
 * Spawns items in the game area based on a provided configuration.
 * The configuration maps item types to their spawn locations and quantities.
 * Each item type should have a corresponding creation method in the WeaponsFactory or other relevant factory.
 */
public class ItemSpawner {
    private static final Logger logger = LoggerFactory.getLogger(ItemSpawner.class);
    private final GameArea gameArea;

    public ItemSpawner(GameArea gameArea) {
        this.gameArea = gameArea;
    }

    /**
     * Spawns items in the game area based on the provided information.
     * Each item type is created using the appropriate factory method.
     * @param config A map where keys are item types and values are lists of ItemSpawnInfo objects
     */
    public void spawnItems(Map<String, List<ItemSpawnInfo>> config) {
        for (String type : config.keySet()) {
            List<ItemSpawnInfo> spawnList = config.get(type);
            for (ItemSpawnInfo spawninfo : spawnList) {
                for (int count = 0; count < spawninfo.quantity; count++) {
                    Entity item = makeItem(type);
                    if (item == null)
                        continue;
                    //makes the item static so its doesnt move around the map when spawned
                    PhysicsComponent physics = item.getComponent(PhysicsComponent.class);
                    if (physics != null) {
                        physics.setBodyType(BodyDef.BodyType.StaticBody);
                    }
                    // spawns the item in the game area at the specified position
                    if (gameArea instanceof ForestGameArea) {
                        ((ForestGameArea) gameArea).spawnItem(item, spawninfo.position);
                    }

                }
            }
        }
    }

    /**
     * Creates an item entity based on the provided type
     * This method currently supports weapon types defined in the Weapons enum
     * Additional item types like consumables, perishables, etc. can be added accordingly
     * @param type which is the type of item to be created
     * @return the created item entity or null if the type is unknown
     */
        protected Entity makeItem(String type) {
            try {
                Weapons weapon = Weapons.valueOf(type.toUpperCase());
                return WeaponsFactory.createWeapon(weapon);
            } catch (IllegalArgumentException e) {
                //try consumables/perishables(other items)
                switch (type.toLowerCase()) {
                    default:
                        logger.warn("Unknown item type: {}", type);
                        return null;
                }
            }
        }

    /**
     * It holds the information needed to spawn an item
     */
    public static class ItemSpawnInfo {
            public final GridPoint2 position;
            public final int quantity;

            public ItemSpawnInfo(GridPoint2 position, int quantity) {
                this.position = position;
                this.quantity = quantity;
            }
        }
    }

