package com.csse3200.game.tutorial;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.screens.PauseMenuDisplay;
import com.csse3200.game.components.teleporter.TeleporterComponent;
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
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TutorialGameScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TutorialGameScreen.class);
    private static final String[] TEXTURES = {"images/heart.png"};
    private static final String[] playerSound1 = {"sounds/jump.mp3"};
    private static final Vector2 CAMERA_POSITION = new Vector2(7.5f, 7.5f);
    private final GdxGame game;
    private final Renderer renderer;
    private final PhysicsEngine physicsEngine;
    private final GameArea gameArea;
    private Entity pauseOverlay;
    private boolean isPauseVisible = false;

    public TutorialGameScreen(GdxGame game, GameArea singleRoomArea) {
        GameArea gameArea1;
        this.game = game;

        // --- Core services (minimal set) ---
        ServiceLocator.registerTimeSource(new GameTime());
        PhysicsService physicsService = new PhysicsService();
        ServiceLocator.registerPhysicsService(physicsService);
        physicsEngine = physicsService.getPhysics();

        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerSaveLoadService(new SaveLoadService());

        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.clearPlayer();

        renderer = RenderFactory.createRenderer();
        renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);
        renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

        loadAssets();
        createUI();

        // --- Single room area only ---
        ServiceLocator.registerGameArea(singleRoomArea);
        singleRoomArea.create();
        gameArea1 = singleRoomArea;

        logger.debug("TutorialGameScreen initialised (single room)");

        var camComp = renderer.getCamera();
        TerrainFactory terrainFactory = new TerrainFactory(camComp);
        GameArea area = new TutorialGameArea(terrainFactory, camComp);

        ServiceLocator.registerGameArea(area);
        area.create();                 // <-- safe now
        gameArea1 = area;
        this.gameArea = gameArea1;
    }

    /**
     * Convenience ctor if you want us to build a bare single room on the default camera.
     */
    public TutorialGameScreen(GdxGame game) {
        this.game = game;

        // Core services FIRST
        ServiceLocator.registerTimeSource(new GameTime());
        PhysicsService physicsService = new PhysicsService();
        ServiceLocator.registerPhysicsService(physicsService);
        physicsEngine = physicsService.getPhysics();

        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerSaveLoadService(new SaveLoadService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.clearPlayer();

        // Now safe to build the renderer (RenderFactory needs EntityService)
        renderer = RenderFactory.createRenderer();
        renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);
        renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

        loadAssets();
        createUI();

        // Build a default Tutorial area using the renderer’s CameraComponent
        var camComp = renderer.getCamera();
        TerrainFactory terrainFactory = new TerrainFactory(camComp);
        GameArea area = new TutorialGameArea(terrainFactory, camComp);

        ServiceLocator.registerGameArea(area);
        area.create();
        this.gameArea = area;

        logger.debug("TutorialGameScreen initialised (single room)");
    }


    private static TutorialGameArea makeDefaultArea() {
        var renderer = RenderFactory.createRenderer();
        var camComp = renderer.getCamera();
        var terrainFactory = new TerrainFactory(camComp);
        return new TutorialGameArea(terrainFactory, camComp);
    }


    @Override
    public void render(float delta) {
        // Reset teleporter ESC consumption at start of frame
        TeleporterComponent.resetEscConsumed();

        // Step the world only if we’re not paused and not in a transition
        if (!isPauseVisible && !ServiceLocator.getTimeSource().isPaused() && !ServiceLocator.isTransitioning()) {
            physicsEngine.update();
        }
        if (!ServiceLocator.isTransitioning()) {
            ServiceLocator.getEntityService().update();
        }

        renderer.render();

        // Toggle Pause overlay on ESC, but don’t double-consume if a teleporter used ESC this frame
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!TeleporterComponent.wasEscConsumedThisFrame()) {
                if (!isPauseVisible) {
                    showPauseOverlay();
                } else {
                    hidePauseOverlay();
                }
            }
        }
    }


    private void showPauseOverlay() {
        Stage stage = ServiceLocator.getRenderService().getStage();
        pauseOverlay = new Entity()
                .addComponent(new PauseMenuDisplay(game))
                .addComponent(new InputDecorator(stage, 100)); // on top

        // Listen to UI events from PauseMenuDisplay
        pauseOverlay.getEvents().addListener("resume", this::hidePauseOverlay);

        ServiceLocator.getEntityService().register(pauseOverlay);
        ServiceLocator.getTimeSource().setPaused(true);
        isPauseVisible = true;
    }

    /**
     * Hides and disposes the pause overlay; resumes the game time.
     */
    private void hidePauseOverlay() {
        if (pauseOverlay != null) {
            pauseOverlay.dispose();
            ServiceLocator.getEntityService().unregister(pauseOverlay);
            pauseOverlay = null;
        }
        ServiceLocator.getTimeSource().setPaused(false);
        isPauseVisible = false;
    }

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing tutorial screen (single room)");
        renderer.dispose();
        unloadAssets();

        // Preserve player entity during disposal (matches your pattern)
        Entity player = ServiceLocator.getPlayer();
        ServiceLocator.getEntityService().disposeExceptPlayer();
        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getResourceService().dispose();
        ServiceLocator.clearExceptPlayer();
    }

    private void loadAssets() {
        ResourceService rs = ServiceLocator.getResourceService();
        rs.loadTextures(TEXTURES);
        rs.loadSounds(playerSound1);
        rs.loadAll();
    }

    private void unloadAssets() {
        ResourceService rs = ServiceLocator.getResourceService();
        rs.unloadAssets(TEXTURES);
    }

    /**
     * Minimal HUD: Performance + Terminal + MainGameActions (no timer UI).
     */
    private void createUI() {
        Stage stage = ServiceLocator.getRenderService().getStage();
        InputComponent inputComponent =
                ServiceLocator.getInputService().getInputFactory().createForTerminal();

        Entity ui = new Entity();
        ui.addComponent(new InputDecorator(stage, 10))
                .addComponent(new PerformanceDisplay())
                .addComponent(new MainGameActions(this.game))
                .addComponent(new Terminal(null, this.game, /* no timer */ null))
                .addComponent(inputComponent)
                .addComponent(new TerminalDisplay(this.game));

        ServiceLocator.getEntityService().register(ui);
    }
}
