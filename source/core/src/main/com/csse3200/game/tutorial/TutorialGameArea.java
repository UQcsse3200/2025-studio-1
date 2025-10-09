package com.csse3200.game.tutorial;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
import com.csse3200.game.components.player.PlayerActionValidator;
import com.csse3200.game.entities.AvatarRegistry;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.Benches;
import com.csse3200.game.entities.factories.InteractableStationFactory;
import com.csse3200.game.entities.factories.KeycardFactory;
import com.csse3200.game.entities.factories.characters.FriendlyNPCFactory;
import com.csse3200.game.entities.factories.characters.NPCFactory;
import com.csse3200.game.entities.factories.system.ObstacleFactory;
import com.csse3200.game.entities.factories.system.TeleporterFactory;
import com.csse3200.game.files.FileLoader;
import com.csse3200.game.files.GameLevelBootstrap;
import com.csse3200.game.files.RegistryEntityPlacer;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Minimal data-driven tutorial room.
 * Terrain/walls & player spawn are code-driven;
 * benches/props/teleporter/keycard/enemies come from JSON.
 */
public class TutorialGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(TutorialGameArea.class);
    private static final float WALL_THICKNESS = 0.1f;
    private static GridPoint2 playerSpawn = new GridPoint2(10, 10);

    private Entity player;

    public TutorialGameArea(TerrainFactory terrainFactory, CameraComponent cameraComponent) {
        super(terrainFactory, cameraComponent);
    }

    public static void setRoomSpawn(GridPoint2 newSpawn) {
        if (newSpawn != null) playerSpawn = newSpawn;
    }

    private static void ensureDefaultAvatarSelected() {
        if (AvatarRegistry.get() == null) {

            var all = AvatarRegistry.getAll();
            if (!all.isEmpty()) AvatarRegistry.set(all.getFirst());
        }
    }


    @Override
    public void create() {
        this.baseScaling = 1f;

        ServiceLocator.registerGameArea(this);
        loadAssets();
        displayUI();
        spawnTerrain();

        player = spawnPlayer();
        ServiceLocator.registerPlayer(player);

        spawnFloor();

        // sanity logs (helps if still invisible)
        var av = AvatarRegistry.get();
        if (av != null) {
            logger.info("Avatar selected: id={} atlas={} texture={}", av.id(), av.atlas(), av.texturePath());
        }
        logger.info("Player world pos={}, camera pos={}",
                player.getPosition(),
                cameraComponent != null ? cameraComponent.getEntity().getPosition() : "null");

        loadTutorialFromJson();
    }


    private void centerCameraOnPlayer() {
        if (cameraComponent != null && player != null) {
            cameraComponent.getEntity().setPosition(player.getPosition());
        }
    }

    private void raisePlayerZ() {
        var arc = player.getComponent(AnimationRenderComponent.class);
        if (arc != null) {
            arc.setZIndex(10f); // above walls/background
        }
    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Tutorial"))
                .addComponent(new com.csse3200.game.components.gamearea.FloorLabelDisplay("Intro"));
        spawnEntity(ui);
    }

    /**
     * Ground + simple world bounds (no door logic here to keep it clean)
     */
    private void spawnTerrain() {
        terrain = terrainFactory.createTerrain(TerrainType.SPAWN_ROOM);
        spawnEntity(new Entity().addComponent(terrain));

        if (cameraComponent != null) {
            var cam = (OrthographicCamera) cameraComponent.getCamera();
            Vector2 camPos = cameraComponent.getEntity().getPosition();
            float vw = cam.viewportWidth;
            float vh = cam.viewportHeight;
            float leftX = camPos.x - vw / 2f;
            float rightX = camPos.x + vw / 2f;
            float bottomY = camPos.y - vh / 2f;
            float topY = camPos.y + vh / 2f;

            // Four thin walls
            Entity left = ObstacleFactory.createWall(WALL_THICKNESS, vh);
            left.setPosition(leftX, bottomY);
            spawnEntity(left);

            Entity right = ObstacleFactory.createWall(WALL_THICKNESS, vh);
            right.setPosition(rightX - WALL_THICKNESS, bottomY);
            spawnEntity(right);

            Entity top = ObstacleFactory.createWall(vw, WALL_THICKNESS);
            top.setPosition(leftX, topY - WALL_THICKNESS);
            spawnEntity(top);

            Entity bottom = ObstacleFactory.createWall(vw, WALL_THICKNESS);
            bottom.setPosition(leftX, bottomY);
            spawnEntity(bottom);
        }
    }

    /**
     * Spawns/positions the player using the shared GameArea helper.
     */
    private Entity spawnPlayer() {
        ensureDefaultAvatarSelected();
        Entity p = spawnOrRepositionPlayer(playerSpawn);

        // attach validator ONLY in Tutorial
        if (p.getComponent(PlayerActionValidator.class) == null) {
            p.addComponent(new PlayerActionValidator());
        }
        return p;
    }

    /**
     * JSON bootstrap: loads core/assets/levels/tutorial/*.json and places entities.
     */
    private void loadTutorialFromJson() {
        AssetManager assets = new AssetManager();
        RegistryEntityPlacer placer = buildTutorialPlacer();


        GameLevelBootstrap.loadDirectoryAndPlace(
                assets,
                FileLoader.Location.INTERNAL,
                "levels/tutorial",
                "*.json",
                placer,
                true  // load JSON-listed textures synchronously
        );
    }

    /**
     * Map type strings from JSON to your factories.
     */
    private RegistryEntityPlacer buildTutorialPlacer() {
        return new RegistryEntityPlacer((name, type, grid) -> {
            if (type == null) return;
            String t = type.trim().toLowerCase(Locale.ROOT);

            if (t.startsWith("enemy:")) {
                spawnEnemyVariant(t.substring("enemy:".length()), grid);
                return;
            }
            if (t.startsWith("bench:")) {
                switch (t) {
                    case "bench:computer" ->
                            spawnEntityAt(InteractableStationFactory.createStation(Benches.COMPUTER_BENCH), grid, true, true);
                    case "bench:health" ->
                            spawnEntityAt(InteractableStationFactory.createStation(Benches.HEALTH_BENCH), grid, true, true);
                    case "bench:speed" ->
                            spawnEntityAt(InteractableStationFactory.createStation(Benches.SPEED_BENCH), grid, true, true);
                    default -> logger.warn("Unknown bench subtype '{}' for '{}'", t, name);
                }
                return;
            }
            if (t.startsWith("prop:")) {
                switch (t) {
                    case "prop:marbleplatform" ->
                            spawnEntityAt(ObstacleFactory.createMarblePlatform(), grid, false, false);
                    default -> logger.warn("Unknown prop subtype '{}' for '{}'", t, name);
                }
                return;
            }
            if (t.startsWith("item:keycard:")) {
                try {
                    int level = Integer.parseInt(t.substring("item:keycard:".length()));
                    Entity key = KeycardFactory.createKeycard(level);
                    key.setPosition(terrain.tileToWorldPosition(grid));
                    spawnEntity(key);
                } catch (NumberFormatException nfe) {
                    logger.warn("Bad keycard level in type='{}' for '{}'", t, name);
                }
                return;
            }
            if (t.equals("teleporter")) {
                Entity tp = TeleporterFactory.createTeleporter(terrain.tileToWorldPosition(grid));
                spawnEntity(tp);
                return;
            }
            if (t.equals("npc:guide")) {
                spawnGuideNear(grid);
                return;
            }

            logger.warn("No handler for type={} (entity={}, at={})", type, name, grid);
        })
                // optional explicit registrations for hot paths:
                .registerCi("bench:computer", (n, tt, g) -> spawnEntityAt(InteractableStationFactory.createStation(Benches.COMPUTER_BENCH), g, true, true))
                .registerCi("bench:health", (n, tt, g) -> spawnEntityAt(InteractableStationFactory.createStation(Benches.HEALTH_BENCH), g, true, true))
                .registerCi("bench:speed", (n, tt, g) -> spawnEntityAt(InteractableStationFactory.createStation(Benches.SPEED_BENCH), g, true, true));
    }

    /**
     * Spawns the guidance NPC with simple default waypoints around the given tile.
     */
    private void spawnGuideNear(GridPoint2 at) {
        Entity p = ServiceLocator.getPlayer();
        if (p == null) p = player;

        var base = terrain.tileToWorldPosition(at);
        var waypoints = List.of(
                new Vector2(base.x, base.y),
                new Vector2(base.x + 4f, base.y),
                new Vector2(base.x + 4f, base.y + 3f)
        );
        Entity guide = FriendlyNPCFactory.createGuidanceNpc(p, waypoints);
        spawnEntityAt(guide, at, true, true);

        AnimationRenderComponent arc = guide.getComponent(AnimationRenderComponent.class);
        if (arc != null) arc.startAnimation("robot_fire");
    }

    /**
     * Map enemy subtypes from JSON to your NPC factory methods.
     */
    protected void spawnEnemyVariant(String variantLower, GridPoint2 grid) {
        float scale = getBaseDifficultyScale();
        Entity p = ServiceLocator.getPlayer();
        if (p == null) p = (player != null) ? player : spawnOrRepositionPlayer(new GridPoint2(1, 1));

        Entity e = switch (variantLower) {
            case "ghostgpt" -> NPCFactory.createGhostGPT(p, this, scale);
            case "ghostgptred" -> NPCFactory.createGhostGPTRed(p, this, scale);
            case "ghostgptblue" -> NPCFactory.createGhostGPTBlue(p, this, scale);

            case "deepspin" -> NPCFactory.createDeepspin(p, this, scale);
            case "deepspinred" -> NPCFactory.createDeepspinRed(p, this, scale);
            case "deepspinblue" -> NPCFactory.createDeepspinBlue(p, this, scale);

            case "vroomba" -> NPCFactory.createVroomba(p, scale);
            case "vroombared" -> NPCFactory.createVroombaRed(p, scale);
            case "vroombablue" -> NPCFactory.createVroombaBlue(p, scale);

            case "grokdroid" -> NPCFactory.createGrokDroid(p, this, scale);
            case "grokdroidred" -> NPCFactory.createGrokDroidRed(p, this, scale);
            case "grokdroidblue" -> NPCFactory.createGrokDroidBlue(p, this, scale);

            case "turret" -> NPCFactory.createTurret(p, this, scale);

            default -> {
                logger.warn("Unknown enemy subtype '{}' at {}", variantLower, grid);
                yield NPCFactory.createGhostGPT(p, this, scale);
            }
        };

        spawnEntityAt(e, grid, true, true);
    }


    /**
     * Minimal asset prep — now also preloads all player textures/atlases
     * declared in configs/avatars.json so PlayerFactory has them ready.
     */
    private void loadAssets() {
        ResourceService rs = ServiceLocator.getResourceService();

        // Terrain background needed by SPAWN_ROOM
        rs.loadTextures(new String[]{"backgrounds/SpawnResize.png", "foreg_sprites/general/Test.png"});

        // ---- Player textures/atlases from AvatarRegistry (configs/avatars.json) ----
        var avatars = AvatarRegistry.getAll();
        var avatarTex = new ArrayList<String>();
        var avatarAtl = new ArrayList<String>();
        for (var a : avatars) {
            // if Avatar is a record: a.texturePath(), a.atlas()
            // if it's a POJO, use getters instead
            if (a.texturePath() != null && !a.texturePath().isBlank()) {
                avatarTex.add(a.texturePath());
            }
            if (a.atlas() != null && !a.atlas().isBlank()) {
                avatarAtl.add(a.atlas());
            }
        }
        if (!avatarTex.isEmpty()) {
            rs.loadTextures(avatarTex.toArray(new String[0]));
        }
        if (!avatarAtl.isEmpty()) {
            rs.loadTextureAtlases(avatarAtl.toArray(new String[0]));
        }

        // Legacy fallback (keeps compatibility if avatars.json misses a default)
        ensurePlayerAtlas(); // loads images/player.atlas if not already present

        // Block until loaded (simple screen, no loading bar)
        rs.loadAll();
    }


    @Override
    public Entity getPlayer() {
        return player;
    }

    @Override
    public String toString() {
        return "Tutorial";
    }
}
