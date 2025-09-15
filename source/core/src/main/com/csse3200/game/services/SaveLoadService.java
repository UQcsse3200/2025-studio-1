package com.csse3200.game.services;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Json;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.ItemComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerInventoryDisplay;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.PlayerFactory;
import com.csse3200.game.files.FileLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal, self-contained Save/Load service with no external registries.
 * Usage:
 *   // at boot (MainGameScreen ctor)
 *   ServiceLocator.registerSaveLoadService(new SaveLoadService(ServiceLocator.getTimeSource()));
 *   // register areas you want to be loadable
 *   ServiceLocator.getSaveLoadService().registerArea("ForestGameArea",
 *       () -> new com.csse3200.game.areas.ForestGameArea(terrainFactory, renderer.getCamera()));
 *   // register prefabs (e.g., player)
 *   ServiceLocator.getSaveLoadService().registerPrefab("PLAYER",
 *       com.csse3200.game.entities.factories.characters.PlayerFactory::createPlayer);
 */
public class SaveLoadService {
    private static final Logger logger = LoggerFactory.getLogger(SaveLoadService.class);
    private String path;
    /** Save the current GameArea to local storage (saves/slotX.json). */
    public boolean save(String slot, GameArea gameArea) {

        PlayerInfo gs = new PlayerInfo();
        gs.areaId = gameArea.toString();

        for (Entity e : gameArea.getEntities()) {
            if (e.getComponent(PlayerStatsDisplay.class) != null) {
                logger.info("Inventory component found: Player found.");
                CombatStatsComponent stat = e.getComponent(CombatStatsComponent.class);
                InventoryComponent inv = e.getComponent(InventoryComponent.class);
                gs.inventory = new ArrayList<>();
                for (int i = 0; i < inv.getSize(); i++) {
                    if (inv.get(i).getComponent(ItemComponent.class) != null) {
                        ItemComponent item = inv.get(i).getComponent(ItemComponent.class);
                        //stores information on the item and easy to expand to increase information
                        String info = item.getName() + '\n' +
                                item.getDescription() + '\n' +
                                inv.getTex(i);
                        gs.inventory.add(info);
                    }
                }
                gs.Health = stat.getHealth();
                gs.position.set(e.getPosition());
            }
        }
        //add round number and stage info later when implemented
        gs.RoundNumber = 2;

        path = "saves" + File.separator + slot + ".json";
        FileLoader.writeClass(gs, path, FileLoader.Location.LOCAL);
        return true;
    }

    /** Load a save file from local storage and rebuild the area + entities. */
    public static boolean load() {

        String filePath = "saves" + File.separator + "slides.json";

        PlayerInfo loadStats =
                FileLoader.readClass(PlayerInfo.class, filePath,
                FileLoader.Location.LOCAL);
        Entity loadPlayer = PlayerFactory.createPlayer();
//        loadStats.areaId.
//        loadPlayer.setPosition(loadStats.position);
//        InventoryComponent loadInv = loadPlayer.getComponent(InventoryComponent.class);

        if (loadStats == null) {
            return false;
        }

        logger.info("successfully loaded");
        return true;
    }

    /** mock game state to store entities. */
    public static class PlayerInfo {
        public String areaId;
        public List<String> inventory;
        public int Health;
        public Vector2 position = new Vector2();
        public int RoundNumber;
    }
}