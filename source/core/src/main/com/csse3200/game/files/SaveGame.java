package com.csse3200.game.files;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.items.ConsumableComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import com.csse3200.game.services.ServiceLocator;
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
        private information player;

//        private InventoryComponent inventory;
//        private GameArea savedArea;
        private String gameArea;
        private int wave;
        private ArrayList loadedInventory;

        public void setPlayer(Entity player) {
            this.player = new information(
                    "placeholder",
                    player.getComponent(InventoryComponent.class).getProcessor(),
                    player.getComponent(CombatStatsComponent.class).getHealth(),
                    player.getComponent(StaminaComponent.class).getStamina(),
                    player.getComponent(StaminaComponent.class).getStamina(), // fix to get maximum later
                    player.getComponent(CombatStatsComponent.class).getMaxHealth(),
                    player.getComponent(CombatStatsComponent.class).getHealth()
            );
        }

        public void setArea(GameArea area) {
//            this.savedArea = area;
            this.gameArea = area.toString();
        }

        public void setWave(int wave) {
            this.wave = wave;
        }
        /**
         * retrieves player inventory to be stored into json file
         */
        public void setInventory(InventoryComponent inventory) {
            loadedInventory = new ArrayList();
            itemRetrieve itemiser = null;
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.get(i).hasComponent(ItemComponent.class)) {
                    Entity item = inventory.get(i);
                    ItemComponent inventoryItem = item.getComponent(ItemComponent.class);

                    if (item.hasComponent(WeaponsStatsComponent.class)) {

                        WeaponsStatsComponent weapon = item.getComponent(WeaponsStatsComponent.class);
                        if (item.hasComponent(MagazineComponent.class)) {
                            itemiser = new itemRetrieve(
                                    inventoryItem.getType(),
                                    item.getComponent(MagazineComponent.class).getCurrentAmmo(),
                                    inventoryItem.getTexture(),
                                    inventoryItem.getCount(),
                                    weapon.getUpgradeStage());
                        } else {
                            itemiser = new itemRetrieve(
                                    inventoryItem.getType(),
                                    null,
                                    inventoryItem.getTexture(),
                                    inventoryItem.getCount(),
                                    weapon.getUpgradeStage());
                        }

                    } else if (item.hasComponent(ConsumableComponent.class)) {
                        itemiser = new itemRetrieve(
                                inventoryItem.getType(),
                                null,
                                inventoryItem.getTexture(),
                                inventoryItem.getCount(),
                                1);

                    }
                }
                loadedInventory.add(itemiser);
            }
        }
    }

    private static class information {
        private String avatar;
        private int ammoReserve;
        private float stamina;
        private float MaxStamina;
        private int maxHealth;
        private int currentHealth;
        private int processor;

        private information(String avatar, int processors, int ammoReserve,
                            float stamina, float maxStamina,
                            int maxHealth, int currentHealth) {
            this.processor = processors;
            this.avatar = avatar;
            this.ammoReserve = ammoReserve;
            this.maxHealth = maxHealth;
            this.currentHealth = currentHealth;
            this.stamina = stamina;
            this.MaxStamina = MaxStamina;
        }
    }
    /**
     * helper class that cleans up json file for ease of readibility
     */
    public static class itemRetrieve {
        private ItemTypes type;
        private Integer ammo;
        private String texture;
        private int count;
        private int upgradeStage;


        public itemRetrieve(ItemTypes type, Integer ammo, String texture, int count, int upgradeStage) {
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
