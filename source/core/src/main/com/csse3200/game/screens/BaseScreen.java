package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.GdxGame;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    /** Logger for lifecycle and debugging events. */
    private static final Logger logger = LoggerFactory.getLogger(BaseScreen.class);

    /** Game instance for navigation and context. */
    protected final GdxGame game;

    /** Renderer responsible for drawing. */
    protected final Renderer renderer;

    /** Optional background textures to load and draw (may be empty). */
    private final String[] backgroundTextures;

    /**
     * Constructs a BaseScreen, registers services, creates a renderer,
     * loads optional background textures, and builds UI.
     *
     * @param game the game instance
     * @param backgroundTextures optional list of texture paths for the background (can be empty)
     */
    protected BaseScreen(GdxGame game, String... backgroundTextures) {
        this.game = game;
        this.backgroundTextures = backgroundTextures != null ? backgroundTextures : new String[0];

        logger.debug("Initialising {} services", getClass().getSimpleName());
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerTimeSource(new GameTime());

        renderer = RenderFactory.createRenderer();
        logger.debug("{} renderer created", getClass().getSimpleName());
        renderer.getCamera().getEntity().setPosition(5f, 5f);

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

    @Override
    public void render(float delta) {
        logger.debug("Rendering {} frame", getClass().getSimpleName());
        ServiceLocator.getEntityService().update();
        renderer.render();
    }

    @Override
    public void resize(int width, int height) {
        logger.debug("Resizing {} to {}x{}", getClass().getSimpleName(), width, height);
        renderer.resize(width, height);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing {}", getClass().getSimpleName());
        renderer.dispose();
        unloadAssets();
        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getEntityService().dispose();
        ServiceLocator.clear();
        logger.debug("{} services cleared", getClass().getSimpleName());
    }

    /** Loads optional background textures if provided. */
    private void loadAssets() {
        if (backgroundTextures.length == 0) return;
        logger.debug("Loading {} assets", getClass().getSimpleName());
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(backgroundTextures);
        resourceService.loadAll();
    }

    /** Unloads optional background textures if provided. */
    private void unloadAssets() {
        if (backgroundTextures.length == 0) return;
        logger.debug("Unloading {} assets", getClass().getSimpleName());
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(backgroundTextures);
    }

    /** Creates background image (if textures provided) and registers the UI entity. */
    private void createUI() {
        logger.debug("Creating {} UI", getClass().getSimpleName());
        Stage stage = ServiceLocator.getRenderService().getStage();

        if (backgroundTextures.length > 0) {
            // Use the first texture as the background.
            Texture bgTex = ServiceLocator.getResourceService()
                    .getAsset(backgroundTextures[0], Texture.class);
            Image bg = new Image(new TextureRegionDrawable(new TextureRegion(bgTex)));
            bg.setFillParent(true);
            bg.setScaling(Scaling.fill);
            stage.addActor(bg);
        }

        Entity ui = createUIScreen(stage);
        ServiceLocator.getEntityService().register(ui);
        logger.debug("{} UI created and registered", getClass().getSimpleName());
    }
}
