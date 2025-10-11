package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.*;
import com.csse3200.game.areas.difficulty.Difficulty;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.maingame.MainGameDisplay;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.screens.Minimap;
import com.csse3200.game.components.screens.MinimapDisplay;
import com.csse3200.game.components.screens.PauseEscInputComponent;
import com.csse3200.game.components.screens.PauseMenuDisplay;
import com.csse3200.game.components.teleporter.TeleporterComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.system.RenderFactory;
import com.csse3200.game.files.SaveGame;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.*;
import com.csse3200.game.session.GameSession;
import com.csse3200.game.session.SessionManager;
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * The main gameplay screen, now built on {@link BaseScreen}.
 * <p>
 * BaseScreen handles:
 * - Service registration
 * - Renderer & camera
 * - Background
 * - Stage lifecycle
 * <p>
 * We provide:
 * - UI entity via {@link #createUIScreen(Stage)}
 * - Game area creation (new or load)
 * - Round/leaderboard plumbing
 * - Pause/minimap overlays
 * - Countdown and death handling
 */
public class MainGameScreen extends BaseScreen {
    private static final Logger logger = LoggerFactory.getLogger(MainGameScreen.class);

    private static final String[] mainGameTextures = {"images/heart.png"};
    private static final Vector2 CAMERA_POSITION = new Vector2(7.5f, 7.5f);

    private final boolean loadSaveGame;

    private Renderer renderer;
    private PhysicsEngine physicsEngine;
    private GameArea gameArea;

    private CountdownTimerService countdownTimer;

    // Session/leaderboard
    private GameSession session;
    private float roundTime = 0f;

    // Overlays
    private Entity pauseOverlay;
    private Entity minimap;
    private boolean isPauseVisible = false;
    private boolean isMinimapVisible = false;
    private boolean pauseToggledThisFrame = false;

    public MainGameScreen(GdxGame game, boolean loadSaveGame) {
        // Choose whatever background suits your main game; can be a solid image or transparent PNG.
        super(game, "images/main_background.png");
        this.loadSaveGame = loadSaveGame;
    }

    /**
     * BaseScreen calls this after services/stage/renderer are ready.
     */
    @Override
    protected Entity createUIScreen(Stage stage) {
        logger.debug("Initialising main game screen services & entities");

        // Pull services that BaseScreen already registered (and add any you need)
        // Time source
        if (ServiceLocator.getTimeSource() == null) {
            ServiceLocator.registerTimeSource(new GameTime());
        }

        // Physics
        PhysicsService physicsService = ServiceLocator.getPhysicsService();
        if (physicsService == null) {
            physicsService = new PhysicsService();
            ServiceLocator.registerPhysicsService(physicsService);
        }
        physicsEngine = physicsService.getPhysics();

        // Input / Resources / Render / Entities
        if (ServiceLocator.getInputService() == null) {
            ServiceLocator.registerInputService(new InputService());
        }
        if (ServiceLocator.getResourceService() == null) {
            ServiceLocator.registerResourceService(new ResourceService());
        }
        if (ServiceLocator.getEntityService() == null) {
            ServiceLocator.registerEntityService(new EntityService());
        }
        if (ServiceLocator.getRenderService() == null) {
            ServiceLocator.registerRenderService(new RenderService());
        }

        // Some systems you referenced
        if (ServiceLocator.getSaveLoadService() == null) {
            ServiceLocator.registerSaveLoadService(new SaveLoadService());
        }
        if (ServiceLocator.getDiscoveryService() == null) {
            ServiceLocator.registerDiscoveryService(new DiscoveryService());
        }

        // Renderer (use your standard factory so camera entity exists)
        renderer = RenderFactory.createRenderer();
        renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);
        renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

        // Session/leaderboard init
        SessionManager sessionManager = new SessionManager();
        session = sessionManager.startNewSession();

        var prev = game.getCarryOverLeaderBoard();
        if (prev != null && !prev.getLeaderBoard().isEmpty()) {
            session.getLeaderBoardManager().setLeaderboard(new ArrayList<>(prev.getLeaderBoard()));
        }
        ServiceLocator.registerLeaderBoardManager(session.getLeaderBoardManager());

        // Assets
        loadAssets();

        // Countdown: 4 minutes (240000 ms)
        countdownTimer = new CountdownTimerService(ServiceLocator.getTimeSource(), 240000);

        // UI root
        InputComponent terminalInput = ServiceLocator.getInputService().getInputFactory().createForTerminal();
        Entity ui = new Entity()
                .addComponent(new InputDecorator(stage, 10))
                .addComponent(new PerformanceDisplay())
                .addComponent(new MainGameActions(this.game))
                .addComponent(new MainGameDisplay(countdownTimer))
                .addComponent(new Terminal(null, this.game, countdownTimer))
                .addComponent(terminalInput)
                .addComponent(new TerminalDisplay(this.game))
                // Frame controller component (moves your old render() logic here)
                .addComponent(new MainGameController());

        ServiceLocator.getEntityService().register(ui);

        // Global event: end of round records leaderboard and carries over
        ServiceLocator.getGlobalEvents().addListener("round:finished", (Boolean won) -> {
            recordRoundForLeaderboard(won);
            game.setCarryOverLeaderBoard(session.getLeaderBoardManager());
        });

        // Game area
        TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());
        ServiceLocator.clearPlayer(); // keep consistent with your original

        Set<String> exploredAreas = new HashSet<>();
        if (loadSaveGame) {
            logger.info("Loading game from save file");
            SaveGame.GameState load = SaveLoadService.load();
            gameArea = new ForestGameArea(terrainFactory, renderer.getCamera());
            ServiceLocator.registerGameArea(gameArea);
            ForestGameArea.setRoomSpawn(new GridPoint2(3, 20));
            gameArea.create();

            switch (load.getGameArea()) {
                case "Forest" -> gameArea = ForestGameArea.load(terrainFactory, renderer.getCamera());
                case "Elevator" -> gameArea = ElevatorGameArea.load(terrainFactory, renderer.getCamera());
                case "Office" -> gameArea = OfficeGameArea.load(terrainFactory, renderer.getCamera());
                case "Mainhall" -> gameArea.clearAndLoad(() -> MainHall.load(terrainFactory, renderer.getCamera()));
                case "Reception" -> gameArea.clearAndLoad(() -> Reception.load(terrainFactory, renderer.getCamera()));
                case "Tunnel" -> gameArea = TunnelGameArea.load(terrainFactory, renderer.getCamera());
                case "Security" -> gameArea = SecurityGameArea.load(terrainFactory, renderer.getCamera());
                case "Storage" -> gameArea = StorageGameArea.load(terrainFactory, renderer.getCamera());
                case "Shipping" -> gameArea = ShippingGameArea.load(terrainFactory, renderer.getCamera());
                case "Server" -> gameArea = ServerGameArea.load(terrainFactory, renderer.getCamera());
                default -> gameArea = null;
            }

            if (gameArea != null) {
                ServiceLocator.registerGameArea(gameArea);
                ServiceLocator.registerDifficulty(new Difficulty(load.getDifficulty()));
                SaveLoadService.loadPlayer(
                        load.getPlayer(),
                        load.getArmour(),
                        load.getInventory()
                );
                exploredAreas = load.getAreasVisited();
            } else {
                logger.error("Couldn't create GameArea from save file");
            }
        } else {
            gameArea = new ForestGameArea(terrainFactory, renderer.getCamera());
            ServiceLocator.registerGameArea(gameArea);
            ForestGameArea.setRoomSpawn(new GridPoint2(3, 20));
            gameArea.create();
            exploredAreas.add(gameArea.toString());
        }

        discover(exploredAreas);

        return ui;
    }

    @Override
    protected void onDispose() {
        logger.debug("Disposing MainGameScreen");

        if (renderer != null) {
            renderer.dispose();
        }
        unloadAssets();

        // Preserve player entity during disposal
        Entity player = ServiceLocator.getPlayer();
        ServiceLocator.getEntityService().disposeExceptPlayer();
        if (ServiceLocator.getRenderService() != null) {
            ServiceLocator.getRenderService().dispose();
        }
        if (ServiceLocator.getResourceService() != null) {
            ServiceLocator.getResourceService().dispose();
        }
        ServiceLocator.clearExceptPlayer();
    }

    /* ------------------------------ Helpers ------------------------------ */

    /**
     * Mark loaded/existing areas as discovered.
     */
    private void discover(Set<String> areas) {
        DiscoveryService ds = ServiceLocator.getDiscoveryService();
        if (ds != null) {
            for (String a : areas) {
                ds.discover(a);
            }
        }
    }

    private void loadAssets() {
        logger.debug("Loading assets");
        ResourceService rs = ServiceLocator.getResourceService();
        rs.loadTextures(mainGameTextures);
        rs.loadAll();
    }

    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService rs = ServiceLocator.getResourceService();
        rs.unloadAssets(mainGameTextures);
    }

    /**
     * Remaining time in seconds, clamped to >= 0.
     */
    private long getRemainingSeconds() {
        long rem = countdownTimer.getRemainingMs();
        return rem > 0 ? rem / 1000 : 0;
    }

    private long getCompleteTime() {
        return (countdownTimer.getDuration() - countdownTimer.getRemainingMs()) / 1000;
    }

    private void recordRoundForLeaderboard(boolean won) {
        if (session == null) return;

        int processors = 0;
        Entity player = (gameArea != null) ? gameArea.getPlayer() : null;
        if (player != null) {
            InventoryComponent inv = player.getComponent(InventoryComponent.class);
            if (inv != null) {
                processors = inv.getProcessor();
            }
        }

        float timePlayedSeconds = won ? (float) getRemainingSeconds() : 0f;
        session.getLeaderBoardManager().addRound(processors, timePlayedSeconds);
        session.getLeaderBoardManager().getLeaderBoard().forEach(entry -> logger.info(entry.toString()));
    }

    private void saveState() {
        logger.info("Saving state");
        if (ServiceLocator.getSaveLoadService().save("slides", gameArea)) {
            logger.info("Saving data successful");
        } else {
            logger.info("Save data failed");
        }
    }

    private void setDeathScreen() {
        ServiceLocator.getGlobalEvents().trigger("round:finished", false);
        game.setCarryOverLeaderBoard(session.getLeaderBoardManager());
        DeathScreen deathScreen = new DeathScreen(game);
        deathScreen.updateTime(getCompleteTime());
        game.setScreen(deathScreen);
    }

    /* ----------------------- Overlay show/hide ----------------------- */

    private void showPauseOverlay() {
        logger.info("Showing pause overlay");
        Stage stage = ServiceLocator.getRenderService().getStage();
        pauseOverlay = new Entity()
                .addComponent(new PauseMenuDisplay(game))
                .addComponent(new InputDecorator(stage, 100))
                .addComponent(new PauseEscInputComponent(110));
        pauseOverlay.getEvents().addListener("save", this::saveState);
        pauseOverlay.getEvents().addListener("resume", this::hidePauseOverlay);
        ServiceLocator.getEntityService().register(pauseOverlay);
        ServiceLocator.getTimeSource().setPaused(true);
        if (!countdownTimer.isPaused()) countdownTimer.pause();
        isPauseVisible = true;
        pauseToggledThisFrame = true;
    }

    private void hidePauseOverlay() {
        if (pauseOverlay != null) {
            pauseOverlay.dispose();
            ServiceLocator.getEntityService().unregister(pauseOverlay);
            if (!isMinimapVisible) {
                ServiceLocator.getTimeSource().setPaused(false);
            }
            pauseOverlay = null;
        }
        if (!isMinimapVisible) {
            ServiceLocator.getTimeSource().setPaused(false);
            if (countdownTimer.isPaused()) countdownTimer.resume();
        }
        isPauseVisible = false;
        pauseToggledThisFrame = true;
    }

    private void showMinimapOverlay() {
        logger.info("Showing minimap overlay");
        Stage stage = ServiceLocator.getRenderService().getStage();
        minimap = new Entity()
                .addComponent(new MinimapDisplay(game,
                        new Minimap(Gdx.graphics.getHeight(), Gdx.graphics.getWidth(),
                                "configs/room_layout.txt")))
                .addComponent(new InputDecorator(stage, 100));
        minimap.getEvents().addListener("resume", this::hideMinimapOverlay);
        ServiceLocator.getEntityService().register(minimap);
        ServiceLocator.getTimeSource().setPaused(true);
        isMinimapVisible = true;
    }

    private void hideMinimapOverlay() {
        logger.info("Hiding minimap overlay");
        if (minimap != null) {
            minimap.dispose();
            ServiceLocator.getEntityService().unregister(minimap);
            if (!isPauseVisible) {
                ServiceLocator.getTimeSource().setPaused(false);
            }
            minimap = null;
        }
        isMinimapVisible = false;
    }

    /* ===================== Per-frame controller ===================== */

    /**
     * Moves the old render() loop behaviour into an ECS component that
     * BaseScreen will naturally tick each frame when EntityService updates.
     */
    private final class MainGameController extends Component {
        @Override
        public void update() {
            float delta = ServiceLocator.getTimeSource().getDeltaTime();
            roundTime += delta;

            // Reset per-frame ESC consumption flags
            TeleporterComponent.resetEscConsumed();

            // Physics & entities: only if not paused/transitioning
            if (!isPauseVisible && !ServiceLocator.getTimeSource().isPaused() && !ServiceLocator.isTransitioning()) {
                physicsEngine.update();
            }
            if (!ServiceLocator.isTransitioning()) {
                ServiceLocator.getEntityService().update();
            }

            // Death screen when player dies
            Entity player = (gameArea != null) ? gameArea.getPlayer() : null;
            if (player != null) {
                CombatStatsComponent stats = player.getComponent(CombatStatsComponent.class);
                if (stats != null && stats.isDead()) {
                    setDeathScreen();
                    return;
                }
            }

            // Capture overlay visibility before Stage input advances
            boolean preIsMinimapVisible = isMinimapVisible;
            boolean preIsPauseVisible = isPauseVisible;

            // ESC priority: Teleporter -> Minimap -> Pause
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                if (PauseMenuDisplay.wasEscConsumedThisFrame() || TeleporterComponent.wasEscConsumedThisFrame() || pauseToggledThisFrame) {
                    // Suppressed: another overlay consumed ESC or toggled already this frame
                } else if (preIsMinimapVisible) {
                    hideMinimapOverlay();
                    if (!isPauseVisible) countdownTimer.resume();
                } else if (preIsPauseVisible) {
                    hidePauseOverlay();
                    if (!isMinimapVisible) countdownTimer.resume();
                } else {
                    showPauseOverlay();
                    countdownTimer.pause();
                }
            }

            // Minimap toggle (TAB)
            if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                if (!isMinimapVisible) {
                    if (!isPauseVisible) {
                        showMinimapOverlay();
                        countdownTimer.pause();
                    }
                } else {
                    hideMinimapOverlay();
                    if (!isPauseVisible) countdownTimer.resume();
                }
            }

            // Minimap zoom
            if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS) && isMinimapVisible) {
                minimap.getComponent(MinimapDisplay.class).zoomIn();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS) && isMinimapVisible) {
                minimap.getComponent(MinimapDisplay.class).zoomOut();
            }

            // Countdown expiry -> death
            if (countdownTimer.isTimeUP()) {
                setDeathScreen();
                return;
            }

            // Reset per-frame guards for next tick
            PauseMenuDisplay.resetEscConsumed();
            pauseToggledThisFrame = false;
        }
    }
}
