package com.csse3200.game.services;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.files.FileLoader;
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

    public SaveLoadService() {
    }

    /** Save the current GameArea to local storage (saves/slotX.json). */
    public boolean save(String slot, GameArea gameArea) {

        GameState gs = new GameState();
        gs.areaId = gameArea.toString();

        for (Entity e : gameArea.getEntities()) {
            EntityState es = new EntityState();
            es.prefabKey = e.toString();
            es.position.set(e.getPosition());

            gs.entities.add(es);
        }

        String path = "saves/" + slot + ".json";
        FileLoader.writeClass(gs, path, FileLoader.Location.LOCAL);
        logger.info("Saved {} entities for area {} to {}", gs.entities.size(), gs.areaId,
                Gdx.files.local(path).file().getAbsolutePath());
        return true;
    }

    /** Load a save file from local storage and rebuild the area + entities. */
    public boolean load(String slot) {
        return false;
    }


    /** mock game state to store entities. */
    public static class GameState {
        public String areaId;
        public List<EntityState> entities = new ArrayList<>();
    }

    /** Snapshot of a single entity. */
    public static class EntityState {
        public String prefabKey;
        public Vector2 position = new Vector2();
        // Add component blobs later if/when you introduce a persistable interface.
    }
}