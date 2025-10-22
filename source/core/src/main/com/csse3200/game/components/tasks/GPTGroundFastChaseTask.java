package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.components.enemy.ProjectileLauncherComponent;
import com.csse3200.game.entities.Entity;

/**
 * Ground fast-chase for GhostGPT: drive X only; Box2D gravity/collisions control Y.
 * Not using MovementTask (it sets X and Y -> flying). See Box2D manual on Forces/Impulses.
 */
public class GPTGroundFastChaseTask extends GPTGroundChaseTask {

    /**
     * Fast chase without projectiles
     */
    public GPTGroundFastChaseTask(Entity target, int priority, float speed) {
        this(target, priority, speed, null, null, 3f, 3f);
    }

    /**
     * Fast chase with projectile support
     */
    public GPTGroundFastChaseTask(Entity target, int priority, float speed,
                                  ProjectileLauncherComponent projectileLauncher, Entity shooter,
                                  float firingCooldown, float currentCooldown) {
        super(target, priority, speed);
        this.projectileLauncher = projectileLauncher;
        this.shooter = shooter;
        this.firingCooldown = firingCooldown;
        this.currentCooldown = currentCooldown;
        this.jumpCooldown = 0.9f;
        this.obstacleCheckDistance = 0.7f; // look a tad further ahead
        this.jumpImpulse = 15f; // slightly stronger jump
    }

    /**
     * Fast chase with projectile support + variant behaviour
     */
    public GPTGroundFastChaseTask(Entity target, int priority, float speed,
                                  ProjectileLauncherComponent projectileLauncher, Entity shooter,
                                  float firingCooldown, float currentCooldown, int burstAmount,
                                  float angleDifferences) {
        super(target, priority, speed);
        this.projectileLauncher = projectileLauncher;
        this.shooter = shooter;
        this.firingCooldown = firingCooldown;
        this.currentCooldown = currentCooldown;
        this.jumpCooldown = 0.9f;
        this.obstacleCheckDistance = 0.7f; // look a tad further ahead
        this.jumpImpulse = 15f; // slightly stronger jump
        this.burstAmount = burstAmount;
        this.angleDifferencesInBurst = angleDifferences;
        this.isVariant = true;
    }

    @Override
    protected void triggerStartEvent() {
        owner.getEntity().getEvents().trigger("chaseStart");
    }

    @Override
    protected void performChaseAction() {
        fireProjectiles();
    }

    private void fireProjectiles() {
        if (projectileLauncher == null) return;
        if (!isTargetVisible()) return;

        currentCooldown += timeSource.getDeltaTime();
        if (currentCooldown >= firingCooldown) {
            currentCooldown %= firingCooldown;
            Vector2 dirToFire = new Vector2(target.getPosition().x - shooter.getPosition().x,
                    target.getPosition().y - shooter.getPosition().y);

            // If a variant, fire more projectiles!
            if (isVariant) {
                projectileLauncher.fireProjectileMultishot(burstAmount, angleDifferencesInBurst, dirToFire,
                        new Vector2(0.2f, 0.8f), new Vector2(0.5f, 0.5f));
            } else {
                projectileLauncher.fireProjectile(dirToFire,
                        new Vector2(0.2f, 0.8f), new Vector2(0.5f, 0.5f));
            }
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
