package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Array;
import com.csse3200.game.components.CameraComponent;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.*;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.weapons.PistolConfig;
import com.csse3200.game.entities.factories.ProjectileFactory;
import com.csse3200.game.physics.PhysicsLayer;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.physics.components.PhysicsProjectileComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.utils.Timer;

/**
 * Component that handles all player actions including movement, jumping, sprinting,
 * dashing, crouching, attacking, and shooting.
 *
 * <p>Events related to the player should be initialized in {@link #create()},
 * and corresponding actions are triggered when these events occur.</p>
 */
public class PlayerActions extends Component {
  // Components
  private StaminaComponent stamina;
  private PhysicsComponent physicsComponent;

  // Movement Constants
  private static final Vector2 MAX_SPEED = new Vector2(3f, 3f);
  private static final Vector2 CROUCH_SPEED = new Vector2(1.5f, 3f);
  private static final Vector2 SPRINT_SPEED = new Vector2(7f, 3f);
  private static final Vector2 JUMP_VELOCITY = new Vector2(0f, 15f);
  private static final Vector2 DASH_SPEED = new Vector2(20f, 9.8f);
  private static final float DASH_DURATION = 0.1f;
  private int DASH_COOLDOWN = 15;

  // Stamina Costs
  private static final int DASH_COST = 30;
  private static final int DOUBLE_JUMP_COST = 10;
  private static final int SPRINT_COST = 1;

  // Jumping Limits
  private static final int MAX_JUMPS = 2;
  private static final long JUMP_COOLDOWN_MS = 300;

  // Internal movement state
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

  // Tracks time since last attack for cooldown purposes
  private float timeSinceLastAttack = 0;

  // Camera reference for world coordinates
  private Camera camera;

  /**
   * Initializes the component by setting required components and
   * registering event listeners for all player actions.
   */
  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    stamina = entity.getComponent(StaminaComponent.class);

    // Add event listeners
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);
    entity.getEvents().addListener("attack", this::attack);
    entity.getEvents().addListener("attack", this::weaponAnimation);
    entity.getEvents().addListener("shoot", this::shoot);
    entity.getEvents().addListener("jumpAttempt", this::jump);
    entity.getEvents().addListener("sprintAttempt", this::sprintAttempt);
    entity.getEvents().addListener("dashAttempt", this::dash);
    entity.getEvents().addListener("crouchAttempt", this::crouchAttempt);
    entity.getEvents().addListener("crouchStop", () -> crouching = false);
    entity.getEvents().addListener("sprintStart", this::startSprinting);
    entity.getEvents().addListener("sprintStop", this::stopSprinting);
    // Find camera from any entity with CameraComponent
    entity.getEvents().addListener("reload", this::reload);
    Array<Entity> entities = ServiceLocator.getEntityService().getEntities();
    for (Entity entity: entities) {
      if (entity.getComponent(CameraComponent.class) != null) {
        camera = entity.getComponent(CameraComponent.class).getCamera();

      }
    }
  }

  /**
   * Updates player movement, speed, and grounded status.
   * Called once per frame.
   */
  @Override
  public void update() {
    if(!Gdx.input.isKeyPressed(Input.Keys.A) && !Gdx.input.isKeyPressed(Input.Keys.D) && !dashing) {
      this.walkDirection.x = 0f;
      entity.getEvents().trigger("walkStop");
      if(!grounded) {
        entity.getEvents().trigger("groundLeft", walkDirection);
      }
    }
    if(!Gdx.input.isKeyPressed(Input.Keys.S) && crouching) {
      entity.getEvents().trigger("crouchStop");
    }
    if (moving || dashing) {
      updateSpeed();
    }

    if (touchingGround()) {
      updateGrounded();
    } else {
      updateMidair();
    }

    timeSinceLastAttack += ServiceLocator.getTimeSource().getDeltaTime();
  }

  /** Updates grounded state and triggers ground touched events. */
  private void updateGrounded() {
    if (!grounded) {
      setGrounded(true);
      entity.getEvents().trigger("groundTouched", walkDirection);
    }
    jumpsLeft = MAX_JUMPS;
  }

  /** Updates midair state and triggers ground left events. */
  private void updateMidair() {
    if (grounded) {
      setGrounded(false);
      entity.getEvents().trigger("groundLeft", walkDirection);
    }
    crouching = false;
  }

  /** Applies player movement based on current walk direction and velocity. */
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

  /** Calculates impulse to apply based on desired speed and current velocity. */
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
   * Moves the player in a given direction.
   *
   * @param direction Direction vector to move in
   */
  void walk(Vector2 direction) {
    moving = true;
    stamina.setMoving(true);
    this.walkDirection = direction;
    facingRight = this.walkDirection.x > 0;

    entity.getEvents().trigger("move", facingRight);
    entity.getEvents().trigger("walkAnimate");
  }

  /** Stops walking and updates speed and movement state. */
  void stopWalking() {
    if (dashing) {
      return;
    }
    this.walkDirection = Vector2.Zero.cpy();
    updateSpeed();
    moving = false;
    stamina.setMoving(false);
  }

  /** Adds vertical velocity to the player to jump if jumps remain. */
  void jump() {
    long currentTime = System.currentTimeMillis();
    Body body = physicsComponent.getBody();

    if (dashing) return;

    if (jumpsLeft > 0) {
      setGrounded(false);

      boolean isGroundJump = (jumpsLeft == MAX_JUMPS);
      boolean canJump = true;

      // Check cooldown for ground jumps
      if (isGroundJump) {
        if (!touchingGround()) canJump = false;
        if (currentTime - lastJumpTime < JUMP_COOLDOWN_MS) canJump = false;
      } else {
        if (!stamina.trySpend(DOUBLE_JUMP_COST)) return;
      }

      if (canJump) {
        entity.getEvents().trigger("jump");
        body.applyLinearImpulse(JUMP_VELOCITY, body.getWorldCenter(), true);
        jumpsLeft--;
        lastJumpTime = currentTime;
      }

    }
  }

  /** Attempts to start sprinting if stamina allows. */
  void sprintAttempt() {
    if (!crouching && stamina.hasStamina(SPRINT_COST)) {
      sprinting = true;
      stamina.setSprinting(true);
      entity.getEvents().trigger("sprintStart");
    }
  }

  /** Attempts to perform a dash if cooldown and stamina allow. */
  void dash() {
    if (dashCooldown == 0 && stamina.trySpend(DASH_COST)) {
      if (crouching && touchingGround()) {
        entity.getEvents().trigger("slide");
      } else {
        entity.getEvents().trigger("dash");
      }
      dashCooldown = DASH_COOLDOWN;
      dashing = true;
      stamina.setDashing(true);
      dashDuration();
      dashCooldown();
    }
  }

  /** Sets the duration of a dash, after which dashing stops. */
  void dashDuration() {
    Timer.schedule(new Timer.Task() {
      @Override
      public void run() {
        dashing = false;
        grounded = true;
        stamina.setDashing(false);
      }
    }, DASH_DURATION);
  }

  /** Handles dash cooldown by decrementing over time. */
  void dashCooldown() {
    Timer.schedule(new Timer.Task() {
      @Override
      public void run() {
        if (dashCooldown > 0) {
          dashCooldown--;
          dashCooldown();
        }
      }
    }, 0.1f);
  }

  /** Attempts to crouch if player is grounded. */
  void crouchAttempt() {
    if (touchingGround()) {
      entity.getEvents().trigger("crouchStart");
      crouching = true;
    }
  }

  /** Sets grounded state and updates stamina accordingly. */
  void setGrounded(boolean grounded) {
    this.grounded = grounded;
    this.stamina.setGrounded(grounded);
  }

  /**
   * Checks if player is touching the ground.
   *
   * @return true if y-velocity is zero
   */
  boolean touchingGround() {
    return (physicsComponent.getBody().getLinearVelocity().y == 0f);
  }

  /** Starts sprinting and updates stamina. */
  void startSprinting() {
    sprinting = true;
    stamina.setSprinting(true);
  }

  /** Stops sprinting and updates stamina. */
  void stopSprinting() {
    sprinting = false;
    stamina.setSprinting(false);
  }

  /** Cheat: infinite dashes. */
  public void infDash() {
    this.DASH_COOLDOWN = 0;
  }

  /** Cheat: infinite jumps. */
  public void infJumps() {
    this.jumpsLeft = 9999;
  }

  /** Cheat: infinite stamina. */
  public void infStamina() {
    stamina.setInfiniteStamina(true);
  }

  /** Fires a projectile towards the mouse cursor. */
  void shoot() {
    if (ServiceLocator.getTimeSource().isPaused())
        return;

    WeaponsStatsComponent weapon = getCurrentWeaponStats();
    if (weapon == null) {
      return;
    }

    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
    Entity gun = inventory.getCurrItem();

    MagazineComponent mag = gun.getComponent(MagazineComponent.class);
    // Check for cooldown, defaulting to zero if no current weapon
    float coolDown = weapon.getCoolDown();
    if (this.timeSinceLastAttack < coolDown || mag.reloading()) {
      return;
    }

    if (mag.getCurrentAmmo() <= 0) {
      Sound attackSound = ServiceLocator.getResourceService()
              .getAsset("sounds/shot_failed.mp3", Sound.class);
      attackSound.play();
      return;
    }


    Sound attackSound = ServiceLocator.getResourceService()
            .getAsset("sounds/laser_blast.mp3", Sound.class);
    attackSound.play();

    Entity bullet = ProjectileFactory.createPistolBullet(weapon);
    Vector2 origin = new Vector2(entity.getPosition());
    bullet.setPosition(new Vector2(origin.x - bullet.getScale().x / 2f + 2f,
            origin.y + 0.6f - bullet.getScale().y / 2f));
    com.csse3200.game.areas.GameArea area = ServiceLocator.getGameArea();
    if (area != null) {
      area.spawnEntity(bullet);
    } else {
      ServiceLocator.getEntityService().register(bullet);
    }

//    ServiceLocator.getEntityService().register(bullet);

    PhysicsProjectileComponent projectilePhysics = bullet.getComponent(PhysicsProjectileComponent.class);

    Vector3 destination = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
    projectilePhysics.fire(new Vector2(destination.x - origin.x, destination.y - origin.y), 5);


    mag.setCurrentAmmo(mag.getCurrentAmmo() - 1);

    timeSinceLastAttack = 0;
  }

  /** Performs a melee attack against nearby enemies. */
  void attack() {
      if (ServiceLocator.getTimeSource().isPaused())
          return;
    WeaponsStatsComponent weapon = getCurrentWeaponStats();
    float coolDown = weapon != null ? weapon.getCoolDown() : 0;
    if (this.timeSinceLastAttack < coolDown) return;

    Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
    attackSound.play();

    float attackRange = 3f;

    for (Entity enemy : ServiceLocator.getEntityService().getEntities()) {
      if (enemy != entity) {
        CombatStatsComponent enemyStats = enemy.getComponent(CombatStatsComponent.class);
        WeaponsStatsComponent attackStats = entity.getComponent(WeaponsStatsComponent.class);
        HitboxComponent enemyHitBox = enemy.getComponent(HitboxComponent.class);

        if (enemyStats != null && attackStats != null && enemyHitBox != null) {
          if (enemyHitBox.getLayer() == PhysicsLayer.NPC) {
            float distance = enemy.getCenterPosition().dst(entity.getCenterPosition());
            if (distance <= attackRange) {
              enemyStats.takeDamage(attackStats.getBaseAttack());
            }
          }
        }
      }
    }

    timeSinceLastAttack = 0;
  }

  public WeaponsStatsComponent getCurrentWeaponStats() {
    InventoryComponent inv = entity.getComponent(InventoryComponent.class);
    if (inv != null) {
      WeaponsStatsComponent curr = inv.getCurrItemStats();
      if (curr != null) return curr;
    }
    return entity.getComponent(WeaponsStatsComponent.class);
  }

  /**
   * Makes player reload their equipped weapon
   */
  void reload() {


    InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
    Entity equippedItem = inventory.getCurrItem();

    if (equippedItem != null) {
      MagazineComponent mag = equippedItem.getComponent(MagazineComponent.class);
      if (mag != null) {

        if (mag.reloading()) {

          return;
        }

        Sound reloadSound;

        if (mag.reload(entity)) {
          reloadSound = ServiceLocator.getResourceService()
                  .getAsset("sounds/reload.mp3", Sound.class);
        }

        else {
          reloadSound = ServiceLocator.getResourceService()
                  .getAsset("sounds/shot_failed.mp3", Sound.class);
        }

        reloadSound.play();
      }
    }
  }

  void weaponAnimation() {
    if (entity.getComponent(AnimationRenderComponent.class) != null) {
      entity.getEvents().trigger("anim");
      System.out.println("TRIGGERED");
    }
  }
}
