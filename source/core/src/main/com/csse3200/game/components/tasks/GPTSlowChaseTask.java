package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.Entity;

/**
 * Chases a target entity indefinitely and the entity being visible causes a speed increase
 */
public class GPTSlowChaseTask extends AbstractChaseTask {

    /**
     * @param target   The entity to chase.
     * @param priority Task priority when chasing (0 when not chasing).
     * @param speed    The speed at which the enemy will chase the player.
     */
    public GPTSlowChaseTask(Entity target, int priority, Vector2 speed) {
        super(target, priority, speed);
    }

    @Override
    protected void triggerStartEvent() {
        this.owner.getEntity().getEvents().trigger("wanderStart");
    }

    @Override
    public int getPriority() {
        // Only slow chase if the player is not visible to the enemy
        if (!isTargetVisible()) {
            return priority;
        }
        return -1;
    }
}
