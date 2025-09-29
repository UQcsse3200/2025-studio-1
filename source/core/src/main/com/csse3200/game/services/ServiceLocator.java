package com.csse3200.game.services;

import com.csse3200.game.areas.GameArea;
import com.csse3200.game.areas.difficulty.Difficulty;
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
    private static Difficulty difficulty;
    private static DiscoveryService discoveryService; // track discovered rooms

    private static Float cachedPlayerStamina; // preserved across area transitions
    private static Integer cachedPlayerHealth; // preserved across area transitions
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

    public static GameArea getGameArea() {
        return gameArea;
    }

    public static boolean isTransitioning() {
        return transitioning;
    }

    public static void setTransitioning(boolean value) {
        transitioning = value;
    }

    public static SaveLoadService getSaveLoadService() {
        return saveLoadService;
    }

    public static Difficulty getDifficulty() {
        return difficulty;
    }

    public static DiscoveryService getDiscoveryService() {
        return discoveryService;
    }

    public static void registerGameArea(GameArea theArea) {
        logger.debug("Registering game area service {}", theArea);
        gameArea = theArea;
    }

    public static void registerPlayer(Entity person) {
        player = person;
    }
    /**
     * Returns cached player stamina to restore after area transitions.
     */
    public static Float getCachedPlayerStamina() {
        return cachedPlayerStamina;
    }

    /**
     * Caches player stamina to be restored when the next player entity is created.
     */
    public static void setCachedPlayerStamina(Float value) {
        cachedPlayerStamina = value;
    }

    /**
     * Returns cached player health to restore after area transitions.
     */
    public static Integer getCachedPlayerHealth() {
        return cachedPlayerHealth;
    }

    /**
     * Caches player health to be restored when the next player entity is created.
     */
    public static void setCachedPlayerHealth(Integer value) {
        cachedPlayerHealth = value;
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

    public static void registerDifficulty(Difficulty source) {
        logger.debug("Registering difficulty {}", source);
        difficulty = source;
    }

    public static void registerDiscoveryService(DiscoveryService service) {
        logger.debug("Registering discovery service {}", service);
        discoveryService = service;
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
        cachedPlayerStamina = null;
        cachedPlayerHealth = null;
        discoveryService = null;
    }

    private static final com.csse3200.game.events.EventHandler globalEvents = new com.csse3200.game.events.EventHandler();

    public static com.csse3200.game.events.EventHandler getGlobalEvents() {
        return globalEvents;
    }

    private ServiceLocator() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
