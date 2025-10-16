package com.csse3200.game.components.tasks;

import com.csse3200.game.entities.Entity;

/**
 * Chases a target entity until they get too far away or go above
 */
public class BossChaseTask extends AbstractChaseTask {
    private static final float ABOVE_EPS = 1f;

    /**
     * @param target           The entity to chase.
     * @param priority         Task priority when chasing (0 when not chasing).
     * @param viewDistance     Maximum distance from the entity at which chasing can start.
     * @param maxChaseDistance Maximum distance from the entity while chasing before giving up.
     */
    public BossChaseTask(Entity target, int priority, float viewDistance, float maxChaseDistance) {
        super(target, priority, viewDistance, maxChaseDistance);
    }

    @Override
    protected void triggerStartEvent() {
        this.owner.getEntity().getEvents().trigger("chaseStart");
    }

    private boolean isTargetAbove() {
        float myY = owner.getEntity().getCenterPosition().y;
        float ty = target.getCenterPosition().y;
        return ty > myY + ABOVE_EPS;
    }

    @Override
    protected int getActivePriority() {
        if (isTargetAbove()) {
            return -1;
        }
        return super.getActivePriority();
    }

    @Override
    protected int getInactivePriority() {
        if (isTargetAbove()) {
            return -1;
        }
        return super.getInactivePriority();
    }
}