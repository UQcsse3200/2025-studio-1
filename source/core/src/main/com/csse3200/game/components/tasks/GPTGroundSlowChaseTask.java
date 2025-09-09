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

/**
 * Ground-based slow chase task for GhostGPT. Only applies horizontal movement so gravity
 * can act on the enemy naturally. Active only when the player is NOT visible (mirrors slow chase logic).
 */
public class GPTGroundSlowChaseTask extends DefaultTask implements PriorityTask {
  private final Entity target;
  private final int priority;
  private final float speedX;
  private final PhysicsEngine physics;
  private final DebugRenderer debugRenderer;
  private final RaycastHit hit = new RaycastHit();
  private PhysicsComponent physicsComponent;

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

    /**
     * Why not use MovementTask?
     * - MovementTask makes enemies float/fly by setting both X and Y velocity.
     * - For ground enemies, we want them to walk, fall, and be affected by gravity.
     * - By controlling only X and letting physics handle Y, enemies move naturally on platforms.
     */

    float dx = target.getPosition().x - owner.getEntity().getPosition().x;
    float dirX = Math.signum(dx);
    float desiredVx = dirX * speedX; // target horizontal speed; units/sec
    /**
     * Horizontal impulse:
     *   impulseX = (desiredVx - currentVx) * mass
     *   Physics meaning:
     *     J = m * Δv  (impulse equals change in momentum). We want an immediate correction from the
     *     current horizontal velocity to our target horizontal velocity (desiredVx). So we compute
     *     the required Δv = (desiredVx - currentVx) and multiply by mass to obtain the impulse J.
     *   We keep the Y component zero so gravity + collisions remain untouched.
     *   References:
     *     - https://www.iforce2d.net/b2dtut/force
     *     - http://www.phys.uni.torun.pl/~jacek/dydaktyka/modsym/studenci/2015-2016/box2d.org/box2d_manual_v2.3.0.pdf
     *     (see "Forces and Impulses" section)
     *     - https://en.wikipedia.org/wiki/Impulse_(physics)
    */

    // Similar to how player's movement is handled in PlayerActions.java
    float currentVx = body.getLinearVelocity().x;
    float impulseX = (desiredVx - currentVx) * body.getMass();
    body.applyLinearImpulse(new Vector2(impulseX, 0f), body.getWorldCenter(), true);
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
