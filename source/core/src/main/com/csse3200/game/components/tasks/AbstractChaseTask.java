package com.csse3200.game.components.tasks;

import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.ServiceLocator;

public abstract class AbstractChaseTask extends DefaultTask implements PriorityTask {
    protected Entity target;
    protected int priority;
    protected float viewDistance;
    protected float maxChaseDistance;
    protected PhysicsEngine physics;
    protected DebugRenderer debugRenderer;
    protected RaycastHit hit = new RaycastHit();
    protected MovementTask movementTask;

    /**
     * @param target           The entity to chase.
     * @param priority         Task priority when chasing (0 when not chasing).
     * @param viewDistance     Maximum distance from the entity at which chasing can start.
     * @param maxChaseDistance Maximum distance from the entity while chasing before giving up.
     */
    public AbstractChaseTask(Entity target, int priority, float viewDistance, float maxChaseDistance) {
        this.target = target;
        this.priority = priority;
        this.viewDistance = viewDistance;
        this.maxChaseDistance = maxChaseDistance;
        physics = ServiceLocator.getPhysicsService().getPhysics();
        debugRenderer = ServiceLocator.getRenderService().getDebug();
    }

    @Override
    public void start() {
        super.start();
        movementTask = new MovementTask(target.getPosition());
        movementTask.create(owner);
        movementTask.start();

        this.owner.getEntity().getEvents().trigger("chaseStart");
    }

    @Override
    public void stop() {
        super.stop();
        movementTask.stop();
    }

    @Override
    public void update() {
        movementTask.setTarget(target.getPosition());
        movementTask.update();
        if (movementTask.getStatus() != Status.ACTIVE) {
            movementTask.start();
        }
    }

    @Override
    public int getPriority() {
        if (status == Status.ACTIVE) {
            return getActivePriority();
        }
        return getInactivePriority();
    }

    private float getDistanceToTarget() {
        return owner.getEntity().getPosition().dst(target.getPosition());
    }

    protected int getActivePriority() {
        float dst = getDistanceToTarget();
        if (dst > maxChaseDistance || !isTargetVisible()) {
            return -1; // Too far, stop chasing
        }
        return priority;
    }

    protected int getInactivePriority() {
        float dst = getDistanceToTarget();
        if (dst < viewDistance && isTargetVisible()) {
            return priority;
        }
        return -1;
    }

    private boolean isTargetVisible() {
        return TaskUtils.isVisible(owner.getEntity(), target, physics, debugRenderer, hit);
    }
}
