package com.csse3200.game.entities;

import com.badlogic.gdx.utils.Array;
import com.csse3200.game.services.ServiceLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a global access point for entities to register themselves. This allows for iterating
 * over entities to perform updates each loop. All game entities should be registered here.
 * <p>
 * Avoid adding additional state here! Global access is often the easy but incorrect answer to
 * sharing data.
 */
public class EntityService {
    private static final Logger logger = LoggerFactory.getLogger(EntityService.class);
    private static final int INITIAL_CAPACITY = 16;

    private final Array<Entity> entities = new Array<>(false, INITIAL_CAPACITY);

    /**
     * Register a new entity with the entity service. The entity will be created and start updating.
     *
     * @param entity new entity.
     */
    public void register(Entity entity) {
        if (com.csse3200.game.services.ServiceLocator.isTransitioning()) {
            // Defer registration until after transition to avoid leaking into the wrong area
            com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                if (!com.csse3200.game.services.ServiceLocator.isTransitioning()) {
                    logger.debug("Deferred-registering {} in entity service", entity);
                    entities.add(entity);
                    entity.create();
                }
            });
            return;
        }
        logger.debug("Registering {} in entity service", entity);
        entities.add(entity);
        entity.create();
    }

    /**
     * Unregister an entity with the entity service. The entity will be removed and stop updating.
     *
     * @param entity entity to be removed.
     */
    public void unregister(Entity entity) {
        logger.debug("Unregistering {} in entity service", entity);
        entities.removeValue(entity, true);
    }

    /**
     * Update all registered entities. Should only be called from the main game loop.
     */
    public void update() {
        if (com.csse3200.game.services.ServiceLocator.isTransitioning()) {
            return;
        }
        Array<Entity> toRemove = new Array<>();
        for (Entity entity : entities) {
            entity.earlyUpdate();
            entity.update();

            if (entity.getToRemove()) {
                toRemove.add(entity);
            }
        }
        for (Entity entity : toRemove) {
            // Don't dispose the player entity even if marked for removal
            Entity player = ServiceLocator.getPlayer();
            if (entity != player) {
                entity.dispose();
                unregister(entity);
            }
        }
    }

    /**
     * Dispose all entities.
     */
    public void dispose() {
        for (Entity entity : entities) {
            entity.dispose();
        }
    }

    /**
     * Dispose all entities except the player.
     */
    public void disposeExceptPlayer() {
        Entity player = ServiceLocator.getPlayer();
        for (Entity entity : entities) {
            if (entity != player) {
                entity.dispose();
            }
        }
    }

    /**
     * Get a safe copy of the entities list. This is used for UI debug buttons to find enemies.
     *
     * @return a copy of the entities array.
     */
    public Array<Entity> getEntities() {
        return new Array<>(entities); // return copy to avoid external mutation
    }
}
