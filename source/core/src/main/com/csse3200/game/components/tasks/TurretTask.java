package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
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
    public TurretTask(Entity target, int priority) {
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
        Body body = physicsComponent.getBody();

        performFiringAction();
    }

    /**
     * Base implementation does nothing, child classes override for
     * implemented functionality
     */
    protected void performFiringAction() {
    }

    public abstract int getPriority();

    protected boolean isTargetVisible() {
        return checkVisibility(target, owner.getEntity(), physics, hit, debugRenderer);
    }

    public static boolean checkVisibility(Entity target, Entity entity, PhysicsEngine physics, RaycastHit hit, DebugRenderer debugRenderer) {
        if (target == null) return false;
        Vector2 from = entity.getCenterPosition();
        Vector2 to = target.getCenterPosition();

        if (physics.raycast(from, to, PhysicsLayer.OBSTACLE, hit)) {
            debugRenderer.drawLine(from, hit.point);
            return false;
        }
        debugRenderer.drawLine(from, to);
        return true;
    }
}
