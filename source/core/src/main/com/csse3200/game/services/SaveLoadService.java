package com.csse3200.game.services;

import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.difficulty.Difficulty;
import com.csse3200.game.areas.difficulty.DifficultyType;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.AvatarRegistry;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Consumables;
import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.configs.weapons.LightsaberConfig;
import com.csse3200.game.entities.factories.items.ConsumableFactory;
import com.csse3200.game.entities.factories.items.WeaponsFactory;
import com.csse3200.game.files.SaveGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;


/**
 * save load service that will extract all information about the current game state and will add it to save file to
 * be loaded.
 */
public class SaveLoadService {
    private static final Logger logger = LoggerFactory.getLogger(SaveLoadService.class);
    private String path;

    /**
     * Load a save file from local storage and rebuild the area and the current
     * players stats.
     */
    public static SaveGame.GameState load() {
        String filePath = "saves" + File.separator + "slides.json";
        SaveGame.GameState savedGame;
        if (SaveGame.loadGame(filePath) != null) {
            savedGame = SaveGame.loadGame(filePath);
        } else {
            logger.error("failed to load in game");
            savedGame = null;
        }
        return savedGame;
    }

    /**
     * sets all the players stats based on the information from the save file json
     *
     * @param playerStats
     */
    public static void loadPlayer(SaveGame.information playerStats) {
        ServiceLocator.getPlayer().getComponent(
                CombatStatsComponent.class).setHealth(playerStats.currentHealth);
        ServiceLocator.getPlayer().getComponent(
                CombatStatsComponent.class).setMaxHealth(playerStats.maxHealth);
        ServiceLocator.getPlayer().getComponent(
                StaminaComponent.class).setStamina(playerStats.stamina);
        ServiceLocator.getPlayer().getComponent(
                InventoryComponent.class).setProcessor(playerStats.processor);
        ServiceLocator.getPlayer().getComponent(
                InventoryComponent.class).setKeycardLevel(playerStats.keyCardLevel);
        ServiceLocator.getPlayer().getComponent(
                AmmoStatsComponent.class).setAmmo(playerStats.ammoReserve);
        ServiceLocator.getPlayer().setPosition(playerStats.playerPos);


        //TEAMNOTE: set Avatar in next sprint or later tonight before PR
    }

    /**
     * loads plays items into their inventory
     *
     * @param inventory from the json file to be loaded
     */
    public static void loadPlayerInventory(ArrayList<SaveGame.itemInInven> inventory) {
        Entity itemEntity;
        for (SaveGame.itemInInven item : inventory) {
            itemEntity = null;

            switch (item.type) {
                case RANGED, MELEE -> itemEntity = getWeapon(item);
                case CONSUMABLE -> itemEntity = getConsumable(item.texture);
            }

            assert itemEntity != null;
            if (itemEntity.hasComponent(WeaponsStatsComponent.class)) {
                while (itemEntity.getComponent(WeaponsStatsComponent.class).getUpgradeStage() < item.upgradeStage) {
                    itemEntity.getComponent(WeaponsStatsComponent.class).upgrade();
                }
            }
            itemEntity.getComponent(ItemComponent.class).setCount(item.count);
            InventoryComponent loadedInventory = ServiceLocator.getPlayer().getComponent(InventoryComponent.class);
            loadedInventory.addItem(itemEntity);
        }
    }

    /**
     * Save the current GameArea to local storage (saves/slotX.json).
     */
    public boolean save(String slot, GameArea gameArea) {
        Entity player = new Entity();
        if (ServiceLocator.getGameArea() != null) {
            player = ServiceLocator.getPlayer();
        } else {
            // if can't find through service locator will attempt hard check
            for (Entity entity : gameArea.getEntities()) {
                if (entity.getComponent(InventoryComponent.class) != null) {
                    player = entity;
                }
            }
        }

        SaveGame.GameState gamestate = new SaveGame.GameState();
        gamestate.setPlayer(player);
        gamestate.setLoadedInventory(player.getComponent(InventoryComponent.class));
        gamestate.setArea(ServiceLocator.getGameArea());
        gamestate.setWave(gameArea.currentWave());
        gamestate.setAreasVisited(ServiceLocator.getDiscoveryService().getDiscovered());
        gamestate.setDifficulty(ServiceLocator.getDifficulty().toString());

        path = "saves" + File.separator + slot + ".json";

        SaveGame.saveGame(gamestate, path);
        return true;
    }

    private static Entity getWeapon(SaveGame.itemInInven item) {
        for (Weapons weapon : Weapons.values()) {
            if (item.texture.equals(weapon.getConfig().texturePath)) {
                return WeaponsFactory.createWeapon(weapon);
            }
        }
        return null;
    }

    private static Entity getConsumable(String texture) {
        for (Consumables consumable : Consumables.values()) {
            if (texture.equals(consumable.getConfig().texturePath)) {
                return ConsumableFactory.createConsumable(consumable);
            }
        }
        return null;
    }
}