package com.csse3200.game.components.tasks;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;

/**
 * Ground slow-chase for GhostGPT: set X only; Box2D gravity handles Y.
 * Not using MovementTask because it sets both X and Y (makes flying). Refer: Box2D manual (forces/impulses).
 */
public class GPTGroundSlowChaseTask extends GPTGroundChaseTask {

    /**
     * @param target      player entity to chase
     * @param priority    task priority when chasing
     * @param speed       horizontal speed (units per second)
     * @param jumpImpulse upward impulse (scaled by mass)
     */
    public GPTGroundSlowChaseTask(Entity target, int priority, float speed, float jumpImpulse) {
        super(target, priority, speed);
        this.jumpImpulse = jumpImpulse;
        this.jumpCooldown = 1.2f; // seconds between jumps
        this.obstacleCheckDistance = 0.6f; // horizontal ray distance to look for obstacle
    }

    @Override
    protected void triggerStartEvent() {
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
