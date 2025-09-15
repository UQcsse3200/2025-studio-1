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
import com.csse3200.game.services.GameTime;

/**
 * Ground slow-chase for GhostGPT: set X only; Box2D gravity handles Y.
 * Not using MovementTask because it sets both X&Y (makes flying). Refer: Box2D manual (forces/impulses).
 */
public class GPTGroundSlowChaseTask extends DefaultTask implements PriorityTask {
  private final Entity target;
  private final int priority;
  private final float speedX;
  private final PhysicsEngine physics;
  private final DebugRenderer debugRenderer;
  private final RaycastHit hit = new RaycastHit();
  private final RaycastHit jumpHit = new RaycastHit();
  private PhysicsComponent physicsComponent;
  // Jump mechanics
  private final GameTime timeSource;
  private float timeSinceLastJump = 0f;
  private final float jumpCooldown = 1.2f; // seconds between jumps
  private final float obstacleCheckDistance = 0.6f; // horizontal ray distance to look for obstacle
  private final float jumpImpulse = 15f; // upward impulse (scaled by mass)

  /**
   * @param target player entity to chase
   * @param priority task priority when chasing
   * @param speed horizontal speed (units per second)
   */
  public GPTGroundSlowChaseTask(Entity target, int priority, float speed) {
    this.target = target;
    this.priority = priority;
    this.speedX = speed;
    this.physics = ServiceLocator.getPhysicsService().getPhysics();
    this.debugRenderer = ServiceLocator.getRenderService().getDebug();
    this.timeSource = ServiceLocator.getTimeSource();
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

    // Gravity note: we DO NOT touch Y; Box2D integrates gravity + collisions for us.
    float dx = target.getPosition().x - owner.getEntity().getPosition().x;
    float dirX = Math.signum(dx);
    float desiredVx = dirX * speedX; // target horizontal speed; units/sec

    // Impulse to reach target vx in one step: Jx = m * (vx_des - vx_cur). (Impulse = Δmomentum)
    // Refs: Box2D manual (Forces/Impulses), iForce2D intro to forces, Wikipedia: Impulse (physics)
    // https://box2d.org/files/Box2D_Manual.pdf  https://www.iforce2d.net/b2dtut/force  https://en.wikipedia.org/wiki/Impulse_(physics)
    float currentVx = body.getLinearVelocity().x;
    float impulseX = (desiredVx - currentVx) * body.getMass();
    body.applyLinearImpulse(new Vector2(impulseX, 0f), body.getWorldCenter(), true);

    attemptJump(dirX, body);
  }

  private void attemptJump(float dirX, Body body) {
    timeSinceLastJump += timeSource.getDeltaTime();
    if (timeSinceLastJump < jumpCooldown) return; // still on cooldown
    if (Math.abs(body.getLinearVelocity().y) > 0.05f) return; // already moving vertically -> not grounded enough
    if (dirX == 0f) return; // no horizontal intent

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
    debugRenderer.drawLine(from, to); // visualize trigger ray (optional)
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
