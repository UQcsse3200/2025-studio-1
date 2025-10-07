package com.csse3200.game.services;

import com.badlogic.gdx.math.Vector2;

import com.csse3200.game.areas.*;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.items.ConsumableComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
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
//                setInventory();


//        gamestate.setInventory(player.getComponent(InventoryComponent.class));
        gamestate.setArea(gameArea);
        gamestate.setWave(2);

        path = "saves" + File.separator + slot + ".json";

        SaveGame.saveGame(gamestate, path);
//        FileLoader.writeClass(gamestate, path, FileLoader.Location.LOCAL);
//        FileLoader.writeClass(gamestate.loadedInventory, path, FileLoader.Location.LOCAL);
        return true;
    }


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

    public static class itemRetrieve {
        public ItemTypes type;
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