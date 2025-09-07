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

abstract class BaseEndScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(BaseEndScreen.class);
    protected final GdxGame game;
    protected final Renderer renderer;

    private static final String[] BACKGROUND = {"images/menu_background.png"};

    /**
     * Constructs an end screen and initialises services, renderer, and UI.
     *
     * @param game the game instance
     */
    protected BaseEndScreen(GdxGame game) {
        this.game = game;

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

    @Override
    public void render(float delta) {
        logger.debug("Rendering {} frame", getClass().getSimpleName());
        ServiceLocator.getEntityService().update();
        renderer.render();
    }

    @Override
    public void resize(int width, int height) {
        logger.debug("Resizing {} to {}*{}", getClass().getSimpleName(), width, height);
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

    private void loadAssets() {
        logger.debug("Loading {} assets", getClass().getSimpleName());
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(BACKGROUND);
        resourceService.loadAll();
    }

    private void unloadAssets() {
        logger.debug("Unloading {} assets", getClass().getSimpleName());
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.unloadAssets(BACKGROUND);
    }

    /** Subclasses must provide the correct UI display component (Win/Death). */
    protected abstract Entity createUIScreen();

    private void createUI() {
        logger.debug("Creating {} UI", getClass().getSimpleName());
        Stage stage = ServiceLocator.getRenderService().getStage();

        Texture bgTex = ServiceLocator.getResourceService()
                .getAsset("images/menu_background.png", Texture.class);
        Image bg = new Image(new TextureRegionDrawable(new TextureRegion(bgTex)));
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);

        Entity ui = createUIScreen();
        ServiceLocator.getEntityService().register(ui);
        logger.debug("{} UI created and registered", getClass().getSimpleName());
    }
}
