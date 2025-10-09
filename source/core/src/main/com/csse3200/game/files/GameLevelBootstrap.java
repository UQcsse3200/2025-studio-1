package com.csse3200.game.files;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.GridPoint2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.characters.NPCFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * One-call bootstrap: find levels by glob, enqueue/load assets, and place entities.
 * Wire your factories into the placer via register(...).
 */
public final class GameLevelBootstrap {
    private static final Logger log = LoggerFactory.getLogger(GameLevelBootstrap.class);

    private GameLevelBootstrap() {
    }

    public static List<LevelIO.LevelData> loadDirectoryAndPlace(
            AssetManager assets,
            FileLoader.Location location,
            String directory,
            String glob,
            RegistryEntityPlacer placer,
            boolean syncTextureLoad
    ) {
        var loader = new LevelPatternLoader(assets, location, placer::place, syncTextureLoad);
        var levels = loader.loadAll(directory, glob);

        log.atInfo().setMessage("Loaded {} level file(s) from {}/{}")
                .addArgument(levels::size).addArgument(directory).addArgument(glob).log();

        // Collect referenced asset paths from JSON (deduped)
        final Set<String> audioPaths = new LinkedHashSet<>();
        final Set<String> texturePaths = new LinkedHashSet<>();

        levels.forEach(ld -> ld.textures().forEach((id, path) -> {
            if (isAudio(path)) {
                audioPaths.add(path);
            } else {
                texturePaths.add(path);
            }
        }));

        // Sanity log for textures that were queued by LevelPatternLoader but not yet loaded
        texturePaths.forEach(path -> {
            if (!assets.isLoaded(path, Texture.class)) {
                log.atDebug().setMessage("Texture queued but not yet loaded: {}").addArgument(path).log();
            }
        });

        // Enqueue audio (as both Music and Sound so callers can use either API)
        for (String path : audioPaths) {
            if (!assets.isLoaded(path, Music.class)) {
                assets.load(path, Music.class);
            }
            if (!assets.isLoaded(path, Sound.class)) {
                assets.load(path, Sound.class);
            }
        }

        if (syncTextureLoad && (!audioPaths.isEmpty())) {
            assets.finishLoading(); // also waits for audio just enqueued
        }

        if (!audioPaths.isEmpty()) {
            log.atInfo().setMessage("Audio enqueued from levels: {} file(s)").addArgument(audioPaths::size).log();
        }

        return levels;
    }

    private static boolean isAudio(String path) {
        if (path == null) return false;
        String p = path.toLowerCase(Locale.ROOT).trim();
        return p.endsWith(".mp3") || p.endsWith(".ogg") || p.endsWith(".wav");
    }

    /**
     * Example wiring — replace handlers with your actual factories.
     */
    public static RegistryEntityPlacer defaultPlacer(GameArea area) {
        // Fallback that pattern-matches "enemy:<variant>"
        RegistryEntityPlacer placer = new RegistryEntityPlacer((name, type, grid) -> {
            if (type == null) return;
            String t = type.trim().toLowerCase(Locale.ROOT);

            switch (t) {
                case "player" -> {
                    area.spawnOrRepositionPlayer(grid);
                    return;
                }
                case "door" -> {
                    Entity d = ObstacleFactory.createDoor();
                    area.spawnEntityAt(d, grid, false, false);
                    return;
                }
            }

            if (t.startsWith("enemy")) {
                String variant = parseVariant(t); // may be ""
                spawnEnemy(area, variant, grid, name);
                return;
            }

            // Unknown type
            log.atWarn().setMessage("No handler for type={} (entity={}, at={})")
                    .addArgument(type).addArgument(name).addArgument(grid).log();
        });

        // Fast exact handlers (avoid fallback for common types)
        placer.registerCi("player", (name, type, grid) -> area.spawnOrRepositionPlayer(grid));
        placer.registerCi("door", (name, type, grid) -> {
            Entity d = ObstacleFactory.createDoor();
            area.spawnEntityAt(d, grid, false, false);
        });

        // Convenience: allow "enemy" (defaults to ghostgpt)
        placer.registerCi("enemy", (name, type, grid) -> spawnEnemy(area, "ghostgpt", grid, name));

        return placer;
    }

    private static String parseVariant(String typeLower) {
        int i = typeLower.indexOf(':');
        return (i >= 0 && i + 1 < typeLower.length()) ? typeLower.substring(i + 1) : "";
    }

    private static void spawnEnemy(GameArea area, String variantLower, GridPoint2 grid, String name) {
        float scale = area.getBaseDifficultyScale();

        // Ensure there is a player (some NPC factories require it)
        Entity player = ServiceLocator.getPlayer();
        if (player == null) {
            player = area.spawnOrRepositionPlayer(new GridPoint2(10, 10)); // safe default
        }

        Entity e = switch (variantLower) {
            case "ghostgpt" -> NPCFactory.createGhostGPT(player, area, scale);
            case "ghostgptred" -> NPCFactory.createGhostGPTRed(player, area, scale);
            case "ghostgptblue" -> NPCFactory.createGhostGPTBlue(player, area, scale);

            case "deepspin" -> NPCFactory.createDeepspin(player, area, scale);
            case "deepspinred" -> NPCFactory.createDeepspinRed(player, area, scale);
            case "deepspinblue" -> NPCFactory.createDeepspinBlue(player, area, scale);

            case "vroomba" -> NPCFactory.createVroomba(player, scale);
            case "vroombared" -> NPCFactory.createVroombaRed(player, scale);
            case "vroombablue" -> NPCFactory.createVroombaBlue(player, scale);

            case "grokdroid" -> NPCFactory.createGrokDroid(player, area, scale);
            case "grokdroidred" -> NPCFactory.createGrokDroidRed(player, area, scale);
            case "grokdroidblue" -> NPCFactory.createGrokDroidBlue(player, area, scale);

            case "turret" -> NPCFactory.createTurret(player, area, scale);

            case "" -> NPCFactory.createGhostGPT(player, area, scale); // enemy (no subtype)
            default -> {
                log.atWarn().setMessage("Unknown enemy subtype='{}' for entity='{}' at {}. Spawning GhostGPT.")
                        .addArgument(variantLower).addArgument(name).addArgument(grid).log();
                yield NPCFactory.createGhostGPT(player, area, scale);
            }
        };

        area.spawnEntityAt(e, grid, true, true); // center on tile by default
    }
}
