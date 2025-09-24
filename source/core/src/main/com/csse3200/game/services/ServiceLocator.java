package com.csse3200.game.services;

import com.csse3200.game.areas.AreaRouter;
import com.csse3200.game.areas.GameArea;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.EntityService;
import com.csse3200.game.events.EventHandler;
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
    private static final EventHandler globalEvents = new EventHandler();

    private static EntityService entityService;
    private static InputService inputService;
    private static PhysicsService physicsService;
    private static RenderService renderService;
    private static ResourceService resourceService;
    private static GameTime timeSource;

    private static GameArea gameArea;
    private static volatile AreaRouter areaRouter;
    private static Entity player;
    private static volatile boolean transitioning = false;

    private ServiceLocator() {
        throw new IllegalStateException("Instantiating static util class");
    }


    // --- service functions ---
    public static void registerEntityService(EntityService service) {
        logger.debug("Registering entity service {}", service);
        entityService = service;
    }
    public static EntityService getEntityService() {
        return entityService;
    }


    public static void registerInputService(InputService source) {
        logger.debug("Registering input service {}", source);
        inputService = source;
    }
    public static InputService getInputService() {
        return inputService;
    }


    public static void registerPhysicsService(PhysicsService service) {
        logger.debug("Registering physics service {}", service);
        physicsService = service;
    }
    public static PhysicsService getPhysicsService() {
        return physicsService;
    }


    public static void registerRenderService(RenderService service) {
        logger.debug("Registering render service {}", service);
        renderService = service;
    }
    public static RenderService getRenderService() {
        return renderService;
    }


    public static void registerResourceService(ResourceService source) {
        logger.debug("Registering resource service {}", source);
        resourceService = source;
    }
    public static ResourceService getResourceService() {
        return resourceService;
    }


    // --- non-service functions ---
    public static void registerAreaRouter(AreaRouter router) {
        logger.debug("Registering game area router service {}", router);
        areaRouter = router;
    }
    public static AreaRouter getAreaRouter() { return areaRouter; }


    public static void registerGameArea(GameArea theArea) {
        logger.debug("Registering game area service {}", theArea);
        gameArea = theArea;
    }
    public static GameArea getGameArea() { return gameArea; }


    public static void registerPlayer(Entity person) {
        logger.debug("Registering player {}", person);
        player = person;
    }
    public static Entity getPlayer() {
        return player;
    }


    public static void registerTimeSource(GameTime source) {
        logger.debug("Registering time source {}", source);
        timeSource = source;
    }
    public static GameTime getTimeSource() { return timeSource; }


    public static void registerTransitioning(boolean value) { transitioning = value; }
    public static boolean getTransitioning() { return transitioning; }

    public static void clear() {
        entityService = null;
        renderService = null;
        physicsService = null;
        timeSource = null;
        inputService = null;
        resourceService = null;
        gameArea = null;
    }

    public static EventHandler getGlobalEvents() {
        return globalEvents;
    }
}
