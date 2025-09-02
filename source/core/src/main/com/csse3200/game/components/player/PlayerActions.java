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
import com.csse3200.game.components.StaminaComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.utils.Timer;

/**
 * Action component for interacting with the player. Player events should be initialised in create()
 * and when triggered should call methods within this class.
 */
public class PlayerActions extends Component {
  // Components
  private StaminaComponent stamina;
  private PhysicsComponent physicsComponent;

  // Added camera variable to allow for entities to spawn in world coordinates instead of
  // screen coordinates.
  private Camera camera;

  // Movement Constants
  private static final Vector2 MAX_SPEED = new Vector2(3f, 3f);
  private static final Vector2 CROUCH_SPEED = new Vector2(1.5f, 3f);
  private static final Vector2 SPRINT_SPEED = new Vector2(7f, 3f);
  private static final Vector2 JUMP_VELOCITY = new Vector2(0f, 30f);
  private static final Vector2 DASH_SPEED = new Vector2(20f, 9.8f);
  private static final float DASH_DURATION = 0.1f;
  private int DASH_COOLDOWN = 15; // hundredths of a second (1.5s)

  // Stamina Costs
  private static final int DASH_COST = 30;
  private static final int DOUBLE_JUMP_COST = 10;
  private static final int SPRINT_COST = 1;


  // Jumping Limits
  private static final int MAX_JUMPS = 2;
  private static final long JUMP_COOLDOWN_MS = 300;

  // Internal variables to manage movement
  private Vector2 walkDirection = Vector2.Zero.cpy();
  private boolean moving = false;
  private boolean sprinting = false;
  private boolean facingRight = true;
  private boolean dashing = false;
  private boolean crouching = false;
  private boolean grounded = true;

  // Ability cooldowns / counters
  private int dashCooldown = 0;
  private int jumpsLeft = MAX_JUMPS;
  private long lastJumpTime = 0; // timestamp of last ground jump


  // Tracks the last integer stamina value we pushed to UI to avoid redundant events
  private float timeSinceLastAttack = 0;


  @Override
  public void create() {

    physicsComponent = entity.getComponent(PhysicsComponent.class);
    stamina = entity.getComponent(StaminaComponent.class);

    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);
    entity.getEvents().addListener("attack", this::attack);
    entity.getEvents().addListener("shoot", this::shoot);
    entity.getEvents().addListener("jumpAttempt", this::jump);
    entity.getEvents().addListener("sprintAttempt", this::sprintAttempt);
    entity.getEvents().addListener("dashAttempt", this::dash);
    entity.getEvents().addListener("crouchAttempt", this::crouchAttempt);
    entity.getEvents().addListener("crouchStop", () -> crouching = false);
    entity.getEvents().addListener("sprintStart", this::startSprinting);
    entity.getEvents().addListener("sprintStop", this::stopSprinting);

    Array<Entity> entities = ServiceLocator.getEntityService().getEntities();
    for (Entity entity: entities) {
      if (entity.getComponent(CameraComponent.class) != null) {
        camera = entity.getComponent(CameraComponent.class).getCamera();
      }
    }
  }

  @Override
  public void update() {
    if (moving || dashing) {
      // An event changing the player's location occurred
      updateSpeed();
    }

    if (touchingGround()) {
      updateGrounded();

    } else {
      updateMidair();
    }

    timeSinceLastAttack += ServiceLocator.getTimeSource().getDeltaTime();
  }

  private void updateGrounded() {
    if (!grounded) {
      setGrounded(true);
      entity.getEvents().trigger("groundTouched", walkDirection);
    }

    jumpsLeft = MAX_JUMPS;
  }

  private void updateMidair() {
    if (grounded) {
      setGrounded(false);
      entity.getEvents().trigger("groundLeft", walkDirection);
    }

    crouching = false;
  }

  private void updateSpeed() {
    Body body = physicsComponent.getBody();
    Vector2 velocity = body.getLinearVelocity();

    if (walkDirection.x > 0) {
      facingRight = true;
    } else if (walkDirection.x < 0) {
      facingRight = false;
    }

    float impulseX = getImpulseX(velocity, body);
    body.applyLinearImpulse(new Vector2(impulseX, 0f), body.getWorldCenter(), true);
  }

  private float getImpulseX(Vector2 velocity, Body body) {
    float maxX;
    float targetVx;

    if (dashing) {
      maxX = (facingRight) ? DASH_SPEED.x : -1 * DASH_SPEED.x;
      targetVx = maxX;
    } else {
      if (crouching && touchingGround()) {
        maxX = CROUCH_SPEED.x;
      } else {
        boolean allowSprint = (
                sprinting && touchingGround() && stamina.hasStamina(SPRINT_COST)
        );
        maxX = allowSprint ? SPRINT_SPEED.x : MAX_SPEED.x;
      }
      targetVx = walkDirection.x * maxX;
    }

    // impulse = (desiredVel - currentVel) * mass
    return (targetVx - velocity.x) * body.getMass();
  }



  /**
   * Moves the player towards a given direction.
   *
   * @param direction direction to move in
   */
  void walk(Vector2 direction) {
    moving = true;
    stamina.setMoving(true);
    this.walkDirection = direction;
    facingRight = this.walkDirection.x > 0;

    entity.getEvents().trigger("move", facingRight);
    entity.getEvents().trigger("walkAnimate");
  }

  /**
   * Stops the player from walking.
   */
  void stopWalking() {
    if (dashing) {
      return;
    }
    this.walkDirection = Vector2.Zero.cpy();
    updateSpeed();
    moving = false;
    stamina.setMoving(false);
  }

  /**
   * Adds an upwards vertical force to the player.
   */
  void jump() {
    long currentTime = System.currentTimeMillis();
    Body body = physicsComponent.getBody();

    if (dashing) {
      return;
    }

    if (jumpsLeft > 0) {
      boolean isGroundJump = (jumpsLeft == MAX_JUMPS); // first jump
      boolean withinCooldown = (currentTime - lastJumpTime) > JUMP_COOLDOWN_MS;
      if ((!isGroundJump || touchingGround())
              && (!isGroundJump || withinCooldown)) {
        if(!isGroundJump) {
          if (!stamina.trySpend(DOUBLE_JUMP_COST)) {
            return;
          }
        }

        entity.getEvents().trigger("jump");
        body.applyLinearImpulse(JUMP_VELOCITY, body.getWorldCenter(), true);
        jumpsLeft--;
        lastJumpTime = currentTime;
    }
  }
}

  void sprintAttempt() {
    if (!crouching && stamina.hasStamina(SPRINT_COST)) {
      sprinting = true;
      stamina.setSprinting(true);
      entity.getEvents().trigger("sprintStart");
    }
  }

  void dash() {
    if (dashCooldown == 0 && stamina.trySpend(DASH_COST)) {
      if (crouching && touchingGround()) {
        entity.getEvents().trigger("slide");
      } else {
        entity.getEvents().trigger("dash");
      }
      dashCooldown = DASH_COOLDOWN;
      dashing = true;
      dashDuration();
      dashCooldown();
    }
  }

  void dashDuration() {
    Timer.schedule(new Timer.Task() {
      @Override
      public void run() {
        dashing = false;
        grounded = true; // Set grounded as true so set to falling afterwards
      }
    }, DASH_DURATION); // seconds
  }

  void dashCooldown() {
    Timer.schedule(new Timer.Task() {
      @Override
      public void run() {
        if (dashCooldown > 0) {
          dashCooldown--;
          dashCooldown(); // Reschedule until 0
        }
      }
    }, 0.1f); // every 0.1 seconds
  }

  void crouchAttempt() {
    if (touchingGround()) {
      entity.getEvents().trigger("crouchStart");
      crouching = true;
    }
  }

  void setGrounded(boolean grounded) {
    this.grounded = grounded;
    this.stamina.setGrounded(grounded);
  }

  /**
   * Checks if player is touching ground (ie. y velocity = 0)
   * @return if player is touching ground or not
   */
  boolean touchingGround() {
    return (physicsComponent.getBody().getLinearVelocity().y == 0f);
  }

  void startSprinting() {
    sprinting = true;
    stamina.setSprinting(true);
  }

  void stopSprinting() {
    sprinting = false;
    stamina.setSprinting(false);
  }

  /**
   * Cheatcode: infinite dashes
   */
  public void infDash() {
    this.DASH_COOLDOWN = 0;
  }

  /**
   * Cheatcode: infinite jumps
   */
  public void infJumps() {
    this.jumpsLeft = 9999;
  }

  /**
   * Cheatcode: infinite stamina
   */
  public void infStamina() {
    stamina.setInfiniteStamina(true);
  }

  /**
   * Fires a bullet from the player at wherever they click
   */

  void shoot() {

    float coolDown = entity.getComponent(CombatStatsComponent.class).getCoolDown();
    if (this.timeSinceLastAttack < coolDown) {
      return;
    }

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

    timeSinceLastAttack = 0;
  }

  /**
   * Makes the player melee attack.
   */
  void attack() {

    float coolDown = entity.getComponent(CombatStatsComponent.class).getCoolDown();
    if (this.timeSinceLastAttack < coolDown) {
      return;
    }

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
    timeSinceLastAttack = 0;
  }
}
