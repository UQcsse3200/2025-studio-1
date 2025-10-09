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
import com.csse3200.game.components.screens.Minimap;
import com.csse3200.game.components.screens.MinimapDisplay;
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
import com.csse3200.game.services.*;
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TutorialGameScreen extends ScreenAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(TutorialGameScreen.class);
    private static final String[] TEXTURES = {"images/heart.png"};
    private static final String[] PLAYER_SOUNDS = {"sounds/jump.mp3"};
    private static final Vector2 CAMERA_POS = new Vector2(7.5f, 7.5f);

    private final GdxGame game;
    private final Renderer renderer;
    private final PhysicsEngine physics;
    private final GameArea gameArea;

    private Entity pauseOverlay;
    private boolean isPauseVisible;

    private Entity minimap;
    private boolean isMinimapVisible;

    private Entity teleporterOverlay;
    private boolean isTeleporterVisible;

    public TutorialGameScreen(GdxGame game) {
        this.game = game;

        // Core services
        ServiceLocator.registerTimeSource(new GameTime());
        PhysicsService physSvc = new PhysicsService();
        ServiceLocator.registerPhysicsService(physSvc);
        physics = physSvc.getPhysics();
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerSaveLoadService(new SaveLoadService());
        ServiceLocator.registerDiscoveryService(new DiscoveryService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.clearPlayer();

        renderer = RenderFactory.createRenderer();
        renderer.getCamera().getEntity().setPosition(CAMERA_POS);
        renderer.getDebug().renderPhysicsWorld(physics.getWorld());

        loadAssets();
        createUI();

        // Build tutorial area
        var cam = renderer.getCamera();
        GameArea area = new TutorialGameArea(new TerrainFactory(cam), cam);
        ServiceLocator.registerGameArea(area);
        area.create();

        // Mark current room discovered for minimap (prevents NPE in Minimap.open)
        var ds = ServiceLocator.getDiscoveryService();
        if (ds != null) {
            ds.discover(area.toString()); // expected "Tutorial"
        }

        this.gameArea = area;
        LOG.debug("TutorialGameScreen initialised.");
    }

    @Override
    public void render(float delta) {
        // Let teleporter consume ESC if it needs to close its own UI
        TeleporterComponent.resetEscConsumed();

        // Step simulation when no modal is up
        if (!isPauseVisible && !isMinimapVisible && !isTeleporterVisible
                && !ServiceLocator.getTimeSource().isPaused()
                && !ServiceLocator.isTransitioning()) {
            physics.update();
        }
        if (!ServiceLocator.isTransitioning()) {
            ServiceLocator.getEntityService().update();
        }

        renderer.render();

        // ESC → pause (unless teleporter consumed)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (!TeleporterComponent.wasEscConsumedThisFrame()) {
                if (!isPauseVisible) showPauseOverlay();
                else hidePauseOverlay();
            }
        }

        // TAB → minimap (only if tutorial allows it)
        boolean tabAllowed = isKeyAllowedByTutorial(Input.Keys.TAB);
        if (tabAllowed && Gdx.input.isKeyJustPressed(Input.Keys.TAB) && !isTeleporterVisible) {
            if (!isMinimapVisible && !isPauseVisible) {
                showMinimapOverlay();
            } else if (isMinimapVisible) {
                hideMinimapOverlay();
            }
        }


        // Minimap zoom while open
        if (isMinimapVisible) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS)) {
                minimap.getComponent(MinimapDisplay.class).zoomIn();
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS)) {
                minimap.getComponent(MinimapDisplay.class).zoomOut();
            }
        }
    }

    /**
     * Query the active tutorial overlays to decide if a polling key (e.g. TAB) should be honoured.
     * - While the Intro overlay is visible: only ENTER/SPACE are allowed; everything else denied.
     * - If a Cue overlay is active: defer to its {@code allowGameplayKey}.
     * - If no tutorial overlay is present: allow.
     */
    private boolean isKeyAllowedByTutorial(int keycode) {
        // Ask the world what overlays are active
        var es = ServiceLocator.getEntityService();
        if (es == null) return true;

        boolean introShowing = false;
        CueTypewriterOverlay cueOverlay = null;

        for (Entity e : es.getEntities()) {
            if (!introShowing && e.getComponent(TutorialIntroOverlay.class) != null) {
                introShowing = true;
            }
            if (cueOverlay == null) {
                cueOverlay = e.getComponent(CueTypewriterOverlay.class);
            }
        }

        // While the intro overlay is visible: allow ONLY Enter/Space, block everything else (incl. TAB)
        if (introShowing) {
            return keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE;
        }

        // While the cue overlay is active: delegate to its dynamic allow-list (current cue keys + ESC/F1)
        if (cueOverlay != null) {
            return cueOverlay.allowGameplayKey(keycode);
        }

        // No tutorial gating overlays → allow
        return true;
    }
    
    // ───────────── Overlays ─────────────

    private void showPauseOverlay() {
        Stage stage = ServiceLocator.getRenderService().getStage();
        pauseOverlay = new Entity()
                .addComponent(new PauseMenuDisplay(game, /* showRestart = */ false))
                .addComponent(new InputDecorator(stage, 100));
        pauseOverlay.getEvents().addListener("resume", this::hidePauseOverlay);

        ServiceLocator.getEntityService().register(pauseOverlay);
        ServiceLocator.getTimeSource().setPaused(true);
        isPauseVisible = true;
    }

    private void hidePauseOverlay() {
        if (pauseOverlay != null) {
            pauseOverlay.dispose();
            ServiceLocator.getEntityService().unregister(pauseOverlay);
            pauseOverlay = null;
        }
        if (!isMinimapVisible && !isTeleporterVisible) {
            ServiceLocator.getTimeSource().setPaused(false);
        }
        isPauseVisible = false;
    }

    private void showMinimapOverlay() {
        // Ensure discovery service exists & current room is known
        if (ServiceLocator.getDiscoveryService() == null) {
            ServiceLocator.registerDiscoveryService(new DiscoveryService());
            ServiceLocator.getDiscoveryService().discover(gameArea.toString());
        }

        LOG.info("Showing minimap overlay");
        Stage stage = ServiceLocator.getRenderService().getStage();
        minimap = new Entity()
                .addComponent(new MinimapDisplay(
                        game,
                        new Minimap(
                                Gdx.graphics.getHeight(),
                                Gdx.graphics.getWidth(),
                                // ensure this file has: "Tutorial,<x>,<y>"
                                "configs/tutorial_layout.txt")))
                .addComponent(new InputDecorator(stage, 100));
        minimap.getEvents().addListener("resume", this::hideMinimapOverlay);

        ServiceLocator.getEntityService().register(minimap);
        ServiceLocator.getTimeSource().setPaused(true);
        isMinimapVisible = true;
    }

    private void hideMinimapOverlay() {
        LOG.info("Hiding minimap overlay");
        if (minimap != null) {
            minimap.dispose();
            ServiceLocator.getEntityService().unregister(minimap);
            minimap = null;
        }
        if (!isPauseVisible && !isTeleporterVisible) {
            ServiceLocator.getTimeSource().setPaused(false);
        }
        isMinimapVisible = false;
    }

    // Optional teleporter hooks (call from teleporter UI)
    public void onTeleporterOpen(Entity overlayEntityOrNull) {
        this.teleporterOverlay = overlayEntityOrNull;
        isTeleporterVisible = true;
        ServiceLocator.getTimeSource().setPaused(true);
    }

    public void onTeleporterClose() {
        if (teleporterOverlay == null) {
            teleporterOverlay.dispose();
            ServiceLocator.getEntityService().unregister(teleporterOverlay);
            teleporterOverlay = null;
        }
        isTeleporterVisible = false;
        if (!isPauseVisible && !isMinimapVisible) {
            ServiceLocator.getTimeSource().setPaused(false);
        }
    }

    // ───────────────────────────── Boilerplate ─────────────────────────────

    @Override
    public void resize(int width, int height) {
        renderer.resize(width, height);
    }

    @Override
    public void dispose() {
        LOG.debug("Disposing tutorial screen");
        renderer.dispose();
        unloadAssets();

        // keep player entity alive per project convention
        Entity player = ServiceLocator.getPlayer();
        ServiceLocator.getEntityService().disposeExceptPlayer();
        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getResourceService().dispose();
        ServiceLocator.clearExceptPlayer();
    }

    private void loadAssets() {
        ResourceService rs = ServiceLocator.getResourceService();
        rs.loadTextures(TEXTURES);
        rs.loadSounds(PLAYER_SOUNDS);
        rs.loadAll();
    }

    private void unloadAssets() {
        ResourceService rs = ServiceLocator.getResourceService();
        rs.unloadAssets(TEXTURES);
    }

    /**
     * Minimal HUD: Performance + Terminal + MainGameActions.
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
