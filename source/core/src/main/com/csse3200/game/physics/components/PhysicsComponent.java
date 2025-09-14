package com.csse3200.game.physics.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.BodyUserData;
import com.csse3200.game.physics.PhysicsContactListener;
import com.csse3200.game.physics.PhysicsEngine;
import com.csse3200.game.services.ServiceLocator;

/**
 * Lets an entity be controlled by physics. Do not directly modify the position of a physics-enabled
 * entity. Instead, use forces to move it.
 *
 * <p>Entities with a PhysicsComponent will fire "collisionStart" and "collisionEnd" events. See
 * {@link PhysicsContactListener }
 */
public class PhysicsComponent extends Component {
  private static final float GROUND_FRICTION = 5f;
  private final PhysicsEngine physics;
  private Body body;
  private final BodyDef bodyDef = new BodyDef();

  /** Create a physics component with default settings. */
  public PhysicsComponent() {
    this(ServiceLocator.getPhysicsService().getPhysics());
  }

  /**
   * Create a physics component
   *
   * @param engine The physics engine to attach the component to
   */
  public PhysicsComponent(PhysicsEngine engine) {
    this.physics = engine;
    bodyDef.type = BodyType.DynamicBody;
    bodyDef.fixedRotation = true;
    bodyDef.linearDamping = GROUND_FRICTION;
    bodyDef.angle = 0f;
    bodyDef.active = false;
  }

  /**
   * Set body type
   *
   * @param bodyType body type, default = dynamic
   * @return self
   */
  public PhysicsComponent setBodyType(BodyType bodyType) {
    if (body != null) {
      body.setType(bodyType);
    } else {
      bodyDef.type = bodyType;
    }
    return this;
  }

  /**
   * Get the physics body.
   *
   * @return physics body if entity has been created, null otherwise.
   */
  public Body getBody() {
    return body;
  }

  @Override
  public void create() {
    if (body == null) {
      body = physics.createBody(bodyDef);
    }
    body.setTransform(entity.getPosition(), 0f);
    body.setActive(true);

    BodyUserData userData = new BodyUserData();
    userData.entity = this.entity;
    body.setUserData(userData);

    entity.getEvents().addListener("setPosition", (Vector2 pos) -> body.setTransform(pos, 0f));
  }

  /**
   * Entity position needs to be updated to match the new physics position. This should happen
   * before other updates, which may use the new position.
   */
  @Override
  public void earlyUpdate() {
    Vector2 bodyPos = body.getPosition();
    // Don't notify position changes due to physics
    entity.setPosition(bodyPos, false);
  }

  @Override
  public void dispose() {
      if (body != null) {
        // Destroy immediately on render thread to avoid overlap with next area's creations
        physics.destroyBody(body);
        body = null;
      }
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    body.setActive(enabled);
  }

  public enum AlignX {
    LEFT,
    CENTER,
    RIGHT
  }

  public enum AlignY {
    BOTTOM,
    CENTER,
    TOP
  }
}
