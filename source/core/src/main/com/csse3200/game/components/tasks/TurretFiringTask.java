package com.csse3200.game.components.tasks;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.ai.tasks.DefaultTask;
import com.csse3200.game.ai.tasks.PriorityTask;
import com.csse3200.game.ai.tasks.Task;
import com.csse3200.game.components.enemy.ProjectileLauncherComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.raycast.RaycastHit;
import com.csse3200.game.rendering.DebugRenderer;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

public class TurretFiringTask extends DefaultTask implements PriorityTask {
    private final Entity target;
    private final int priority;
    private final float speedX;
    private final PhysicsEngine physics;
    private final DebugRenderer debugRenderer;
    private final RaycastHit hit = new RaycastHit();
    private final RaycastHit jumpHit = new RaycastHit();
    private PhysicsComponent physicsComponent;

    // Projectile firing
    private final ProjectileLauncherComponent projectileLauncher;
    private final Entity shooter;
    private final float firingCooldown; // seconds
    private float currentCooldown; // starts ready to fire
    private final GameTime timeSource;
    private int burstAmount;
    private float burstCooldown;

    public TurretFiringTask(Entity target, int priority,
                                  ProjectileLauncherComponent projectileLauncher, Entity shooter,
                                  float firingCooldown, float currentCooldown, int burstAmount,
                                  float burstCooldown) {
        this.target = target;
        this.priority = priority;
        this.speedX = 0f;
        this.projectileLauncher = projectileLauncher;
        this.shooter = shooter;
        this.firingCooldown = firingCooldown;
        this.currentCooldown = currentCooldown;
        this.burstAmount = burstAmount;
        this.burstCooldown = burstCooldown;
        this.physics = ServiceLocator.getPhysicsService().getPhysics();
        this.debugRenderer = ServiceLocator.getRenderService().getDebug();
        this.timeSource = ServiceLocator.getTimeSource();
    }

    @Override
    public void start() {
        super.start();
        physicsComponent = owner.getEntity().getComponent(PhysicsComponent.class);
        owner.getEntity().getEvents().trigger("chaseStart");
    }

    @Override
    public void update() {
        if (target == null || physicsComponent == null) return;
        Body body = physicsComponent.getBody();

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
    public void stop() {
        super.stop();
    }

    @Override
    public int getPriority() {
        if (isTargetVisible()) {
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
