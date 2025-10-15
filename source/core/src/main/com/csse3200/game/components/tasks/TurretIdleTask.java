package com.csse3200.game.components.tasks;

import com.csse3200.game.entities.Entity;

/**
 * Stationary entity with firing capabilities when it sees enemy on sight. Pair it with TurretFiringTask.
 */
public class TurretIdleTask extends TurretTask {

    /**
     * @param target   player entity to chase
     * @param priority task priority when chasing
     */
    public TurretIdleTask(Entity target, int priority) {
        super(target, priority);
    }

    @Override
    public void triggerStartEvent() {
        owner.getEntity().getEvents().trigger("wanderStart");
    }

    @Override
    public int getPriority() {
        // Slow chase only if player NOT visible
        if (!isTargetVisible()) {
            return priority;
        }
        return -1;
    }
}
