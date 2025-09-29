package com.csse3200.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.areas.*;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.maingame.MainGameDisplay;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.ItemPickUpComponent;
import com.csse3200.game.components.screens.PauseMenuDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.system.RenderFactory;
import com.csse3200.game.input.InputComponent;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.SaveLoadService;
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.services.CountdownTimerService;
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
    private final GameArea gameArea;

    private CountdownTimerService countdownTimer;


    private Entity pauseOverlay;
    private boolean isPauseVisible = false;

    public MainGameScreen(GdxGame game) {
        this.game = game;

        logger.debug("Initialising main game screen services");
        ServiceLocator.registerTimeSource(new GameTime());

        PhysicsService physicsService = new PhysicsService();
        ServiceLocator.registerPhysicsService(physicsService);
        physicsEngine = physicsService.getPhysics();

        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerSaveLoadService(new SaveLoadService());


        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());

        renderer = RenderFactory.createRenderer();
        renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);
        renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

        loadAssets();
        countdownTimer = new CountdownTimerService(ServiceLocator.getTimeSource(), 60000);
        createUI();

        logger.debug("Initialising main game screen entities");
        TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());
        gameArea = new ForestGameArea(terrainFactory, renderer.getCamera());
        com.csse3200.game.services.ServiceLocator.registerGameArea(gameArea);
        gameArea.create();

    }

    @Override
    public void render(float delta) {
        if (!isPauseVisible && !(ServiceLocator.getTimeSource().isPaused())
                && !ServiceLocator.isTransitioning()) {
            physicsEngine.update();
        }
        if (!com.csse3200.game.services.ServiceLocator.isTransitioning()) {
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
        renderer.render();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!isPauseVisible) {
                showPauseOverlay();
                countdownTimer.pause();
            } else {
                hidePauseOverlay();
                countdownTimer.resume();
            }
        }

        //switch to death screen when countdown timer is up
        if (countdownTimer.isTimeUP()) {
            setDeathScreen();
        }
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

        ServiceLocator.getEntityService().dispose();
        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getResourceService().dispose();
        ServiceLocator.clear();
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
                .addComponent(new Terminal(this.game, countdownTimer))
                .addComponent(inputComponent)
                .addComponent(new TerminalDisplay(this.game));

        ServiceLocator.getEntityService().register(ui);
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
                .addComponent(new InputDecorator(stage, 100));
        pauseOverlay.getEvents().addListener("save", this::saveState);
        pauseOverlay.getEvents().addListener("resume", this::hidePauseOverlay);
        ServiceLocator.getEntityService().register(pauseOverlay);
        ServiceLocator.getTimeSource().setPaused(true);
        isPauseVisible = true;
    }

    /**
     * Removes and disposes the pause menu overlay.
     * Unregisters the overlay entity so it is no longer drawn or receives input.
     */
    private void hidePauseOverlay() {
        if (pauseOverlay != null) {
            pauseOverlay.dispose();
            ServiceLocator.getEntityService().unregister(pauseOverlay);
            ServiceLocator.getTimeSource().setPaused(false);
            pauseOverlay = null;
        }
        isPauseVisible = false;
    }

    private void saveState() {
        logger.info("Saving state");
        if (ServiceLocator.getSaveLoadService().save("slides", gameArea)) {
            logger.info("Saving data successful");
        } else {
            logger.info("Save data failed");
        }
    }

    /**
     * Calculates the total elapsed time of the countdown timer in second
     *
     * @return the elapsed time in seconds
     */
    private long getCompleteTime(){
        return (countdownTimer.getDuration() - countdownTimer.getRemainingMs()) / 1000;
    }

    /**
     * Sets the game's screen to death screen
     *
     * <p>
     *     Updates the death screen with the elapsed time before switching.
     * </p>
     */
    private void setDeathScreen(){
        DeathScreen deathScreen = new DeathScreen(game);
        deathScreen.updateTime(getCompleteTime());
        game.setScreen(deathScreen);
    }


    /**
     * Overloaded constructor for loading the game from save file
     *
     * @param game     game
     * @param Filename loaded file
     */
    public MainGameScreen(GdxGame game, String Filename) {


        this.game = game;
        SaveLoadService.PlayerInfo load = SaveLoadService.load();
        logger.debug("Initialising main game screen services from file load");
        ServiceLocator.registerTimeSource(new GameTime());

        PhysicsService physicsService = new PhysicsService();
        ServiceLocator.registerPhysicsService(physicsService);
        physicsEngine = physicsService.getPhysics();

        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerSaveLoadService(new SaveLoadService());


        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());

        renderer = RenderFactory.createRenderer();
        renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);
        renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

        loadAssets();
        createUI();
        // null so default can return error
        GameArea areaLoad = null;
        logger.debug("Initialising main game screen entities");
        TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());

        //cases for all current areas
        switch (load.areaId) {
            case "Forest" -> areaLoad = ForestGameArea.load(terrainFactory, renderer.getCamera());
            case "Elevator" -> areaLoad = ElevatorGameArea.load(terrainFactory, renderer.getCamera());
            case "Office" -> areaLoad = OfficeGameArea.load(terrainFactory, renderer.getCamera());
            case "Mainhall" -> areaLoad = MainHall.load(terrainFactory, renderer.getCamera());
            case "Reception" -> areaLoad = Reception.load(terrainFactory, renderer.getCamera());
            case "Tunnel" -> areaLoad = TunnelGameArea.load(terrainFactory, renderer.getCamera());
            case "Security" -> areaLoad = SecurityGameArea.load(terrainFactory, renderer.getCamera());
            case "Storage" -> areaLoad = StorageGameArea.load(terrainFactory, renderer.getCamera());
            case "Shipping" -> areaLoad = ShippingGameArea.load(terrainFactory, renderer.getCamera());
            case "Server" -> areaLoad = ServerGameArea.load(terrainFactory, renderer.getCamera());
            default -> logger.error("couldnt create Game area from file");
        }

        gameArea = areaLoad;
        com.csse3200.game.services.ServiceLocator.registerGameArea(gameArea);
        gameArea.create();
        InventoryComponent help = gameArea.getPlayer().getComponent(InventoryComponent.class);
        ItemPickUpComponent testLoading = new ItemPickUpComponent(help);
        //repopulates the inventory
        if (load.inventory != null) {
            for (int i = 0; i < load.inventory.size(); i++) {
                Entity placehold = testLoading.createItemFromTexture(load.inventory.get(i));
                help.addItem(placehold);
            }
        }


        // currently not needed: sprint 3 refactor to fix everything
//    gameArea.getPlayer().getEvents().trigger("load player", load.inventory, load.ProcessNumber);
        // functionally bad but if it works
//    gameArea.loadIn(load.inventory, load.Health,load.ProcessNumber, load.position.x, load.position.y);

    }

}
