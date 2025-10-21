package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.*;
import com.csse3200.game.areas.difficulty.Difficulty;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CombatStatsComponent;
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
import com.csse3200.game.lighting.LightingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * The game screen containing the main game.
 *
 * <p>Details on libGDX screens: https://happycoding.io/tutorials/libgdx/game-screens
 */
public class MainGameScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MainGameScreen.class);
    private static final String[] mainGameTextures = {"images/heart.png"};
    private static final Vector2 CAMERA_POSITION = new Vector2(7.5f, 7.5f);

    private final GdxGame game;
    private final Renderer renderer;
    private final PhysicsEngine physicsEngine;
    private GameArea gameArea;

    private CountdownTimerService countdownTimer;


    //Leaderboard & Session fields
    private GameSession session;
    private float roundTime = 0f;

    private Entity pauseOverlay;
    private Entity minimap;
    private boolean isPauseVisible = false;
    private boolean isMinimapVisible = false;
    private boolean pauseToggledThisFrame = false; // guard to avoid reopen on same ESC

    public MainGameScreen(GdxGame game, boolean loadSaveGame) {
        this.game = game;

        logger.debug("Initialising main game screen services");

        //Initialize session for this playthrough
        SessionManager sessionManager = new SessionManager();
        session = sessionManager.startNewSession();

        var prev = game.getCarryOverLeaderBoard();
        if (prev != null && !prev.getLeaderBoard().isEmpty()) {
            session.getLeaderBoardManager()
                    .setLeaderboard(new ArrayList<>(prev.getLeaderBoard()));
        }
        ServiceLocator.registerLeaderBoardManager(session.getLeaderBoardManager());

        ServiceLocator.registerTimeSource(new GameTime());

        PhysicsService physicsService = new PhysicsService();
        ServiceLocator.registerPhysicsService(physicsService);
        physicsEngine = physicsService.getPhysics();

        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerSaveLoadService(new SaveLoadService());
        ServiceLocator.registerDiscoveryService(new DiscoveryService()); // NEW: track discovered rooms


        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());

        ServiceLocator.clearPlayer();
        this.unclearAllRooms();

        renderer = RenderFactory.createRenderer();
        renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);
        renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

        LightingService lightingService =
                new LightingService(renderer.getCamera(), physicsEngine.getWorld());
        ServiceLocator.registerLightingService(lightingService);

        loadAssets();
        countdownTimer = new CountdownTimerService(ServiceLocator.getTimeSource(), 240000);
        createUI();

        logger.debug("Initialising main game screen entities");
        TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());
        ServiceLocator.getGlobalEvents().addListener("round:finished", (Boolean won) -> {
            recordRoundForLeaderboard(won);
            game.setCarryOverLeaderBoard(session.getLeaderBoardManager());
        });

        Set<String> exploredAreas = new HashSet<>();

        if (loadSaveGame) {
            logger.info("loading game from save file");
            SaveGame.GameState load = ServiceLocator.getSaveLoadService().load("saves" + File.separator + "slides.json");
            gameArea = new ForestGameArea(terrainFactory, renderer.getCamera());
            ServiceLocator.registerGameArea(gameArea);
            ForestGameArea.setRoomSpawn(new GridPoint2(3, 20));
            gameArea.create();
            //all areas in the game for loading
            switch (load.getGameArea()) {
                case "Forest" -> gameArea = ForestGameArea.load(terrainFactory, renderer.getCamera());
                case "Elevator" ->
                        gameArea.clearAndLoad(() -> ElevatorGameArea.load(terrainFactory, renderer.getCamera()));
                case "Office" -> gameArea.clearAndLoad(() -> OfficeGameArea.load(terrainFactory, renderer.getCamera()));
                case "Mainhall" -> gameArea.clearAndLoad(() -> MainHall.load(terrainFactory, renderer.getCamera()));
                case "Reception" -> gameArea.clearAndLoad(() -> Reception.load(terrainFactory, renderer.getCamera()));
                case "Tunnel" -> gameArea.clearAndLoad(() -> TunnelGameArea.load(terrainFactory, renderer.getCamera()));
                case "Security" -> gameArea.clearAndLoad(() -> SecurityGameArea.load(terrainFactory, renderer.getCamera()));
                case "Storage" -> gameArea.clearAndLoad(() -> StorageGameArea.load(terrainFactory, renderer.getCamera()));
                case "Shipping" -> gameArea.clearAndLoad(() -> ShippingGameArea.load(terrainFactory, renderer.getCamera()));
                case "Server" -> gameArea.clearAndLoad(() -> ServerGameArea.load(terrainFactory, renderer.getCamera()));
                case "Casino" -> gameArea.clearAndLoad(() -> CasinoGameArea.load(terrainFactory, renderer.getCamera()));
                case "Research" ->
                        gameArea.clearAndLoad(() -> ResearchGameArea.load(terrainFactory, renderer.getCamera()));
                default -> gameArea = null;
            }

            clearUpTo(load.getGameArea());

            //will instantiate all items
            if (gameArea != null) {
                ServiceLocator.registerGameArea(gameArea);
                ServiceLocator.registerDifficulty(new Difficulty(load.getDifficulty()));
                SaveLoadService.loadPlayer(
                        load.getPlayer(),
                        load.getArmour(),
                        load.getInventory()
                );
                gameArea.setWave(load.getWave());
                exploredAreas = load.getAreasVisited();

            } else {
                logger.error("couldn't create Game area from file");
            }

        } else {

            gameArea = new ForestGameArea(terrainFactory, renderer.getCamera());
            ServiceLocator.registerGameArea(gameArea);
            ForestGameArea.setRoomSpawn(new GridPoint2(3, 20));
            gameArea.create();
            exploredAreas.add(gameArea.toString());
        }
        // for when loading a save game
        discover(exploredAreas);
    }

    /**
     * private helper method for setting multiple areas to be discovered on startup
     * such as on loading
     *
     * @param areas Set of areas to be marked discovered
     */
    private void discover(Set<String> areas) {
        DiscoveryService dsInit = ServiceLocator.getDiscoveryService();
        // mark any area of a savefile or initial area as discovered
        if (dsInit != null) {
            for (String areaString : areas) {
                dsInit.discover(areaString);
            }
        }
    }

    @Override
    public void render(float delta) {
        //accumulates elapsed time
        this.roundTime += delta;

        // Reset per-frame ESC consumption flags at start of frame
        TeleporterComponent.resetEscConsumed();
        // PauseMenuDisplay.resetEscConsumed(); // moved to end of render to avoid reopening pause
        if (!isPauseVisible && !(ServiceLocator.getTimeSource().isPaused())
                && !ServiceLocator.isTransitioning()) {
            physicsEngine.update();
        }
        if (!ServiceLocator.isTransitioning()) {
            ServiceLocator.getEntityService().update();
        }
        Entity player = gameArea.getPlayer();
        //show death screen when player is dead
        if (player != null) {
            var playerStat = player.getComponent(CombatStatsComponent.class);
            if (playerStat != null && playerStat.isDead()) {
                setDeathScreen();
            }
        }

        // Capture overlay visibility before Stage processes input (renderer.render likely advances Stage)
        boolean preIsMinimapVisible = isMinimapVisible;
        boolean preIsPauseVisible = isPauseVisible;

        renderer.render();
        // Unified ESC handling priority: Teleporter -> Minimap -> Pause
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (PauseMenuDisplay.wasEscConsumedThisFrame() || TeleporterComponent.wasEscConsumedThisFrame() || pauseToggledThisFrame) {
                // ESC was consumed by a focused overlay (pause or teleporter), or pause toggled earlier this frame; suppress others
            } else if (preIsMinimapVisible) {
                // Close minimap first if it was open at the start of this frame
                hideMinimapOverlay();
                if (!isPauseVisible) {
                    countdownTimer.resume();
                }
            } else if (preIsPauseVisible) {
                // If pause menu was open at the start of this frame, close it
                hidePauseOverlay();
                if (!isMinimapVisible) {
                    countdownTimer.resume();
                }
            } else {
                // Otherwise, open pause menu
                showPauseOverlay();
                countdownTimer.pause();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
            if (!isMinimapVisible) {
                if (!isPauseVisible) {
                    showMinimapOverlay();
                    countdownTimer.pause();
                }
            } else {
                hideMinimapOverlay();
                if (!isPauseVisible) {
                    countdownTimer.resume();
                }
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS) && isMinimapVisible) {
            minimap.getComponent(MinimapDisplay.class).zoomIn();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS) && isMinimapVisible) {
            minimap.getComponent(MinimapDisplay.class).zoomOut();
        }

        //switch to death screen when countdown timer is up
        if (countdownTimer.isTimeUP()) {
            setDeathScreen();
        }

        // Reset per-frame guards/flags for the next frame
        PauseMenuDisplay.resetEscConsumed();
        pauseToggledThisFrame = false;
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
        logger.trace("Resized renderer: ({} x {})", width, height);
    }

    @Override
    public void pause() {
        logger.info("Game paused");
    }

    @Override
    public void resume() {
        logger.info("Game resumed");
    }

    @Override
    public void dispose() {
        logger.debug("Disposing main game screen");

        renderer.dispose();
        unloadAssets();

        // Preserve player entity during disposal
        Entity player = ServiceLocator.getPlayer();
        ServiceLocator.getEntityService().disposeExceptPlayer();

        var ls = ServiceLocator.getLightingService();
        if (ls != null && ls.getEngine() != null) {
            ls.getEngine().dispose();
        }

        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getResourceService().dispose();
        ServiceLocator.clearExceptPlayer();
    }

    private void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(mainGameTextures);
        ServiceLocator.getResourceService().loadAll();
    }

    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(mainGameTextures);
    }

    /**
     * Creates the main game's ui including components for rendering ui elements to the screen and
     * capturing and handling ui input.
     */
    private void createUI() {
        logger.debug("Creating ui");
        Stage stage = ServiceLocator.getRenderService().getStage();
        InputComponent inputComponent =
                ServiceLocator.getInputService().getInputFactory().createForTerminal();

        Entity ui = new Entity();
        ui.addComponent(new InputDecorator(stage, 10))
                .addComponent(new PerformanceDisplay())
                .addComponent(new MainGameActions(this.game))
                .addComponent(new MainGameDisplay(countdownTimer))
                .addComponent(new Terminal(null, this.game, countdownTimer))
                .addComponent(inputComponent)
                .addComponent(new TerminalDisplay(this.game));

        ServiceLocator.getEntityService().register(ui);
    }

    /**
     * Remaining time in seconds, clamped to >= 0
     */
    private long getRemainingSeconds() {
        long rem = countdownTimer.getRemainingMs();
        return rem > 0 ? rem / 1000 : 0;
    }

    /**
     * = Records player's current round performance and updates the leaderboard.
     * = This method is called automatically when a round ends.
     * = The leaderboard only persists for the duration of the current game session
     * and is cleared when the session ends.
     */
    private void recordRoundForLeaderboard(boolean won) {
        if (session == null) return;

        // Currency = processors from the player's InventoryComponent
        int processors = 0;
        Entity player = (gameArea != null) ? gameArea.getPlayer() : null;
        if (player != null) {
            InventoryComponent inv = player.getComponent(InventoryComponent.class);
            if (inv != null) {
                processors = inv.getProcessor();
            }
        }

        // Time played = remaining time on countdown timer if won, else 0
        float timePlayedSeconds = won ? (float) getRemainingSeconds() : 0f;

        session.getLeaderBoardManager().addRound(processors, timePlayedSeconds);
        session.getLeaderBoardManager().getLeaderBoard().forEach(entry -> logger.info(entry.toString()));
    }

    /**
     * Creates and displays the pause menu overlay on top of the game.
     * Registers the overlay entity so it can capture input and show the UI,
     * and listens for the "resume" event to remove itself when requested.
     */
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
        if (!countdownTimer.isPaused()) {
            countdownTimer.pause();
        }
        isPauseVisible = true;
        pauseToggledThisFrame = true;
    }

    /**
     * Removes and disposes the pause menu overlay.
     * Unregisters the overlay entity so it is no longer drawn or receives input.
     */
    private void hidePauseOverlay() {
        if (pauseOverlay != null) {
            pauseOverlay.dispose();
            ServiceLocator.getEntityService().unregister(pauseOverlay);
            if (!isMinimapVisible) {
                ServiceLocator.getTimeSource().setPaused(false);
            }
            pauseOverlay = null;
        }

        // Only unpause/resume if minimap is not visible
        if (!isMinimapVisible) {
            ServiceLocator.getTimeSource().setPaused(false);
            if (countdownTimer.isPaused()) {
                countdownTimer.resume();
            }
        }

        isPauseVisible = false;
        pauseToggledThisFrame = true;
    }

    private void saveState() {
        logger.info("Saving state");
        if (ServiceLocator.getSaveLoadService().save("slides", gameArea)) {
            logger.info("Saving data successful");
        } else {
            logger.info("Save data failed");
        }
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


    /**
     * Calculates the total elapsed time of the countdown timer in second
     *
     * @return the elapsed time in seconds
     */
    private long getCompleteTime() {
        return (countdownTimer.getDuration() - countdownTimer.getRemainingMs()) / 1000;
    }

    /**
     * Sets the game's screen to death screen
     *
     * <p>
     * Updates the death screen with the elapsed time before switching.
     * </p>
     */
    private void setDeathScreen() {
        ServiceLocator.getGlobalEvents().trigger("round:finished", false);
        game.setCarryOverLeaderBoard(session.getLeaderBoardManager());
        DeathScreen deathScreen = new DeathScreen(game);
        deathScreen.updateTime(getCompleteTime());
        game.setScreen(deathScreen);
    }

    /**
     * This private helper function sets the
     * 'isCleared' variable to false, so that
     * enemies will spawn in all the rooms
     * <p>
     * Should be called sometime before the game starts
     */
    private void unclearAllRooms() {
        Reception.unclearRoom();
        MainHall.unclearRoom();
        ElevatorGameArea.unclearRoom();
        OfficeGameArea.unclearRoom();
        ResearchGameArea.unclearRoom();
        SecurityGameArea.unclearRoom();
        ServerGameArea.unclearRoom();
        ShippingGameArea.unclearRoom();
        StorageGameArea.unclearRoom();
        TunnelGameArea.unclearRoom();
        MovingBossRoom.unclearRoom();
        StaticBossRoom.unclearRoom();
        FlyingBossRoom.unclearRoom();
    }

    /**
     * Clears all rooms up to specified room
     * Should be called when loading in.
     *
     * @param room room that the player will be spawned in.
     *             Clear all rooms behind it
     */
    private void clearUpTo(String room) {
        String[] roomList = {"Forest", "Reception", "Mainhall",
                "Security", "MovingBossRoom", "Office", "Elevator",
                "Research", "FlyingBossRoom", "Shipping",
                "Storage", "Server", "Tunnel"};

        Runnable[] runnables = {() -> {
        }, Reception::clearRoom,
                MainHall::clearRoom, SecurityGameArea::clearRoom,
                OfficeGameArea::clearRoom, ElevatorGameArea::clearRoom,
                ResearchGameArea::clearRoom, FlyingBossRoom::clearRoom,
                ShippingGameArea::clearRoom, StorageGameArea::clearRoom,
                ServerGameArea::clearRoom, TunnelGameArea::clearRoom};

        int i = 0;
        for (String eachRoom : roomList) {
            if (eachRoom.equals(room)) return;
            runnables[i].run();
            i++;
        }
    }
}
