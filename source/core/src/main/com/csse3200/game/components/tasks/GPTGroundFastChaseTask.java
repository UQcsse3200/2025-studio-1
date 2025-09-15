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

/**
 * Ground fast-chase for GhostGPT: drive X only; Box2D gravity/collisions control Y.
 * Not using MovementTask (it sets X and Y -> flying). See Box2D manual on Forces/Impulses.
 */
public class GPTGroundFastChaseTask extends DefaultTask implements PriorityTask {
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

  // Jump mechanics (slightly faster cadence than slow chase)
  private float timeSinceLastJump = 0f;
  private final float jumpCooldown = 0.9f; // faster enemies jump a bit more often
  private final float obstacleCheckDistance = 0.7f; // look a tad further ahead
  private final float jumpImpulse = 15f; // slightly stronger jump

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
    this.target = target;
    this.priority = priority;
    this.speedX = speed;
    this.projectileLauncher = projectileLauncher;
    this.shooter = shooter;
    this.firingCooldown = firingCooldown;
    this.currentCooldown = currentCooldown;
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

    // Gravity integration handled by physics world; don't modify Y here.
    float dx = target.getPosition().x - owner.getEntity().getPosition().x;
    float dirX = Math.signum(dx);
    float desiredVx = dirX * speedX; // target horizontal speed

    // Impulse formula: Jx = m * (vx_des - vx_cur). We nudge X toward desired speed in one step.
    // Refs: https://box2d.org/files/Box2D_Manual.pdf  https://www.iforce2d.net/b2dtut/force  https://en.wikipedia.org/wiki/Impulse_(physics)
    float currentVx = body.getLinearVelocity().x;
    float impulseX = (desiredVx - currentVx) * body.getMass();
    body.applyLinearImpulse(new Vector2(impulseX, 0f), body.getWorldCenter(), true);

    attemptJump(dirX, body);
    fireProjectiles();
  }

  private void attemptJump(float dirX, Body body) {
    timeSinceLastJump += timeSource.getDeltaTime();
    if (timeSinceLastJump < jumpCooldown) return;
    if (Math.abs(body.getLinearVelocity().y) > 0.05f) return; // not grounded enough
    if (dirX == 0f) return;

    Vector2 from = owner.getEntity().getCenterPosition();
    Vector2 to = new Vector2(from.x + dirX * obstacleCheckDistance, from.y);

    if (!physics.raycast(from, to, PhysicsLayer.OBSTACLE, jumpHit)) {
      return; // nothing to jump
    }

    float impulseY = body.getMass() * jumpImpulse;
    body.applyLinearImpulse(new Vector2(0f, impulseY), body.getWorldCenter(), true);
    timeSinceLastJump = 0f;
    debugRenderer.drawLine(from, to);
  }

  private void fireProjectiles() {
    if (projectileLauncher == null) return;
    if (!isTargetVisible()) return;

    currentCooldown += timeSource.getDeltaTime();
    if (currentCooldown >= firingCooldown) {
      currentCooldown %= firingCooldown;
      Vector2 dirToFire = new Vector2(target.getPosition().x - shooter.getPosition().x,
              target.getPosition().y - shooter.getPosition().y);
      projectileLauncher.FireProjectile(dirToFire,
              new Vector2(0.2f, 0.8f), new Vector2(0.5f, 0.5f));
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
