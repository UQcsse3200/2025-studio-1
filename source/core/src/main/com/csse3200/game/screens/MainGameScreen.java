package com.csse3200.game.screens;

import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.csse3200.game.GdxGame;
import com.csse3200.game.areas.ForestGameArea;
import com.csse3200.game.areas.terrain.TerrainFactory;
import com.csse3200.game.components.maingame.MainGameActions;
import com.csse3200.game.components.screens.PauseMenuDisplay;
import com.csse3200.game.components.screens.ShopScreenDisplay;
import com.csse3200.game.components.shop.CatalogService;
import com.csse3200.game.components.shop.ShopDemo;
import com.csse3200.game.components.shop.ShopManager;
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
import com.csse3200.game.services.ServiceLocator;
import com.csse3200.game.ui.terminal.Terminal;
import com.csse3200.game.ui.terminal.TerminalDisplay;
import com.csse3200.game.components.maingame.MainGameExitDisplay;
import com.csse3200.game.components.gamearea.PerformanceDisplay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.csse3200.game.GdxGame.ScreenType;
import com.csse3200.game.components.CombatStatsComponent;


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
  private final ForestGameArea forestGameArea;

  private Entity pauseOverlay;
  private boolean isPauseVisible = false;
  private Entity shopOverlay;
  private boolean isShopVisible = false;

  public MainGameScreen(GdxGame game) {
    this.game = game;

    logger.debug("Initialising main game screen services");
    ServiceLocator.registerTimeSource(new GameTime());

    PhysicsService physicsService = new PhysicsService();
    ServiceLocator.registerPhysicsService(physicsService);
    physicsEngine = physicsService.getPhysics();

    ServiceLocator.registerInputService(new InputService());
    ServiceLocator.registerResourceService(new ResourceService());

    ServiceLocator.registerEntityService(new EntityService());
    ServiceLocator.registerRenderService(new RenderService());

    renderer = RenderFactory.createRenderer();
    renderer.getCamera().getEntity().setPosition(CAMERA_POSITION);
    renderer.getDebug().renderPhysicsWorld(physicsEngine.getWorld());

    loadAssets();
    createUI();

    logger.debug("Initialising main game screen entities");
    TerrainFactory terrainFactory = new TerrainFactory(renderer.getCamera());
    forestGameArea = new ForestGameArea(terrainFactory, renderer.getCamera());
    com.csse3200.game.services.ServiceLocator.registerGameArea(forestGameArea);
    forestGameArea.create();
  }

  @Override
  public void render(float delta) {
    if (!isPauseVisible && !isShopVisible
            && !com.csse3200.game.services.ServiceLocator.isTransitioning()) {
      physicsEngine.update();
    }
    if (!com.csse3200.game.services.ServiceLocator.isTransitioning()) {
      ServiceLocator.getEntityService().update();
    }
    Entity player = forestGameArea.getPlayer();
    //show death screen when player is dead
    if (player != null) {
      var playerStat = player.getComponent(CombatStatsComponent.class);
      if (playerStat != null && playerStat.isDead()) {
        game.setScreen(ScreenType.DEATH_SCREEN);
      }
    }
    renderer.render();
    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      if (!isPauseVisible) {
        showPauseOverlay();
      } else {
        hidePauseOverlay();
      }
    }
    if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
      if (!isShopVisible) {
        showShopOverlay();
      } else {
        hideShopOverlay();
      }
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
        .addComponent(new MainGameExitDisplay())
        .addComponent(new Terminal(this.game))
        .addComponent(inputComponent)
        .addComponent(new TerminalDisplay());
    ServiceLocator.getEntityService().register(ui);
  }

  /**
   * Creates and displays the pause menu overlay on top of the game.
   * Registers the overlay entity so it can capture input and show the UI,
   * and listens for the "resume" event to remove itself when requested.
   */
  private void showPauseOverlay() {
    Stage stage = ServiceLocator.getRenderService().getStage();
    pauseOverlay = new Entity()
            .addComponent(new PauseMenuDisplay(game))
            .addComponent(new InputDecorator(stage, 100));
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

  /**
   * Creates and displays the shop overlay on top of the game.
   */
  private void showShopOverlay() {
    // once shop is an entity
    // shop.show()
    Stage stage = ServiceLocator.getRenderService().getStage();
    CatalogService catalog = ShopDemo.makeDemoCatalog();
    ShopManager manager = new ShopManager(catalog);

    shopOverlay = new Entity()
            .addComponent(new ShopScreenDisplay(forestGameArea, manager))
            .addComponent(new InputDecorator(stage, 100));

    shopOverlay.getEvents().addListener("closeShop", this::hideShopOverlay);
    ServiceLocator.getEntityService().register(shopOverlay);
    ServiceLocator.getTimeSource().setPaused(true);
    isShopVisible = true;
  }

  /**
   * Removes and disposes the shop overlay.
   */
  private void hideShopOverlay() {
    // once shop is an entity
    // shop.hide()
    if (shopOverlay != null) {
      shopOverlay.dispose();
      ServiceLocator.getEntityService().unregister(shopOverlay);
      shopOverlay = null;
    }
    ServiceLocator.getTimeSource().setPaused(false);
    isShopVisible = false;
  }


}
