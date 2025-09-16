package com.csse3200.game.services;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.csse3200.game.areas.*;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.ItemPickUpComponent;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.files.FileLoader;
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
    /** Save the current GameArea to local storage (saves/slotX.json). */
    public boolean save(String slot, GameArea gameArea) {


        PlayerInfo gs = new PlayerInfo();
        gs.areaId = gameArea.toString();
        for (Entity e : gameArea.getEntities()) {
            if (e.getComponent(InventoryComponent.class) != null) {
                logger.info("Inventory component found: Player found.");
                CombatStatsComponent stat = e.getComponent(CombatStatsComponent.class);
                InventoryComponent inv = e.getComponent(InventoryComponent.class);
                gs.inventory = new ArrayList<>();
                for (int i = 0; i < inv.getSize(); i++) {
                    if (inv.get(i).getComponent(ItemComponent.class) != null) {
                        gs.inventory.add(inv.getTex(i));
                    }
                }
                gs.Health = stat.getHealth();
                gs.position.set(e.getPosition());
                gs.ProcessNumber = inv.getProcessor();
            }
        }
        //add round number and stage info later when implemented
        gs.RoundNumber = 2;

        path = "saves" + File.separator + slot + ".json";
        FileLoader.writeClass(gs, path, FileLoader.Location.LOCAL);
        return true;
    }

    /** Load a save file from local storage and rebuild the area + entities. */
    public static PlayerInfo load() {
        //tags to link areas
        FileLoader.jsonSave.addClassTag("Forest", ForestGameArea.class);
        FileLoader.jsonSave.addClassTag("Elevator", ElevatorGameArea.class);
        FileLoader.jsonSave.addClassTag("Office", OfficeGameArea.class);
        FileLoader.jsonSave.addClassTag("Floor5", Floor5GameArea.class);
        FileLoader.jsonSave.addClassTag("Floor2", Floor2GameArea.class);
        FileLoader.jsonSave.addClassTag("Tunnel", TunnelGameArea.class);
        String filePath = "saves" + File.separator + "slides.json";

        PlayerInfo loadStats =
                FileLoader.readPlayer(PlayerInfo.class, filePath,
                FileLoader.Location.LOCAL);
        logger.info("area id retrieved");
        return loadStats;
    }

    /** mock game state to store entities. */
    public static class PlayerInfo {
        public String areaId;
        public List<String> inventory;
        public int Health;
        public int ProcessNumber;
        public Vector2 position = new Vector2();
        public int RoundNumber;
    }
}