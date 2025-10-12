package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.components.enemy.ProjectileLauncherComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

public abstract class GPTGroundChaseTask extends DefaultTask implements PriorityTask {
    protected Entity target;
    protected int priority;
    protected float speedX;
    protected PhysicsEngine physics;
    protected DebugRenderer debugRenderer;
    protected RaycastHit hit = new RaycastHit();
    protected RaycastHit jumpHit = new RaycastHit();
    protected ProjectileLauncherComponent projectileLauncher;
    protected Entity shooter;
    protected float firingCooldown; // seconds
    protected GameTime timeSource;
    protected float jumpCooldown; // faster enemies jump a bit more often
    protected float obstacleCheckDistance; // look a tad further ahead
    protected float jumpImpulse; // upward impulse (scaled by mass)
    protected PhysicsComponent physicsComponent;
    protected float currentCooldown; // starts ready to fire
    protected float timeSinceLastJump = 0f;

    /**
     * @param target   player entity to chase
     * @param priority task priority when chasing
     * @param speed    horizontal speed (units per second)
     */
    public GPTGroundChaseTask(Entity target, int priority, float speed) {
        this.target = target;
        this.priority = priority;
        this.speedX = speed;
        this.physics = ServiceLocator.getPhysicsService().getPhysics();
        this.debugRenderer = ServiceLocator.getRenderService().getDebug();
        this.timeSource = ServiceLocator.getTimeSource();
    }

    @Override
    public void update() {
        if (target == null || physicsComponent == null) return;
        Body body = physicsComponent.getBody();

        // Gravity integration handled by physics world; don't modify Y here.
        float dx = target.getPosition().x - owner.getEntity().getPosition().x;
        float dirX = Math.signum(dx);
        float impulseX = getImpulseX(dirX, body);
        body.applyLinearImpulse(new Vector2(impulseX, 0f), body.getWorldCenter(), true);

        attemptJump(dirX, body);
        performChaseAction();
    }

    private float getImpulseX(float dirX, Body body) {
        float desiredVx = dirX * speedX; // target horizontal speed

        // Impulse to reach target vx in one step: Jx = m * (vx_des - vx_cur). (Impulse = Δmomentum)
        // Refs: Box2D manual (Forces/Impulses), iForce2D intro to forces, Wikipedia: Impulse (physics)
        // https://box2d.org/files/Box2D_Manual.pdf  https://www.iforce2d.net/b2dtut/force  https://en.wikipedia.org/wiki/Impulse_(physics)
        float currentVx = body.getLinearVelocity().x;
        float impulseX = (desiredVx - currentVx) * body.getMass();
        return impulseX;
    }

    /**
     * A method to be called in the event of a chase.
     * Does nothing as base implementation.
     */
    protected void performChaseAction() {
    }

    private void attemptJump(float dirX, Body body) {
        timeSinceLastJump += timeSource.getDeltaTime();
        if (timeSinceLastJump < jumpCooldown) return; // still on cooldown
        if (Math.abs(body.getLinearVelocity().y) > 0.05f) return; // already moving vertically -> not grounded enough
        if (dirX == 0f) return;

        Vector2 from = owner.getEntity().getCenterPosition();
        Vector2 to = new Vector2(from.x + dirX * obstacleCheckDistance, from.y);

        // Raycast ahead at center height for an obstacle
        if (!physics.raycast(from, to, PhysicsLayer.OBSTACLE, jumpHit)) {
            return; // nothing to jump over
        }

        // Simple jump: apply vertical impulse
        float impulseY = body.getMass() * jumpImpulse;
        body.applyLinearImpulse(new Vector2(0f, impulseY), body.getWorldCenter(), true);
        timeSinceLastJump = 0f;
        debugRenderer.drawLine(from, to);
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

    public abstract int getPriority();

    protected boolean isTargetVisible() {
        return TaskUtils.isVisible(owner.getEntity(), target, physics, debugRenderer, hit);
    }
}