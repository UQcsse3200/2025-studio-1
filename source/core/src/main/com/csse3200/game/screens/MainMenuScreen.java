package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.mainmenu.MainMenuActions;
import com.csse3200.game.components.mainmenu.MainMenuDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.system.RenderFactory;
import com.csse3200.game.input.InputDecorator;
import com.csse3200.game.input.InputService;
import com.csse3200.game.rendering.RenderService;
import com.csse3200.game.rendering.Renderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ResourceService;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The game screen containing the main menu.
 */
public class MainMenuScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(MainMenuScreen.class);
    private final GdxGame game;
    private final Renderer renderer;
    private static final String[] mainMenuTextures = {"images/logo.png", "images/menu_background.png"};

    /**
     * Builds the main menu screen.
     * Registers services, creates the renderer, loads assets, and builds the UI.
     */
    public MainMenuScreen(GdxGame game) {
        this.game = game;

        logger.debug("Initialising main menu screen services");
        // Clear any existing player reference when starting fresh
        ServiceLocator.clear();
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerTimeSource(new GameTime());

        renderer = RenderFactory.createRenderer();
        logger.debug("Main menu screen renderer created");

        loadAssets();
        createUI();
    }

    /**
     * Updates entities and renders the frame.
     */
    @Override
    public void render(float delta) {
        logger.debug("Rendering main menu screen frame");
        ServiceLocator.getEntityService().update();
        renderer.render();
    }

    /**
     * Forwards new size to the renderer.
     */
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

    /**
     * Frees screen resources and clears registered services.
     * Do not reuse the screen after this is called.
     */
    @Override
    public void dispose() {
        logger.debug("Disposing main menu screen");
        renderer.dispose();
        unloadAssets();
        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getEntityService().dispose();
        ServiceLocator.clear();
    }

    /**
     * Loads textures needed by the main menu.
     */
    private void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(mainMenuTextures);
        ServiceLocator.getResourceService().loadAll();
    }

    /**
     * Unloads textures that were loaded for this screen.
     */
    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(mainMenuTextures);
    }

    /**
     * Creates the main menu's ui including components for rendering ui elements to the screen and
     * capturing and handling ui input.
     */
    private void createUI() {
        logger.debug("Creating ui");
        Stage stage = ServiceLocator.getRenderService().getStage();

        // Add the background image as a Stage actor
        Texture bgTex = ServiceLocator.getResourceService()
                .getAsset("images/menu_background.png", Texture.class);
        logger.debug("Main menu screen background texture asset loaded");
        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(bgTex)));
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);
        logger.debug("Main menu screen background added");

        // Register the UI entity that owns the display and actions
        Entity ui = new Entity();
        ui.addComponent(new MainMenuDisplay(game))
                .addComponent(new InputDecorator(stage, 10))
                .addComponent(new MainMenuActions(game));
        ServiceLocator.getEntityService().register(ui);
        logger.debug("Main menu screen ui created and registered");
    }
}

