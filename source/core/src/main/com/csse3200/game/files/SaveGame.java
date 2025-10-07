package com.csse3200.game.files;

import com.badlogic.gdx.math.Vector2;
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


/**
 * class for saving all necassary components of a game so
 * that it can be retrieved by the save load service
 */
public class SaveGame {
    private static final Logger logger = LoggerFactory.getLogger(SaveGame.class);

    public static GameState loadGame(String fileName) {
        return FileLoader.readPlayer(GameState.class, fileName, FileLoader.Location.LOCAL);
    }

    public static void saveGame(GameState gameState, String fileName) {
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

        public GameState() {
        }

        public information getPlayer() {
            return player;
        }

        public void setPlayer(Entity playerInfo) {
            this.player = new information();
            this.player.playerPos = playerInfo.getPosition();
            this.player.avatar = "placeholder";
            this.player.processor = playerInfo.getComponent(InventoryComponent.class).getProcessor();
            this.player.ammoReserve = playerInfo.getComponent(AmmoStatsComponent.class).getAmmo();
            this.player.stamina = playerInfo.getComponent(StaminaComponent.class).getStamina();
            this.player.maxStamina = playerInfo.getComponent(StaminaComponent.class).getStamina(); // ->Max stamina variable
            this.player.maxHealth = playerInfo.getComponent(CombatStatsComponent.class).getMaxHealth();
            this.player.currentHealth = playerInfo.getComponent(CombatStatsComponent.class).getHealth();
            this.player.keyCardLevel = playerInfo.getComponent(InventoryComponent.class).getKeycardLevel();
            logger.info("player set successfully {}", this.player);
        }

        public void setArea(GameArea area) {
            this.gameArea = area.toString();
        }

        public String getGameArea() {
            return this.gameArea;
        }

        public int getWave() {
            return this.wave;
        }

        public void setWave(int wave) {
            this.wave = wave;
        }

        public void setLoadedInventory(InventoryComponent inventory) {
            loadedInventory = setInventory(inventory);
        }

        /**
         * retrieves player inventory to be stored into json file
         */

        public ArrayList<itemRetrieve> getInventory() {
            return loadedInventory;
        }

        /**
         * retrieves player inventory to be stored into json file
         */
        private ArrayList<itemRetrieve> setInventory(InventoryComponent inventory) {
            ArrayList<itemRetrieve> inventoryFilter = new ArrayList<>();
            itemRetrieve itemiser = null;
            for (int i = 0; i < inventory.getSize(); i++) {
                if (inventory.get(i).hasComponent(ItemComponent.class)) {
                    Entity item = inventory.get(i);
                    ItemComponent inventoryItem = item.getComponent(ItemComponent.class);

                    if (item.hasComponent(WeaponsStatsComponent.class)) {

                        WeaponsStatsComponent weapon = item.getComponent(WeaponsStatsComponent.class);
                        if (item.hasComponent(MagazineComponent.class)) {

                            itemiser = new itemRetrieve();
                            itemiser.type = inventoryItem.getType();
                            itemiser.ammo = item.getComponent(MagazineComponent.class).getCurrentAmmo();
                            itemiser.texture = inventoryItem.getTexture();
                            itemiser.count = inventoryItem.getCount();
                            itemiser.upgradeStage = weapon.getUpgradeStage();
                        } else {

                            itemiser = new itemRetrieve();
                            itemiser.type = inventoryItem.getType();
                            itemiser.ammo = 0;
                            itemiser.texture = inventoryItem.getTexture();
                            itemiser.count = inventoryItem.getCount();
                            itemiser.upgradeStage = weapon.getUpgradeStage();
                        }

                    } else if (item.hasComponent(ConsumableComponent.class)) {

                        itemiser = new itemRetrieve();
                        itemiser.type = inventoryItem.getType();
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

    /**
     * helper class to improve the readibility of the output json file and
     */
    public static class information {
        public Vector2 playerPos;
        public String avatar;
        public int ammoReserve;
        public float stamina;
        public float maxStamina;
        public int maxHealth;
        public int currentHealth;
        public int processor;
        public int keyCardLevel;

        public information() {
        }

    }

    /**
     * helper class that cleans up json file for ease of readibility
     */
    public static class itemRetrieve {
        public ItemTypes type;
        public Integer ammo;
        public String texture;
        public int count;
        public int upgradeStage;

        public itemRetrieve() {
        }

    }
}
