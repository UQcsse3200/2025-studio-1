package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.system.RenderFactory;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;

/**
 * Generic base for UI-driven screens (menus, win/lose, settings, etc.).
 * Centralises:
 * <ul>
 *   <li>Service registration (Input/Resource/Entity/Render/Time)</li>
 *   <li>Renderer creation and resize handling</li>
 *   <li>Background asset load/unload and drawing (optional)</li>
 *   <li>UI creation via {@link #createUIScreen(Stage)}</li>
 * </ul>
 * Subclasses override {@link #createUIScreen(Stage)} to provide their UI entity.
 */
abstract class BaseScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BaseScreen.class);

    protected final GdxGame game;
    protected final Renderer renderer;
    /**
     * Non-null UI stage guaranteed; also pushed into RenderService when possible.
     */
    protected final Stage stage;

    /**
     * Optional background textures to load and draw (might be empty).
     */
    private final String[] backgroundTextures;
    private final boolean hasBackground;

    /**
     * Constructs a BaseScreen, registers services, creates a renderer,
     * loads optional background textures, and builds UI.
     *
     * @param game               the game instance
     * @param backgroundTextures optional list of texture paths for the background (can be empty)
     */
    protected BaseScreen(GdxGame game, String... backgroundTextures) {
        this.game = Objects.requireNonNull(game, "game");
        this.backgroundTextures = backgroundTextures != null
                ? Arrays.copyOf(backgroundTextures, backgroundTextures.length)
                : new String[0];
        this.hasBackground = this.backgroundTextures.length > 0;

        logger.atDebug().log("Initialising {} services", getClass().getSimpleName());
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerTimeSource(new GameTime());

        this.renderer = RenderFactory.createRenderer();
        this.stage = ensureStage(); // make tests and headless envs happy

        // Keep existing behaviour: set initial camera position
        renderer.getCamera().getEntity().setPosition(5f, 5f);
        logger.atDebug().log("{} renderer created", getClass().getSimpleName());

        loadAssets();
        createUI();
    }

    /**
     * Subclasses must provide the screen's UI entity (added to the stage by this base).
     *
     * @param stage stage for UI
     * @return UI entity to register
     */
    protected abstract Entity createUIScreen(Stage stage);

    /**
     * Expose the stage to subclasses if needed.
     */
    protected Stage getStage() {
        return stage;
    }

    @Override
    public void render(float delta) {
        logger.atDebug().log("Rendering {} frame", getClass().getSimpleName());
        ServiceLocator.getEntityService().update();
        renderer.render();
    }

    @Override
    public void resize(int width, int height) {
        logger.atDebug().log("Resizing {} to {}x{}", getClass().getSimpleName(), width, height);
        renderer.resize(width, height);
    }

    @Override
    public void dispose() {
        logger.atDebug().log("Disposing {}", getClass().getSimpleName());
        try {
            renderer.dispose();
        } catch (Exception e) {
            logger.atDebug().setCause(e).log("Renderer dispose failed");
        }
        unloadAssets();
        try {
            ServiceLocator.getRenderService().dispose();
        } catch (Exception e) {
            logger.atDebug().setCause(e).log("RenderService dispose failed");
        }
        try {
            ServiceLocator.getEntityService().disposeExceptPlayer();
        } catch (Exception e) {
            logger.atDebug().setCause(e).log("EntityService dispose failed");
        }
        ServiceLocator.clearExceptPlayer();
        logger.atDebug().log("{} services cleared", getClass().getSimpleName());
    }

    /**
     * Loads optional background textures if provided.
     */
    private void loadAssets() {
        if (!hasBackground) return;
        logger.atDebug().log("Loading {} assets", getClass().getSimpleName());
        var resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(backgroundTextures);
        resourceService.loadAll();
    }

    /**
     * Unloads optional background textures if provided.
     */
    private void unloadAssets() {
        if (!hasBackground) return;
        logger.atDebug().log("Unloading {} assets", getClass().getSimpleName());
        var resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(backgroundTextures);
    }

    /**
     * Creates background image (if textures provided) and registers the UI entity.
     */
    private void createUI() {
        logger.atDebug().log("Creating {} UI", getClass().getSimpleName());

        if (hasBackground) {
            var resourceService = ServiceLocator.getResourceService();
            Texture bgTex = resourceService.getAsset(backgroundTextures[0], Texture.class);
            if (bgTex != null) {
                var bg = new Image(new TextureRegionDrawable(new TextureRegion(bgTex)));
                bg.setFillParent(true);
                bg.setScaling(Scaling.fill);
                stage.addActor(bg);
            } else {
                logger.atWarn().addArgument(backgroundTextures[0])
                        .log("Background texture {} was not loaded");
            }
        }

        var ui = createUIScreen(stage);
        ServiceLocator.getEntityService().register(ui);
        logger.atDebug().log("{} UI created and registered", getClass().getSimpleName());
    }

    /**
     * Ensures a non-null Stage. If RenderService does not yet have one,
     * create a Stage and try to bind it back to RenderService.
     */
    private Stage ensureStage() {
        var renderService = ServiceLocator.getRenderService();
        Stage s = renderService.getStage();
        if (s == null) {
            logger.atDebug().log("RenderService stage is null; creating a ScreenViewport-backed Stage");
            s = new Stage(new ScreenViewport());
            try {
                // If available, bind the stage so the Renderer uses the same instance.
                renderService.setStage(s);
            } catch (Exception e) {
                // Some test doubles may not expose setStage; local field still satisfies UI/tests.
                logger.atDebug().setCause(e)
                        .log("RenderService#setStage unavailable; using local Stage fallback");
            }
        }
        return s;
    }
}
