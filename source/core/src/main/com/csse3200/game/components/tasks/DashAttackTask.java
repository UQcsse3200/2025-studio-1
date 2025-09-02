package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.ServiceLocator;

/** Chases a target entity indefinitely and the entity being visible causes a speed increase */
public class DashAttackTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private final int priority;
    private final Vector2 speed;
    private final long cooldown;
    private final long dashTime;
    private long lastDashTime;
    private final PhysicsEngine physics;
    private final DebugRenderer debugRenderer;
    private final RaycastHit hit = new RaycastHit();
    private MovementTask movementTask;

    /**
     * @param target The entity to chase.
     * @param priority Task priority when chasing (0 when not chasing).
     */
    public DashAttackTask(Entity target, int priority, Vector2 speed, long cooldown, long dashTime) {
        this.target = target;
        this.priority = priority;
        this.speed = speed;
        this.cooldown = cooldown;
        this.dashTime = dashTime;
        lastDashTime = ServiceLocator.getTimeSource().getTime();
        physics = ServiceLocator.getPhysicsService().getPhysics();
        debugRenderer = ServiceLocator.getRenderService().getDebug();
    }

    @Override
    public void start() {
        super.start();
        movementTask = new MovementTask(target.getPosition(), new Vector2(0f, 0f));
        movementTask.create(owner);
        movementTask.start();

        this.owner.getEntity().getEvents().trigger("chaseStart");
    }

    @Override
    public void update() {
        movementTask.setTarget(target.getPosition());
        movementTask.update();
        if (ServiceLocator.getTimeSource().getTime() - lastDashTime > cooldown + dashTime) {
            movementTask.setSpeed(new Vector2(5f, 5f)); // Dash
            lastDashTime = ServiceLocator.getTimeSource().getTime();
        } else if (ServiceLocator.getTimeSource().getTime() - lastDashTime > dashTime){
            movementTask.setSpeed(new Vector2(0f, 0f)); // Stand still
        }
        if (movementTask.getStatus() != Status.ACTIVE) {
            movementTask.start();
        }
    }

    @Override
    public void stop() {
        super.stop();
        movementTask.stop();
    }

    @Override
    public int getPriority() {
        // Only fast chase if the player is visible to the enemy
        if (isTargetVisible()) {
            return priority;
        }
        return -1;
    }

    private boolean isTargetVisible() {
        Vector2 from = owner.getEntity().getCenterPosition();
        Vector2 to = target.getCenterPosition();

        // If there is an obstacle in the path to the player, not visible.
        if (physics.raycast(from, to, PhysicsLayer.OBSTACLE, hit)) {
            debugRenderer.drawLine(from, hit.point);
            return false;
        }
        debugRenderer.drawLine(from, to);
        return true;
    }
}
