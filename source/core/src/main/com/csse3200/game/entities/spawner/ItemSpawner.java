package com.csse3200.game.entities.spawner;

import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.WeaponsFactory;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.badlogic.gdx.physics.box2d.BodyDef;

import java.util.List;
import java.util.Map;


public class ItemSpawner {
    private final GameArea gameArea;

    public ItemSpawner(GameArea gameArea) {
        this.gameArea = gameArea;
    }

    public void spawnItems(Map<String, List<ItemSpawnInfo>> config) {
        for (String type : config.keySet()) {
            List<ItemSpawnInfo> spawnList = config.get(type);
            for (ItemSpawnInfo spawninfo : spawnList) {
                for (int count = 0; count < spawninfo.quantity; count++) {
                    Entity item = makeItem(type);
                    if (item == null)
                        continue;

                    PhysicsComponent physics = item.getComponent(PhysicsComponent.class);
                    if (physics != null) {
                        physics.setBodyType(BodyDef.BodyType.StaticBody);
                    }

                    if (gameArea instanceof ForestGameArea) {
                        ((ForestGameArea) gameArea).spawnItem(item, spawninfo.position);
                    }

                }
            }
        }
    }

        private Entity makeItem(String type) {
            switch (type.toLowerCase()) {
                case "dagger": return WeaponsFactory.createDagger();
                case "pistol": return WeaponsFactory.createPistol();
                case "rifle": return WeaponsFactory.createRifle();
                case "lightsaber": return WeaponsFactory.createLightsaber();
                default:
                    System.out.println("Oops! Unknown item type: " + type);
                    return null;
            }
        }

        public static class ItemSpawnInfo {
            public final GridPoint2 position;
            public final int quantity;

            public ItemSpawnInfo(GridPoint2 position, int quantity) {
                this.position = position;
                this.quantity = quantity;
            }
        }
    }

