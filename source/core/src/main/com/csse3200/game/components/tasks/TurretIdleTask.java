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
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Stationary entity with firing capabilities when it sees enemy on sight. Pair it with TurretFiringTask.
 */
public class TurretIdleTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private final int priority;
    private final float speedX;
    private final PhysicsEngine physics;
    private final DebugRenderer debugRenderer;
    private final RaycastHit hit = new RaycastHit();
    private final RaycastHit jumpHit = new RaycastHit();
    private PhysicsComponent physicsComponent;

    /**
     * @param target      player entity to chase
     * @param priority    task priority when chasing
     */
    public TurretIdleTask(Entity target, int priority) {
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
        owner.getEntity().getEvents().trigger("wanderStart");
    }

    @Override
    public void update() {
        if (target == null || physicsComponent == null) return;
        Body body = physicsComponent.getBody();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public int getPriority() {
        // Slow chase only if player NOT visible
        if (!isTargetVisible()) {
            return priority;
        }
        return -1;
    }

    private boolean isTargetVisible() {
        if (target == null) return false;
        Vector2 from = owner.getEntity().getCenterPosition();
        Vector2 to = target.getCenterPosition();

        if (physics.raycast(from, to, PhysicsLayer.OBSTACLE, hit)) {
            debugRenderer.drawLine(from, hit.point);
            return false;
        }
        debugRenderer.drawLine(from, to);
        return true;
    }
}
