package com.csse3200.game.files;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * class for saving all necassary components of a game so
 * that it can be retrieved by the save load service
 */
public class SaveGame {
    private static final Logger logger = LoggerFactory.getLogger(SaveGame.class);

    public static GameState loadGame(String fileName) {
        return FileLoader.readPlayer(GameState.class, fileName, FileLoader.Location.LOCAL);
    }

    public static void saveGame(GameState gameState,String fileName) {
        FileLoader.writeClass(gameState, fileName, FileLoader.Location.LOCAL);
    }

    /**
     * sets and gets all necessary game information
     */
    public static class GameState {
//        private HashMap<String, Object> player;
        private information player;
        private String gameArea;
        private int wave;
        private ArrayList<itemRetrieve> loadedInventory;

        public GameState() {}

        public void setPlayer(Entity playerInfo) {
//            this.player = new HashMap<>();
//            player.put("ammoReserve", playerInfo.getComponent(AmmoStatsComponent.class).getAmmo());
//            player.put("avatar", "placeholder for avatar");
//            player.put("stamina", playerInfo.getComponent(StaminaComponent.class).getStamina());
//            player.put("maxStamina", playerInfo.getComponent(StaminaComponent.class).getStamina());
//            player.put("maxhealth", playerInfo.getComponent(CombatStatsComponent.class).getMaxHealth());
//            player.put("health", playerInfo.getComponent(CombatStatsComponent.class).getHealth());
//            player.put("processors", playerInfo.getComponent(InventoryComponent.class).getProcessor());
//            logger.debug("playerInfo: {}", player);
            this.player = new information();
            this.player.avatar = "placeholder";
            this.player.processor = playerInfo.getComponent(InventoryComponent.class).getProcessor();
            this.player.ammoReserve = playerInfo.getComponent(AmmoStatsComponent.class).getAmmo();
            this.player.stamina = playerInfo.getComponent(StaminaComponent.class).getStamina();
            this.player.stamina = playerInfo.getComponent(StaminaComponent.class).getStamina(); // ->Max stamina variable
            this.player.maxHealth = playerInfo.getComponent(CombatStatsComponent.class).getMaxHealth();
            this.player.currentHealth = playerInfo.getComponent(CombatStatsComponent.class).getHealth();
            logger.info("player set successfully {}", this.player);
        }

//        public HashMap<String, Object> getPlayer() {
//            return player;
//        }

        public information getPlayer() {
            return player;
        }
        public void setArea(GameArea area) {
            this.gameArea = area.toString();
        }

        public String getGameArea() {
            return this.gameArea;
        }

        public void setWave(int wave) {
            this.wave = wave;
        }

        public int getWave() {return this.wave;}

        public void setLoadedInventory(InventoryComponent inventory) {
            loadedInventory = setInventory(inventory);
        }
        /**
         * retrieves player inventory to be stored into json file
         */

        public ArrayList<itemRetrieve> getInventory() {
            return loadedInventory;
        }

//        public HashMap<String, Object> getPlayerInfo() {
//            HashMap<String, Object> playerInfo = new HashMap<>();
//            playerInfo.put("ammoReserve", player.ammoReserve);
//            playerInfo.put("avatar", player.avatar);
//            playerInfo.put("stamina", player.stamina);
//            playerInfo.put("maxStamina", player.MaxStamina);
//            playerInfo.put("maxhealth", player.maxHealth);
//            playerInfo.put("health", player.currentHealth);
//            playerInfo.put("processors", player.processor);
//            logger.debug("playerInfo: {}", playerInfo);
//            return playerInfo;
//        }

        /**
         * retrieves player inventory to be stored into json file
         */
        private ArrayList<itemRetrieve> setInventory(InventoryComponent inventory) {
            ArrayList<itemRetrieve> inventoryFilter = new ArrayList();
            itemRetrieve itemiser = null;
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.get(i).hasComponent(ItemComponent.class)) {
                    Entity item = inventory.get(i);
                    ItemComponent inventoryItem = item.getComponent(ItemComponent.class);

                    if (item.hasComponent(WeaponsStatsComponent.class)) {

                        WeaponsStatsComponent weapon = item.getComponent(WeaponsStatsComponent.class);
                        if (item.hasComponent(MagazineComponent.class)) {
//                            itemiser = new itemRetrieve(
//                                    inventoryItem.getType(),
//                                    item.getComponent(MagazineComponent.class).getCurrentAmmo(),
//                                    inventoryItem.getTexture(),
//                                    inventoryItem.getCount(),
//                                    weapon.getUpgradeStage());
                            itemiser = new itemRetrieve();
                            itemiser.type = inventoryItem.getType().getString();
                            itemiser.ammo = item.getComponent(MagazineComponent.class).getCurrentAmmo();
                            itemiser.texture = inventoryItem.getTexture();
                            itemiser.count = inventoryItem.getCount();
                            itemiser.upgradeStage = weapon.getUpgradeStage();
                        } else {
//                            itemiser = new itemRetrieve(
//                                    inventoryItem.getType(),
//                                    null,
//                                    inventoryItem.getTexture(),
//                                    inventoryItem.getCount(),
//                                    weapon.getUpgradeStage());
                            itemiser = new itemRetrieve();
                            itemiser.type = inventoryItem.getType().getString();
                            itemiser.ammo = null;
                            itemiser.texture = inventoryItem.getTexture();
                            itemiser.count = inventoryItem.getCount();
                            itemiser.upgradeStage = weapon.getUpgradeStage();
                        }

                    } else if (item.hasComponent(ConsumableComponent.class)) {
//                        itemiser = new itemRetrieve(
//                                inventoryItem.getType(),
//                                null,
//                                inventoryItem.getTexture(),
//                                inventoryItem.getCount(),
//                                1);
                        itemiser = new itemRetrieve();
                        itemiser.type = inventoryItem.getType().getString();
                        itemiser.ammo = null;
                        itemiser.texture = inventoryItem.getTexture();
                        itemiser.count = inventoryItem.getCount();
                        itemiser.upgradeStage = 1;

                    }
                }
                inventoryFilter.add(itemiser);
            }
            return inventoryFilter;
        }

    }

    public static class information {
        public String avatar;
        public int ammoReserve;
        public float stamina;
        public float MaxStamina;
        public int maxHealth;
        public int currentHealth;
        public int processor;

//        public information(String avatar, int processors, int ammoReserve,
//                            float stamina, float maxStamina,
//                            int maxHealth, int currentHealth) {
//            this.processor = processors;
//            this.avatar = avatar;
//            this.ammoReserve = ammoReserve;
//            this.maxHealth = maxHealth;
//            this.currentHealth = currentHealth;
//            this.stamina = stamina;
//            this.MaxStamina = maxStamina;
//        }
        public information() {}

    }
    /**
     * helper class that cleans up json file for ease of readibility
     */
    public static class itemRetrieve {
        public String type;
        public Integer ammo;
        public String texture;
        public int count;
        public int upgradeStage;

        public itemRetrieve() {}

//        public itemRetrieve(ItemTypes type, Integer ammo, String texture, int count, int upgradeStage) {
//            this.type = type;
//            this.ammo = ammo;
//            this.texture = texture;
//            this.count = count;
//            this.upgradeStage = upgradeStage;
//        }
//
//        public List items() {
//            List<Object> items = new ArrayList<>();
//            items.add(type);
//            items.add(ammo);
//            items.add(texture);
//            items.add(count);
//            items.add(upgradeStage);
//            return items;
//        }
    }
}
