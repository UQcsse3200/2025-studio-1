package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.screens.DeathScreenDisplay;
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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

/**
 * The game screen containing the death screen.
 */
public class DeathScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(DeathScreen.class);
    private final GdxGame game;
    private final Renderer renderer;
    private static final String[] deathScreenTextures = {"images/menu_background.png"};

    /**
     * Creates a new DeathScreen instance
     * Registers services, creates the renderer, loads assets, and builds the UI.
     *
     * @param game the {@link GdxGame} instance
     */
    public DeathScreen(GdxGame game) {
        this.game = game;

        logger.debug("Initialising death screen service");
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerTimeSource(new GameTime());


        renderer = RenderFactory.createRenderer();
        logger.debug("Death Screen renderer created");
        renderer.getCamera().getEntity().setPosition(5f, 5f);
        logger.debug("Death Screen renderer camera position setted");

        loadAssets();
        createUI();
    }

    @Override
    public void render(float delta) {
        logger.debug("Rendering death screen frame");
        ServiceLocator.getEntityService().update();
        renderer.render();
    }

    /**
     * Adjusts the death screens layout when the window is resized
     * @param width New width in pixels
     * @param height New height in pixels
     */
    @Override
    public void resize(int width, int height) {
        logger.debug("Resizing death screen frame to {}*{}", width, height);
        renderer.resize(width, height);
    }

    /**
     * Cleans up and diaposes of resources when the death screen is no longer used
     */
    @Override
    public void dispose() {
        logger.debug("Disposing death screen");
        renderer.dispose();
        unloadAssets();
        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getEntityService().dispose();
        logger.debug("Death screen service disposed");
        ServiceLocator.clear();
        logger.debug("Death screen ServiceLocation cleared");

    }

    /**
     * Loads textures and other resources required for the death screen.
     */
    private void loadAssets() {
        logger.debug("Loading assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(deathScreenTextures);
        ServiceLocator.getResourceService().loadAll();
    }

    /**
     * Unloads textures and other resources required for the death screen.
     */
    private void unloadAssets() {
        logger.debug("Death Screen assets unloading");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(deathScreenTextures);
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
        logger.debug("Death Screen background texture asset loaded");
        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(bgTex)));
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);
        logger.debug("Death Screen background image added");

        Entity ui = new Entity();
        ui.addComponent(new DeathScreenDisplay(game)).addComponent(new InputDecorator(stage, 10));
        ServiceLocator.getEntityService().register(ui);
        logger.debug("Death Screen UI created and registered");
    }
}
