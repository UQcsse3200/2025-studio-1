package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.components.enemy.ProjectileLauncherComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.projectiles.ProjectileConfig;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/** Chases a target entity indefinitely and the entity being visible causes a speed increase
 *  CAN have added functionality to launch projectiles at the player too if wanted. */
public class GPTFastChaseTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private final int priority;
    private final Vector2 speed;
    private final PhysicsEngine physics;
    private final DebugRenderer debugRenderer;
    private final RaycastHit hit = new RaycastHit();
    private MovementTask movementTask;

    // Projectile configurations
    private ProjectileLauncherComponent projectileLauncher = null;
    private GameTime timeSource = null;
    private final float firingCooldown = 3f;
    private float currentCooldown = 3f;
    private Entity shooter = null;

    /**
     * @param target The entity to chase.
     * @param priority Task priority when chasing (0 when not chasing).
     * @param speed The speed at which the enemy will chase the player
     */
    public GPTFastChaseTask(Entity target, int priority, Vector2 speed) {
        this.target = target;
        this.priority = priority;
        this.speed = speed;
        physics = ServiceLocator.getPhysicsService().getPhysics();
        debugRenderer = ServiceLocator.getRenderService().getDebug();
    }

    /**
     * @param target The entity to chase.
     * @param priority Task priority when chasing (0 when not chasing).
     * @param speed The speed at which the enemy will chase the player
     * @param projectileLauncher the projectile launcher component used to launch projectiles at the player
     * @param shooter the enemy that is shooting the projectiles
     */
    public GPTFastChaseTask(Entity target, int priority, Vector2 speed,
                            ProjectileLauncherComponent projectileLauncher, Entity shooter) {
        this.target = target;
        this.priority = priority;
        this.speed = speed;
        physics = ServiceLocator.getPhysicsService().getPhysics();
        debugRenderer = ServiceLocator.getRenderService().getDebug();

        // Projectile launcher
        this.projectileLauncher = projectileLauncher;
        timeSource = ServiceLocator.getTimeSource();
        this.shooter = shooter;
    }

    @Override
    public void start() {
        super.start();
        movementTask = new MovementTask(target.getPosition(), speed);
        movementTask.create(owner);
        movementTask.start();

        this.owner.getEntity().getEvents().trigger("chaseStart");
    }

    @Override
    public void update() {
        movementTask.setTarget(target.getPosition());
        movementTask.update();
        if (movementTask.getStatus() != Status.ACTIVE) {
            movementTask.start();
        }

        FireLasers();
    }

    /**
     * If there is a projectile launcher present, fire lasers. If not, this does nothing.
     */
    public void FireLasers() {
        // Projectile launcher related
        if (isTargetVisible() && projectileLauncher != null) {
            currentCooldown += timeSource.getDeltaTime();

            if (currentCooldown >= firingCooldown) {
                currentCooldown = currentCooldown % firingCooldown;

                Vector2 dirToFire = new Vector2(target.getPosition().x - shooter.getPosition().x,
                        target.getPosition().y - shooter.getPosition().y);

                projectileLauncher.FireProjectile(dirToFire,
                        new Vector2(0.2f, 0.8f), new Vector2(0.5f, 0.5f));
            }
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
