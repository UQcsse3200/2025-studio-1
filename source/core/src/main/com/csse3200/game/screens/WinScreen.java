package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.screens.WinScreenDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.entities.factories.RenderFactory;
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
 * The game screen containing the win screen.
 */
public class WinScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(WinScreen.class);
    private final GdxGame game;
    private final Renderer renderer;
    private static final String[] winScreenTextures = {"images/menu_background.png"};

    /**
     * Creates a new WinScreen instance
     * Registers services, creates the renderer, loads assets, and builds the UI.
     *
     * @param game the {@link GdxGame} instance
     */
    public WinScreen(GdxGame game) {
        this.game = game;

        logger.debug("Initialising win screen service");
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerTimeSource(new GameTime());


        renderer = RenderFactory.createRenderer();
        logger.debug("Win Screen renderer created");
        renderer.getCamera().getEntity().setPosition(5f, 5f);
        logger.debug("Win Screen renderer camera position set");

        loadAssets();
        createUI();
    }

    @Override
    public void render(float delta) {
        logger.debug("Rendering win screen frame");
        ServiceLocator.getEntityService().update();
        renderer.render();
    }

    /**
     * Adjusts the win screens layout when the window is resized
     * @param width New width in pixels
     * @param height New height in pixels
     */
    @Override
    public void resize(int width, int height) {
        logger.debug("Resizing win screen frame to {}*{}", width, height);
        renderer.resize(width, height);
    }

    /**
     * Cleans up and disposes of resources when the win screen is no longer used
     */
    @Override
    public void dispose() {
        logger.debug("Disposing win screen");
        renderer.dispose();
        unloadAssets();
        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getEntityService().dispose();
        logger.debug("Win screen service disposed");
        ServiceLocator.clear();
        logger.debug("Win screen ServiceLocation cleared");
    }

    /**
     * Loads textures and other resources required for the win screen.
     */
    private void loadAssets() {
        logger.debug("Loading Win Screen assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(winScreenTextures);
        ServiceLocator.getResourceService().loadAll();
    }

    /**
     * Unloads textures and other resources required for the win screen.
     */
    private void unloadAssets() {
        logger.debug("Unloading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(winScreenTextures);
    }

    /**
     * Creates the setting screen's ui including components for rendering ui elements to the screen
     * and capturing and handling ui input.
     */
    private void createUI() {
        logger.debug("Creating ui");
        Stage stage = ServiceLocator.getRenderService().getStage();

        Texture bgTex = ServiceLocator.getResourceService()
                .getAsset("images/menu_background.png", Texture.class);
        logger.debug("Win Screen background texture asset loaded");
        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(bgTex)));
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);
        logger.debug("Win Screen background image added");

        Entity ui = new Entity();
        ui.addComponent(new WinScreenDisplay(game)).addComponent(new InputDecorator(stage, 10));
        ServiceLocator.getEntityService().register(ui);
        logger.debug("Win Screen UI created and registered");
    }
}

