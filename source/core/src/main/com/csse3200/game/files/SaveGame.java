package com.csse3200.game.files;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.items.ConsumableComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import jdk.jshell.execution.LoaderDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * class for saving all necassary components of a game so
 * that it can be retrieved by the save load service
 */
public class SaveGame {
    private static final Logger logger = LoggerFactory.getLogger(SaveGame.class);

    public static GameState loadGame(String fileName) {
        return FileLoader.readClass(GameState.class, fileName);
    }

    public static void saveGame(GameState gameState,String fileName) {
        FileLoader.writeClass(gameState, fileName, FileLoader.Location.LOCAL);
    }



    public static class GameState {
        private Entity player;
        private InventoryComponent inventory;
        private GameArea savedArea;
        Json json = new Json();
        public ArrayList loadedInventory = new ArrayList();




        public void setInventory(InventoryComponent inventory) {
//                if (items.getId())
            this.inventory = inventory;
        }

        public void setPlayer(Entity player) {
            this.player = player;
        }

//        public void saveEntities(List<Entity> entities) {
//            for (Entity entity : entities) {
//                if (entity != null) {
//                    logger.info("Saving entity: {}", entity);
//                    this.entities.add(entity);
//                }
//            }
//        }

        public void setArea(GameArea area) {
            this.savedArea = area;
        }

        public void getPlayerInventory() {
            ArrayList inventoryitem = new ArrayList();
            Json json = new Json(JsonWriter.OutputType.minimal);
            StringWriter output = new StringWriter();
            JsonWriter writer = new JsonWriter(output);
            json.setWriter(writer);

            json.writeObjectStart();
            json.writeArrayStart("inventory");
            itemRetrieve itemiser = null;
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.get(i).getComponent(ItemComponent.class) != null) {
                    Entity item = inventory.get(i);
                    ItemComponent inventoryItem = item.getComponent(ItemComponent.class);

                    if (item.hasComponent(WeaponsStatsComponent.class)) {
                        json.writeArrayStart();
                        WeaponsStatsComponent weapon = item.getComponent(WeaponsStatsComponent.class);

                        itemiser = new itemRetrieve(
                                inventoryItem.getType(),
                                item.getComponent(MagazineComponent.class).getCurrentAmmo(),
                                inventoryItem.getTexture(),
                                inventoryItem.getCount(),
                                weapon.getUpgradeStage());
                        inventoryitem.add(item.getComponent(MagazineComponent.class).getCurrentAmmo());
                        inventoryitem.add(inventoryItem.getTexture());

                        json.writeValue(weapon.getUpgradeStage());
                        json.writeValue(item.getComponent(MagazineComponent.class).getCurrentAmmo());
                        json.writeValue(inventoryItem.getTexture());

                    } else if (item.hasComponent(ConsumableComponent.class)) {
                        inventoryitem.add(inventoryItem.getCount());
                        inventoryitem.add(inventoryItem.getTexture());
                        inventoryitem.add(inventoryItem.getType());
                    }
                }
                json.writeArrayEnd();
                loadedInventory.add(itemiser.items());
                inventoryitem.clear();
            }
            json.writeArrayEnd();

            json.writeObjectEnd();

//            try {
//                writer.flush();
//                writer.close();
//            } catch (IOException e) {
//                logger.error("Error while flushing inventory", e);
//                e.printStackTrace();
//            }

        }
    }

    public static class itemRetrieve {
//        public String name;
        public ItemTypes type;
        public int ammo;
        public String texture;
        public int count;
        public int upgradeStage;


        public itemRetrieve(ItemTypes type, int ammo, String texture, int count, int upgradeStage) {
            this.type = type;
            this.ammo = ammo;
            this.texture = texture;
            this.count = count;
            this.upgradeStage = upgradeStage;
        }

        public List items() {
            List<Object> items = new ArrayList<>();
            items.add(type);
            items.add(ammo);
            items.add(texture);
            items.add(count);
            items.add(upgradeStage);
            return items;
        }


    }
}
