package com.csse3200.game.tutorial;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.areas.terrain.TerrainFactory.TerrainType;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.gamearea.GameAreaDisplay;
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

import static com.badlogic.gdx.Gdx.app;

public class TutorialGameArea extends GameArea {
    private static final Logger logger = LoggerFactory.getLogger(TutorialGameArea.class);
    private static final float WALL_THICKNESS = 0.1f;
    private static GridPoint2 playerSpawn = new GridPoint2(10, 10);
    private boolean tutorialRunning = false;
    private Entity player;

    // Tutorial input gating
    private InputListener tutorialInputGate;
    private CueTypewriterOverlay activeCueOverlay;   // NEW: expose cue state to the screen

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

    private static int keycodeFor(String raw) {
        if (raw == null) return -1;
        String n = raw.replace('\u2013', '-').replace('\u2014', '-')
                .trim().toUpperCase(java.util.Locale.ROOT);
        n = n.replaceAll("[^A-Z0-9_]", "_").replaceAll("_+", "_");
        n = switch (n) {
            case "TAB" -> "Tab";
            case "SHIFT_LEFT" -> "L-Shift";
            case "SHIFT_RIGHT" -> "R-Shift";
            case "CTRL", "CONTROL" -> "CONTROL_LEFT";
            case "CTRL_LEFT" -> "CONTROL_LEFT";
            case "CTRL_RIGHT" -> "CONTROL_RIGHT";
            case "SHIFT" -> "SHIFT_LEFT";
            case "ALT" -> "ALT_LEFT";
            case "ESC" -> "ESCAPE";
            case "RETURN" -> "ENTER";
            case "SPACE" -> "Space";
            default -> n;
        };
        return Input.Keys.valueOf(n); // -1 if unknown
    }

    /**
     * Install a Stage key gate that allows only ESC, F1, and the current cue’s keys.
     */
    private void installInputGate() {
        if (tutorialInputGate != null) return; // already installed
        Stage stage = ServiceLocator.getRenderService().getStage();
        tutorialInputGate = new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (activeCueOverlay == null) return false; // fail-open until overlay attaches
                // return true = CONSUME (block); false = allow
                return !activeCueOverlay.allowGameplayKey(keycode);
            }

            @Override
            public boolean keyUp(InputEvent event, int keycode) {
                if (activeCueOverlay == null) return false;
                return !activeCueOverlay.allowGameplayKey(keycode);
            }
        };
        // add AFTER overlay so overlay sees keys first
        stage.addListener(tutorialInputGate);
    }

    /**
     * Remove the Stage input gate.
     */
    private void removeInputGate() {
        if (tutorialInputGate != null) {
            Stage stage = ServiceLocator.getRenderService().getStage();
            stage.removeListener(tutorialInputGate);
            tutorialInputGate = null;
        }
        activeCueOverlay = null;
    }

    /**
     * NEW: let the screen ask whether a key (e.g., TAB) is allowed during the tutorial.
     */
    public boolean tutorialAllowsKey(int keycode) {
        // Always allow global controls
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.F1) return true;

        // If the tutorial is not running, everything is allowed
        if (!tutorialRunning) return true;

        // Tutorial running: only the overlay decides; if not attached yet → block
        if (activeCueOverlay == null) return false;

        return activeCueOverlay.allowGameplayKey(keycode);
    }

    private void attachTutorialIntro() {
        Entity hud = new Entity().addComponent(
                new TutorialIntroOverlay(startedByKey -> {
                    var cues = readCuesList("levels/tutorial/tutorial_01.json");
                    if (cues.isEmpty()) return;

                    tutorialRunning = true;                        // NEW: start blocking now

                    CueTypewriterOverlay overlay = new CueTypewriterOverlay(cues);
                    overlay.setOnComplete(() -> {                  // when cues end → unlock everything
                        removeInputGate();
                        tutorialRunning = false;                   // NEW: stop blocking
                    });

                    Entity cueHud = new Entity().addComponent(overlay);
                    ServiceLocator.getEntityService().register(cueHud);

                    // Overlay reference and Stage gate (overlay sees keys first)
                    this.activeCueOverlay = overlay;
                    installInputGate();

                    if (startedByKey >= 0) {
                        app.postRunnable(() -> overlay.injectKey(startedByKey));
                    }
                })
        );
        ServiceLocator.getEntityService().register(hud);
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

        var av = AvatarRegistry.get();
        if (av != null) {
            logger.info("Avatar selected: id={} atlas={} texture={}", av.id(), av.atlas(), av.texturePath());
        }
        logger.info("Player world pos={}, camera pos={}",
                player.getPosition(),
                cameraComponent != null ? cameraComponent.getEntity().getPosition() : "null");

        loadTutorialFromJson();
        attachTutorialIntro();
    }

    private void displayUI() {
        Entity ui = new Entity();
        ui.addComponent(new GameAreaDisplay("Tutorial"));
        spawnEntity(ui);
    }

    private void spawnTerrain() {
        terrain = terrainFactory.createTerrain(TerrainType.SPAWN_ROOM);
        spawnEntity(new Entity().addComponent(terrain));

        if (cameraComponent != null) {
            var cam = (OrthographicCamera) cameraComponent.getCamera();
            Vector2 camPos = cameraComponent.getEntity().getPosition();
            float vw = cam.viewportWidth, vh = cam.viewportHeight;
            float leftX = camPos.x - vw / 2f, rightX = camPos.x + vw / 2f;
            float bottomY = camPos.y - vh / 2f, topY = camPos.y + vh / 2f;

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

    private Entity spawnPlayer() {
        ensureDefaultAvatarSelected();
        return spawnOrRepositionPlayer(playerSpawn);
    }

    private void loadTutorialFromJson() {
        AssetManager assets = new AssetManager();
        RegistryEntityPlacer placer = buildTutorialPlacer();
        GameLevelBootstrap.loadDirectoryAndPlace(
                assets,
                FileLoader.Location.INTERNAL,
                "levels/tutorial",
                "*.json",
                placer,
                true
        );
    }

    private List<CueTypewriterOverlay.Cue> readCuesList(String jsonPath) {
        var list = new ArrayList<CueTypewriterOverlay.Cue>();
        try {
            var fh = com.badlogic.gdx.Gdx.files.internal(jsonPath);
            if (!fh.exists()) return list;
            var root = new com.badlogic.gdx.utils.JsonReader().parse(fh);
            var cues = root.get("cues");
            if (cues == null || !cues.isObject()) return list;

            for (com.badlogic.gdx.utils.JsonValue e = cues.child; e != null; e = e.next) {
                String rawKeyName = e.name();
                if (rawKeyName == null) continue;

                String[] parts = rawKeyName.trim().split("\\s*[-\u2013\u2014]\\s*");
                int[] seq = new int[parts.length];
                boolean bad = false;

                StringBuilder display = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    String rawPart = parts[i];
                    int code = keycodeFor(rawPart);
                    if (code < 0) {
                        logger.warn("Unknown Input.Keys name in cues: '{}'", rawPart);
                        bad = true;
                        break;
                    }
                    logger.debug("cue key '{}' → '{}' ({})",
                            rawPart, com.badlogic.gdx.Input.Keys.toString(code), code);
                    seq[i] = code;
                    if (i > 0) display.append('-');
                    display.append(com.badlogic.gdx.Input.Keys.toString(code).toUpperCase(Locale.ROOT));
                }
                if (bad) continue;

                String prompt, feedback;
                if (e.isString()) {
                    prompt = e.asString();
                    feedback = "Good!";
                } else if (e.isObject()) {
                    prompt = e.getString("cue", e.getString("prompt", "(do action)"));
                    feedback = e.getString("feedback", "Nice!");
                } else continue;

                list.add(new CueTypewriterOverlay.Cue(seq, display.toString(), prompt, feedback));
            }
        } catch (Exception ex) {
            logger.error("Failed to read cues from {}", jsonPath, ex);
        }
        return list;
    }

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
                if (t.equals("prop:marbleplatform")) {
                    spawnEntityAt(ObstacleFactory.createMarblePlatform(), grid, false, false);
                } else logger.warn("Unknown prop subtype '{}' for '{}'", t, name);
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
                .registerCi("bench:computer", (n, tt, g) ->
                        spawnEntityAt(InteractableStationFactory.createStation(Benches.COMPUTER_BENCH), g, true, true))
                .registerCi("bench:health", (n, tt, g) ->
                        spawnEntityAt(InteractableStationFactory.createStation(Benches.HEALTH_BENCH), g, true, true))
                .registerCi("bench:speed", (n, tt, g) ->
                        spawnEntityAt(InteractableStationFactory.createStation(Benches.SPEED_BENCH), g, true, true));
    }

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

    private void loadAssets() {
        ResourceService rs = ServiceLocator.getResourceService();
        rs.loadTextures(new String[]{"backgrounds/SpawnResize.png", "foreg_sprites/general/Test.png"});

        var avatars = AvatarRegistry.getAll();
        var avatarTex = new ArrayList<String>();
        var avatarAtl = new ArrayList<String>();
        for (var a : avatars) {
            if (a.texturePath() != null && !a.texturePath().isBlank()) avatarTex.add(a.texturePath());
            if (a.atlas() != null && !a.atlas().isBlank()) avatarAtl.add(a.atlas());
        }
        if (!avatarTex.isEmpty()) rs.loadTextures(avatarTex.toArray(new String[0]));
        if (!avatarAtl.isEmpty()) rs.loadTextureAtlases(avatarAtl.toArray(new String[0]));

        ensurePlayerAtlas();
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
