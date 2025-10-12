package com.csse3200.game.components.tasks;

import com.csse3200.game.entities.Entity;

/**
 * Chases a target entity until they get too far away or line of sight is lost
 */
public class ChaseTask extends AbstractChaseTask {

    /**
     * @param target           The entity to chase.
     * @param priority         Task priority when chasing (0 when not chasing).
     * @param viewDistance     Maximum distance from the entity at which chasing can start.
     * @param maxChaseDistance Maximum distance from the entity while chasing before giving up.
     */
    public ChaseTask(Entity target, int priority, float viewDistance, float maxChaseDistance) {
        super(target, priority, viewDistance, maxChaseDistance);
    }
}
