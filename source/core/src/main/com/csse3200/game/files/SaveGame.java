package com.csse3200.game.files;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.difficulty.Difficulty;
import com.csse3200.game.areas.difficulty.DifficultyType;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.attachments.BulletEnhancerComponent;
import com.csse3200.game.components.attachments.LaserComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.AvatarRegistry;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * class for saving all necassary components of a game so
 * that it can be retrieved by the save load service and then injected
 * into the Maingame
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
        private information player;
        private String gameArea;
        private int wave;
        private ArrayList<itemInInven> loadedInventory;
        private String difficulty;
        private ArrayList<String> areasVisited;

        public GameState() {
        }

        public void setAreasVisited(Set<String> areasVisited) {
            this.areasVisited = new ArrayList<>(areasVisited);
        }

        public Set<String> getAreasVisited() {
            return new HashSet<>(this.areasVisited);
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public DifficultyType getDifficulty() {
            for (DifficultyType diff: DifficultyType.values()) {
                if (diff.toString().equals(this.difficulty)) return diff;
            }
            logger.error("couldnt read difficulty {} from" +
                    " save file setting to normal", difficulty);
            return DifficultyType.NORMAL;
        }

        public information getPlayer() {
            return player;
        }

        /**
         * Gather Important details for creating a player entity that needs to be set or adjusted
         * upon character creation
         *
         * @param playerInfo entity of the player
         */
        public void setPlayer(Entity playerInfo) {
            this.player = new information();
            this.player.playerPos = playerInfo.getPosition();
            this.player.avatar = AvatarRegistry.get().texturePath(); // gets current avatar
            this.player.processor = playerInfo.getComponent(InventoryComponent.class).getProcessor();
            this.player.ammoReserve = playerInfo.getComponent(AmmoStatsComponent.class).getAmmo();
            this.player.stamina = playerInfo.getComponent(StaminaComponent.class).getStamina();
            // when/if maximum stamina can be increased put in here otherwise remove
            this.player.maxStamina = playerInfo.getComponent(StaminaComponent.class).getStamina(); // ->Max stamina variable
            this.player.maxHealth = playerInfo.getComponent(CombatStatsComponent.class).getMaxHealth();
            this.player.currentHealth = playerInfo.getComponent(CombatStatsComponent.class).getHealth();
            this.player.keyCardLevel = playerInfo.getComponent(InventoryComponent.class).getKeycardLevel();
            logger.info("player set successfully {}", this.player);
        }

        // saves any necessary information to do with gameArea (currently only needs to be string)
        public void setArea(GameArea area) {
            this.gameArea = area.toString();
        }

        public String getGameArea() {
            return this.gameArea;
        }

        //Once the waves have full Functionality this will be accessible and set the Enemy units
        public int getWave() {
            return this.wave;
        }

        public void setWave(int wave) {
            this.wave = Math.max(wave, 0);
        }

        //due to nature of json files or that im just silly this was a better implementation then making setInventory
        //a public variable
        public void setLoadedInventory(InventoryComponent inventory) {
            loadedInventory = setInventory(inventory);
        }

        /**
         * retrieves player inventory to be stored into json file
         */

        public ArrayList<itemInInven> getInventory() {
            return loadedInventory;
        }

        /**
         * retrieves player inventory to be stored into json file
         *
         * @param inventory The players inventory component
         */
        private ArrayList<itemInInven> setInventory(InventoryComponent inventory) {
            ArrayList<itemInInven> inventoryFilter = new ArrayList<>();

            itemInInven itemiser;
            for (Entity e : inventory.getInventory()) {
                //to ensure the inventory is populated by null items
                itemiser = null;

                if (e != null && e.hasComponent(ItemComponent.class)) {

                    ItemComponent inventoryItem = e.getComponent(ItemComponent.class);

                    itemiser = new itemInInven();
                    // will set the consumable components as they do not have unique parts
                    itemiser.type = inventoryItem.getType();
                    itemiser.texture = inventoryItem.getTexture();
                    itemiser.count = inventoryItem.getCount();
                    itemiser.ammo = null;
                    itemiser.upgradeStage = 1;

                    // checks if item is a weapon or not
                    if (e.hasComponent(WeaponsStatsComponent.class)) {
                        WeaponsStatsComponent weapon = e.getComponent(WeaponsStatsComponent.class);

                        //last sorting will split between melee weapons and ranged weapons
                        if (e.hasComponent(MagazineComponent.class)) {
                            itemiser.ammo = e.getComponent(MagazineComponent.class).getCurrentAmmo();
                        }
                        itemiser.upgradeStage = weapon.getUpgradeStage();
                    }
                    if (e.hasComponent(LaserComponent.class)) {
                        itemiser.Attachments.add("laser");
                    }
                    if (e.hasComponent(BulletEnhancerComponent.class)) {
                        itemiser.Attachments.add("bullet");
                    }
                }
                inventoryFilter.add(itemiser);
            }
            return inventoryFilter;
        }
    }

    /**
     * helper class to improve the readibility of the output json file
     * If new components that the Player needs to have saved insert here - yes bad practice to change existing code
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

        //needs a noargs constructor
        public information() {
        }

    }

    /**
     * helper class that cleans up json file for ease of readibility
     */
    public static class itemInInven {
        public ItemTypes type;
        public Integer ammo;
        public String texture;
        public int count;
        public int upgradeStage;
        public String name;
        public ArrayList<String> Attachments;

        public itemInInven() {
        }

    }
}
