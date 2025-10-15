package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.enemy.ProjectileLauncherComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Stationary entity with firing capabilities when it sees enemy on sight.  Does burst firing.
 */
public class TurretFiringTask extends TurretTask {
    private final ProjectileLauncherComponent projectileLauncher;
    private final Entity shooter;
    private final float firingCooldown; // seconds
    private float currentCooldown; // starts ready to fire
    private final GameTime timeSource;
    private final int burstAmount;
    private final float burstCooldown;

    /**
     * Constructs the task
     *
     * @param target             The target to fire at
     * @param priority           The priority
     * @param projectileLauncher The projectile launcher component used to fire the projectiles
     * @param shooter            The entity firing the projectiles
     * @param firingCooldown     The firing cooldown between each burst.
     * @param currentCooldown    The current cooldown
     * @param burstAmount        The amount of projectiles to fire in one burst
     * @param burstCooldown      The cooldown between each fired projectile in a burst
     */
    public TurretFiringTask(Entity target, int priority,
                            ProjectileLauncherComponent projectileLauncher, Entity shooter,
                            float firingCooldown, float currentCooldown, int burstAmount,
                            float burstCooldown) {
        super(target, priority);
        this.projectileLauncher = projectileLauncher;
        this.shooter = shooter;
        this.firingCooldown = firingCooldown;
        this.currentCooldown = currentCooldown;
        this.burstAmount = burstAmount;
        this.burstCooldown = burstCooldown;
        this.timeSource = ServiceLocator.getTimeSource();
    }

    @Override
    public void triggerStartEvent() {
        owner.getEntity().getEvents().trigger("chaseStart");
    }

    @Override
    public void performFiringAction() {
        fireProjectiles();
    }

    private void fireProjectiles() {
        if (projectileLauncher == null) return;
        if (!isTargetVisible()) return;

        currentCooldown += timeSource.getDeltaTime();
        if (currentCooldown >= firingCooldown) {
            currentCooldown %= firingCooldown;

            // Burst fire logic
            Timer.Task burstFireTask = new Timer.Task() {
                int currentCount = 0;

                @Override
                public void run() {
                    // An error would keep occurring with the physics server upon cleanup. Have to check that it no longer
                    // exists.
                    if (ServiceLocator.getPhysicsService() == null) {
                        cancel(); // stop task if physics no longer exists
                        return;
                    }

                    Vector2 dirToFire = new Vector2(target.getPosition().x - shooter.getPosition().x,
                            target.getPosition().y - shooter.getPosition().y);

                    projectileLauncher.fireProjectile(dirToFire, new Vector2(0.2f, 0.9f), new Vector2(0.5f, 0.5f));
                    currentCount++;
                    if (currentCount >= burstAmount) {
                        cancel();
                    }
                }
            };

            Timer.schedule(burstFireTask, 0f, burstCooldown);
        }
    }

    @Override
    public int getPriority() {
        if (isTargetVisible()) {
            return priority;
        }
        return -1;
    }
}
