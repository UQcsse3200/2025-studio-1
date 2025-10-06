package com.csse3200.game.services;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.csse3200.game.areas.*;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.SaveGame;
import com.csse3200.game.ui.terminal.commands.WavesCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


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


        // current placeholder for new class to improve cohesion between file savign and loading
//        SaveGame save = new SaveGame();
//        SaveGame.GameState gameState = new SaveGame.GameState();
//        gameState.setInventory(ServiceLocator.getGameArea().getPlayer().getComponent(InventoryComponent.class));
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
        gamestate.setInventory(player.getComponent(InventoryComponent.class));
        gamestate.setArea(gameArea);
        gamestate.setWave(2);
        // test for writing
//        gs.inventory = (gamestate.loadedInventory);

        path = "saves" + File.separator + slot + ".json";

        FileLoader.writeClass(gamestate, path, FileLoader.Location.LOCAL);
//        FileLoader.writeClass(gamestate.loadedInventory, path, FileLoader.Location.LOCAL);
        return true;
    }


    /**
     * Load a save file from local storage and rebuild the area + entities.
     */
    public static PlayerInfo load() {
        //tags to link areas - this will be commented out as they are not needed at this time
        FileLoader.jsonSave.addClassTag("Forest", ForestGameArea.class);
        FileLoader.jsonSave.addClassTag("Elevator", ElevatorGameArea.class);
        FileLoader.jsonSave.addClassTag("Office", OfficeGameArea.class);
        FileLoader.jsonSave.addClassTag("Mainhall", MainHall.class);
        FileLoader.jsonSave.addClassTag("Reception", Reception.class);
        FileLoader.jsonSave.addClassTag("Tunnel", TunnelGameArea.class);
        String filePath = "saves" + File.separator + "slides.json";

        PlayerInfo loadStats =
                FileLoader.readPlayer(PlayerInfo.class, filePath,
                        FileLoader.Location.LOCAL);

        return loadStats;
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
//
//    public static class inventoryinfo {
//        public ArrayList ll;
//    }
}