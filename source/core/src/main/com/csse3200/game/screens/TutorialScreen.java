package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.GdxGame;
import com.csse3200.game.components.screens.TutorialClip;
import com.csse3200.game.components.screens.TutorialScreenDisplay;
import com.csse3200.game.components.screens.TutorialStep;
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

import java.util.List;


/**
 * The game screen containing the tutorial screen.
 */
public class TutorialScreen extends ScreenAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TutorialScreen.class);
    private final GdxGame game;
    private final Renderer renderer;
    private static final String[] tutorialTextures = {
            "images/background.png"
    };

    /**
     * Builds the tutorial screen.
     * Registers services, creates the renderer, loads assets, and builds the UI.
     */
    public TutorialScreen(GdxGame game) {
        this.game = game;
        logger.debug("Initialising tutorial screen service");
        // Register services
        ServiceLocator.registerInputService(new InputService());
        ServiceLocator.registerResourceService(new ResourceService());
        ServiceLocator.registerEntityService(new EntityService());
        ServiceLocator.registerRenderService(new RenderService());
        ServiceLocator.registerTimeSource(new GameTime());

        renderer = RenderFactory.createRenderer();
        logger.debug("Tutorial Screen renderer created");
        renderer.getCamera().getEntity().setPosition(5f, 5f);
        logger.debug("Tutorial Screen renderer camera position set");

        loadAssets();
        createUI();
    }

    /**
     * Loads textures needed by the tutorial screen.
     */
    private void loadAssets() {
        logger.debug("Loading tutorial screen assets");
        ResourceService resourceService = ServiceLocator.getResourceService();
        resourceService.loadTextures(tutorialTextures);
        resourceService.loadAll();
        logger.debug("Tutorial screen assets loaded");
    }

    /**
     * Creates the tutorial screen's ui including components for rendering ui elements to the screen and
     * capturing and handling ui input.
     */
    private void createUI() {
        logger.debug("Creating TutorialScreen UI");
        Stage stage = ServiceLocator.getRenderService().getStage();

        Texture bgTex = ServiceLocator.getResourceService()
                .getAsset("images/background.png", Texture.class);
        logger.debug("Tutorial Screen background texture asset loaded");
        Image bg = new Image(new TextureRegionDrawable(bgTex));
        bg.setFillParent(true);
        bg.setScaling(Scaling.fill);
        stage.addActor(bg);
        logger.debug("Tutorial Screen background image added");

        List<TutorialStep> steps = List.of(
                new TutorialStep("Welcome!", "Use WASD to move your character.",
                        new TutorialClip("images/tutorial/move", "frame_%04d.png", 25, 12f, true)),
                new TutorialStep("Attack", "Use space to attack enemies.",
                        new TutorialClip("images/tutorial/move", "frame_%04d.png", 25, 12f, true)),
                new TutorialStep("Pick up item", "Walk on an item to pick it up",
                        new TutorialClip("images/tutorial/move", "frame_%04d.png", 25, 12f, true))
        );
        logger.debug("Tutorial steps created");

        Entity ui = new Entity();
        ui.addComponent(new TutorialScreenDisplay(game, steps))
                .addComponent(new InputDecorator(stage, 10));
        ServiceLocator.getEntityService().register(ui);
        logger.debug("Tutorial Screen UI created and registered");
    }

    /**
     * Updates entities and renders the frame.
     */
    @Override
    public void render(float delta) {
        logger.debug("Render tutorial screen frame");
        ServiceLocator.getEntityService().update();
        renderer.render();
        logger.debug("Tutorial screen frame rendered");
    }

    /**
     * Forwards new size to the renderer.
     */
    @Override
    public void resize(int width, int height) {
        logger.debug("Resizing tutorial screen frame to {}*{}", width, height);
        renderer.resize(width, height);
        logger.debug("Resized death screen frame to {}*{}", width, height);
    }

    /**
     * Cleans up renderer and services and unloads assets.
     */
    @Override
    public void dispose() {
        logger.debug("Disposing tutorial screen");
        renderer.dispose();
        ServiceLocator.getResourceService().unloadAssets(tutorialTextures);
        ServiceLocator.getRenderService().dispose();
        ServiceLocator.getEntityService().dispose();
        logger.debug("Tutorial screen service disposed");
        ServiceLocator.clear();
        logger.debug("Tutorial screen ServiceLocation cleared");
    }
}