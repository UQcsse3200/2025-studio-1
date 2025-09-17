package com.csse3200.game.services;

import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.input.InputService;
import com.csse3200.game.physics.PhysicsService;
import com.csse3200.game.rendering.RenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simplified implementation of the Service Locator pattern:
 * https://martinfowler.com/articles/injection.html#UsingAServiceLocator
 *
 * <p>Allows global access to a few core game services.
 * Warning: global access is a trap and should be used <i>extremely</i> sparingly.
 * Read the wiki for details (https://github.com/UQcsse3200/game-engine/wiki/Service-Locator).
 */
public class ServiceLocator {
  private static final Logger logger = LoggerFactory.getLogger(ServiceLocator.class);
  private static EntityService entityService;
  private static RenderService renderService;
  private static PhysicsService physicsService;
  private static GameTime timeSource;
  private static InputService inputService;
  private static ResourceService resourceService;
  private static GameArea gameArea;
  private static SaveLoadService saveLoadService;
  private static Entity player;

  public static Entity getPlayer() {
    return player;
  }
  private static volatile boolean transitioning = false;

  public static EntityService getEntityService() {
    return entityService;
  }

  public static RenderService getRenderService() {
    return renderService;
  }

  public static PhysicsService getPhysicsService() {
    return physicsService;
  }

  public static GameTime getTimeSource() {
    return timeSource;
  }

  public static InputService getInputService() {
    return inputService;
  }

  public static ResourceService getResourceService() {
    return resourceService;
  }

  public static GameArea getGameArea() {return gameArea;}
  public static boolean isTransitioning() { return transitioning; }
  public static void setTransitioning(boolean value) { transitioning = value; }

  public static SaveLoadService getSaveLoadService() {return saveLoadService;}

  public static void registerGameArea(GameArea theArea) {
    logger.debug("Registering game area service {}", theArea);
    gameArea = theArea;
  }

  public static void registerPlayer(Entity person) {
    player = person;
  }

  public static void registerEntityService(EntityService service) {
    logger.debug("Registering entity service {}", service);
    entityService = service;
  }

  public static void registerRenderService(RenderService service) {
    logger.debug("Registering render service {}", service);
    renderService = service;
  }

  public static void registerPhysicsService(PhysicsService service) {
    logger.debug("Registering physics service {}", service);
    physicsService = service;
  }

  public static void registerTimeSource(GameTime source) {
    logger.debug("Registering time source {}", source);
    timeSource = source;
  }

  public static void registerInputService(InputService source) {
    logger.debug("Registering input service {}", source);
    inputService = source;
  }

  public static void registerResourceService(ResourceService source) {
    logger.debug("Registering resource service {}", source);
    resourceService = source;
  }

  public static void registerSaveLoadService(SaveLoadService source) {
    logger.debug("Registering save service {}", source);
    saveLoadService = source;
  }

  public static void clear() {
    entityService = null;
    renderService = null;
    physicsService = null;
    timeSource = null;
    inputService = null;
    resourceService = null;
    gameArea = null;
    saveLoadService = null;
  }
  private static final com.csse3200.game.events.EventHandler globalEvents = new com.csse3200.game.events.EventHandler();

  public static com.csse3200.game.events.EventHandler getGlobalEvents() {
    return globalEvents;
  }
  private ServiceLocator() {
    throw new IllegalStateException("Instantiating static util class");
  }
}
