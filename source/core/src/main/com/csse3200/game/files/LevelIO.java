package com.csse3200.game.files;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Loads the level JSON schema:
 * {
 * "textures": { "<group>Textures": { id -> path, ... }, ... },
 * "entities": [ { "name": "...", "type": "...", "location": { "x": .., "y": .. } }, ... ]
 * }
 */
public final class LevelIO {
    private static final Logger log = LoggerFactory.getLogger(LevelIO.class);

    private LevelIO() {
    }

    /**
     * Load one level file (both textures + entities) from a given location.
     */
    public static Optional<LevelData> load(String filename, FileLoader.Location location) {
        final var texOpt = FileLoader.readTextureMap(filename, location);
        final var entOpt = FileLoader.readMapEntities(filename, location);

        if (texOpt.isEmpty() && entOpt.isEmpty()) {
            log.atWarn()
                    .setMessage("No textures or entities found in {} ({})")
                    .addArgument(filename).addArgument(location).log();
            return Optional.empty();
        }
        final var textures = new LinkedHashMap<>(texOpt.orElseGet(Map::of));
        final var entities = new ArrayList<>(entOpt.orElseGet(List::of));
        return Optional.of(new LevelData(textures, entities));
    }

    /**
     * Queue all textures referenced by the level file into the AssetManager.
     */
    public static void enqueueTextures(AssetManager assets, LevelData data) {
        Objects.requireNonNull(assets, "assets");
        data.textures().values().forEach(path -> {
            if (!assets.isLoaded(path, Texture.class)) {
                assets.load(path, Texture.class);
            }
        });
    }

    /**
     * Convenience: enqueue + block until ready.
     */
    public static void loadTexturesBlocking(AssetManager assets, LevelData data) {
        enqueueTextures(assets, data);
        assets.finishLoading();
    }

    /**
     * In-memory representation of a level file.
     */
    public record LevelData(
            Map<String, String> textures,
            List<FileLoader.MapEntitySpec> entities
    ) {
    }
}
