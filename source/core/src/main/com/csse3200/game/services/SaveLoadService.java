package com.csse3200.game.services;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.AmmoStatsComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.StaminaComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Consumables;
import com.csse3200.game.entities.configs.Weapons;
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
        SaveGame.GameState savedGame = SaveGame.loadGame(filePath);

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
    public static void loadPlayerInventory(ArrayList<SaveGame.itemRetrieve> inventory) {
        for (SaveGame.itemRetrieve item : inventory) {
            Entity itemEntity = null;

            switch (item.type) {
                case MELEE:
                    if (item.texture.equals(Weapons.LIGHTSABER.getConfig().texturePath)) {
                        itemEntity = WeaponsFactory.createWeapon(Weapons.LIGHTSABER);
                    } else if (item.texture.equals(Weapons.DAGGER.getConfig().texturePath)) {
                        itemEntity = WeaponsFactory.createWeapon(Weapons.DAGGER);
                    }
                    break;
                case CONSUMABLE:
                    if (item.texture.equals(Consumables.HEALTH_MONSTER_DRINK.getConfig().texturePath)) {
                        itemEntity = ConsumableFactory.createConsumable(Consumables.HEALTH_MONSTER_DRINK);

                    } else if (item.texture.equals(Consumables.LIGHTNING_IN_A_BOTTLE.getConfig().texturePath)) {
                        itemEntity = ConsumableFactory.createConsumable(Consumables.LIGHTNING_IN_A_BOTTLE);
                    }

                    break;
                case RANGED:
                    if (item.texture.equals(Weapons.RIFLE.getConfig().texturePath)) {
                        itemEntity = WeaponsFactory.createWeapon(Weapons.PISTOL);
                    } else if (item.texture.equals(Weapons.PISTOL.getConfig().texturePath)) {
                        itemEntity = WeaponsFactory.createWeapon(Weapons.RIFLE);
                    }

                    assert itemEntity != null;
                    itemEntity.getComponent(MagazineComponent.class).setCurrentAmmo(item.ammo);
                    break;
                default:
                    logger.error("Invalid item type");
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
        PlayerInfo gs = new PlayerInfo();
        Entity player = new Entity();
        if (ServiceLocator.getGameArea() != null) {
            gs.areaId = ServiceLocator.getGameArea().toString();
            player = ServiceLocator.getPlayer();
        } else {
            gs.areaId = gameArea.toString();
            logger.error("failed to save Game area creating new instance");
            // if can't find through service locator will attempt hard check
            for (Entity entity : gameArea.getEntities()) {
                if (entity.getComponent(InventoryComponent.class) != null) {
                    player = entity;
                }
            }
        }

        // commented out because i might need this later
//        if (player.getComponent(InventoryComponent.class) != null) {
//                logger.info("Inventory component found: Player found.");
//                CombatStatsComponent stat = player.getComponent(CombatStatsComponent.class);
//                InventoryComponent inv = player.getComponent(InventoryComponent.class);
//                gs.inventory = new ArrayList<>();
//                for (int i = 0; i < inv.getSize(); i++) {
//                    if (inv.get(i).getComponent(ItemComponent.class) != null) {
//                        gs.inventory.add(inv.getTex(i));
//                    }
//                }
//
//        }
        gs.Health = player.getComponent(CombatStatsComponent.class).getHealth();
        gs.position.set(player.getPosition());
        gs.ProcessNumber = player.getComponent(InventoryComponent.class).getProcessor();
        // future solution
        gs.RoundNumber = 2;

        SaveGame.GameState gamestate = new SaveGame.GameState();
        gamestate.setPlayer(player);
        gamestate.setLoadedInventory(player.getComponent(InventoryComponent.class));
        gamestate.setArea(gameArea);
        gamestate.setWave(2);

        path = "saves" + File.separator + slot + ".json";

        SaveGame.saveGame(gamestate, path);
        return true;
    }

    /**
     * mock game state to store entities.
     */
    public static class PlayerInfo {
        public String areaId;
        public ArrayList<Object> inventory;
        public int Health;
        public int ProcessNumber;
        public Vector2 position = new Vector2();
        public int RoundNumber;
    }
}