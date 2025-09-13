package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerStatsDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
                    gs.inventory.add(inv.getTex(i));
                }
                gs.Health = stat.getHealth();
                gs.position.set(e.getPosition());
            }
        }
        //add round number and stage info later when implemented
        gs.RoundNumber = 2;

        String path = "saves/" + slot + ".json";
        FileLoader.writeClass(gs, path, FileLoader.Location.LOCAL);
        return true;
    }

    /** Load a save file from local storage and rebuild the area + entities. */
    public boolean load(String slot) {
        return false;
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