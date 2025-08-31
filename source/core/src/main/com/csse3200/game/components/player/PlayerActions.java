package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.LightsaberConfig;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.ColliderComponent;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Action component for interacting with the player. Player events should be initialised in create()
 * and when triggered should call methods within this class.
 */
public class PlayerActions extends Component {
  private static final Vector2 MAX_SPEED = new Vector2(3f, 3f); // Metres per second

  private PhysicsComponent physicsComponent;
  private Vector2 walkDirection = Vector2.Zero.cpy();
  private boolean moving = false;
  /*
  Added camera variable to allow for entities to spawn in world coordinates instead of
  screen coordinates.
   */
  private Camera camera;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);
    entity.getEvents().addListener("attack", this::attack);
    entity.getEvents().addListener("shoot", this::shoot);
    Array<Entity> entities = ServiceLocator.getEntityService().getEntities();
    for (Entity entity: entities) {

      if (entity.getComponent(CameraComponent.class) != null) {
        camera = entity.getComponent(CameraComponent.class).getCamera();
      }
    }
  }

  @Override
  public void update() {
    if (moving) {
      updateSpeed();
    }
  }

  private void updateSpeed() {
    Body body = physicsComponent.getBody();
    Vector2 velocity = body.getLinearVelocity();
    Vector2 desiredVelocity = walkDirection.cpy().scl(MAX_SPEED);
    // impulse = (desiredVel - currentVel) * mass
    Vector2 impulse = desiredVelocity.sub(velocity).scl(body.getMass());
    body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
  }

  /**
   * Moves the player towards a given direction.
   *
   * @param direction direction to move in
   */
  void walk(Vector2 direction) {
    this.walkDirection = direction;
    moving = true;
  }

  /**
   * Stops the player from walking.
   */
  void stopWalking() {
    this.walkDirection = Vector2.Zero.cpy();
    updateSpeed();
    moving = false;
  }

  /**
   * Fires a bullet from the player at wherever they click
   */

  void shoot() {

    Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
    attackSound.play();

    Entity bullet = ProjectileFactory.createPistolBullet();
    Vector2 origin = new Vector2(entity.getPosition());
    bullet.setPosition(origin);
    ServiceLocator.getEntityService().register(bullet);

    PhysicsProjectileComponent projectilePhysics = bullet.
            getComponent(PhysicsProjectileComponent.class);

    Vector3 destination = camera.unproject(new Vector3(Gdx.input.getX(),
            Gdx.input.getY(), 0));

    projectilePhysics.fire(new Vector2(destination.x - origin.x,
            destination.y - origin.y), 5);

  }

  /**
   * Makes the player attack.
   */
  void attack() {
    Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
    attackSound.play();

    float attackRange = 3f; // CHANGE THIS

    for (Entity enemy : ServiceLocator.getEntityService().getEntities()) {
        if (enemy != entity) {
          CombatStatsComponent enemyStats = enemy.getComponent(CombatStatsComponent.class);
          CombatStatsComponent attackStats = entity.getComponent(CombatStatsComponent.class);
          HitboxComponent enemyHitBox = enemy.getComponent(HitboxComponent.class);
          if (enemyStats != null && attackStats != null
                  && enemyHitBox != null) {
            if (enemyHitBox.getLayer() == PhysicsLayer.NPC) {
                float distance = enemy.getCenterPosition().dst(entity.getCenterPosition());
                if (distance <= attackRange) {
                  System.out.println("TRYING TO HIT: " + enemy);
                  enemy.getComponent(CombatStatsComponent.class).hit(entity.getComponent(CombatStatsComponent.class));
                }
            }
          }
        }
    }
  }
}
