package com.csse3200.game.components.tasks;

import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.ServiceLocator;

public abstract class TurretTask extends DefaultTask implements PriorityTask {
    protected Entity target;
    protected int priority;
    protected float speedX = 0f;
    protected PhysicsEngine physics;
    protected DebugRenderer debugRenderer;
    protected RaycastHit hit = new RaycastHit();
    protected RaycastHit jumpHit = new RaycastHit();
    protected PhysicsComponent physicsComponent;

    /**
     * Constructs the task
     *
     * @param target   The target to fire at
     * @param priority The priority
     */
    protected TurretTask(Entity target, int priority) {
        this.target = target;
        this.priority = priority;
        this.speedX = 0f;
        this.physics = ServiceLocator.getPhysicsService().getPhysics();
        this.debugRenderer = ServiceLocator.getRenderService().getDebug();
    }

    @Override
    public void start() {
        super.start();
        physicsComponent = owner.getEntity().getComponent(PhysicsComponent.class);
        triggerStartEvent();
    }

    /**
     * Base implementation does nothing, children override to
     * trigger a specified event if needed.
     */
    protected void triggerStartEvent() {
    }

    @Override
    public void update() {
        if (target == null || physicsComponent == null) return;

        performFiringAction();
    }

    /**
     * Base implementation does nothing, child classes override for
     * implemented functionality
     */
    protected void performFiringAction() {
    }

    protected boolean isTargetVisible() {
        return TaskUtils.isVisible(owner.getEntity(), target, physics, debugRenderer, hit);
    }
}
