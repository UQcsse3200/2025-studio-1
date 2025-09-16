package com.csse3200.game.entities.spawner;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.*;
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
        config.forEach(this::spawnItemsOfType);
    }

    /**
     * It spawns all items of a specific type at their designated coordinates and quantities
     * @param type it is the name of the item type
     * @param spawnList is a list of positions and quantities for this type
     */
    private void spawnItemsOfType(String type, List<ItemSpawnInfo> spawnList) {
        for (ItemSpawnInfo spawnInfo : spawnList) {
            spawnItemsAtLocation(type, spawnInfo);
        }
    }

    /**
     * It spawns item of a specific type at a specific location and quantity
     * @param type it is the name of the item type
     * @param spawnInfo it holds the position and quantity for this spawn
     */
    private void spawnItemsAtLocation(String type, ItemSpawnInfo spawnInfo) {
        for (int i = 0; i < spawnInfo.quantity; i++) {
            Entity item = makeItem(type);
            if (item == null) continue;
            makeItemStatic(item);
            spawnInGameArea(item, spawnInfo.position);
        }
    }

    /**
     * It makes the item static by setting it to StaticBody so that it doesn't move
     * @param item is the item which is made static
     */
    private void makeItemStatic(Entity item) {
        PhysicsComponent physics = item.getComponent(PhysicsComponent.class);
        if (physics != null) {
            physics.setBodyType(BodyDef.BodyType.StaticBody);
        }
    }

    /**
     * It spawns the item in different maps(game areas) at given positions
     * @param item is the item to be spawned
     * @param position is the position where the item is spawned
     * can add more game areas here
     */
    private void spawnInGameArea(Entity item, GridPoint2 position) {
        if (gameArea instanceof ForestGameArea forestArea) {
            forestArea.spawnItem(item, position);
        } else if (gameArea instanceof TunnelGameArea tunnelArea) {
            tunnelArea.spawnItem(item, position);
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

