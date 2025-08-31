package com.csse3200.game.components.player;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.csse3200.game.components.Component;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.services.ServiceLocator;
import com.badlogic.gdx.utils.Timer;

/**
 * Action component for interacting with the player. Player events should be initialised in create()
 * and when triggered should call methods within this class.
 */
public class PlayerActions extends Component {
  private static final Vector2 MAX_SPEED = new Vector2(3f, 3f); // Metres per second
  private static final Vector2 CROUCH_SPEED = new Vector2(1.5f, 3f);
  private static final Vector2 SPRINT_SPEED = new Vector2(7f, 3f);
  private static final Vector2 JUMP_VELOCITY = new Vector2(0f, 120f);
  private static final Vector2 DASH_SPEED = new Vector2(20f, 9.8f);
  private int DASH_COOLDOWN = 15; // In hundredths of a second so equals 1.5 seconds

  private PhysicsComponent physicsComponent;
  private Vector2 walkDirection = Vector2.Zero.cpy();
  private boolean moving = false;
  private boolean sprinting = false; // If 'Left Shift' is held
  private boolean facingRight = true;
  private boolean dashing = false;
  private int dashCooldown = 0;
  private boolean crouching = false;

  private static final int MAX_JUMPS = 2; // allow 1 normal jump + 1 double jump
  private int jumpsLeft = MAX_JUMPS;
  private long lastJumpTime = 0; // timestamp of last ground jump
  private static final long JUMP_COOLDOWN_MS = 300; // 300ms between jumps
  private static final float DASH_DURATION = 0.1f;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);
    entity.getEvents().addListener("attack", this::attack);
    entity.getEvents().addListener("jump", this::jump);
    entity.getEvents().addListener("sprintAttempt", this::sprintAttempt);
    entity.getEvents().addListener("sprintStop",  () -> sprinting = false);
    entity.getEvents().addListener("dashAttempt", this::dash);
    entity.getEvents().addListener("crouchAttempt", this::crouchAttempt);
    entity.getEvents().addListener("crouchStop", () -> crouching = false);
  }

  @Override
  public void update() {
    if (moving || dashing) {
      updateSpeed();
    }

    Body body = physicsComponent.getBody();
    if (body.getLinearVelocity().y < 0f) {
      jumpsLeft = MAX_JUMPS;
    }
  }

  private void updateSpeed() {
    Body body = physicsComponent.getBody();
    Vector2 velocity = body.getLinearVelocity();

    if (walkDirection.x > 0) {
      facingRight = true;
    } else if (walkDirection.x < 0) {
      facingRight = false;
    }

    boolean hasDir = !walkDirection.isZero(0.0001f);
    float maxX;
    float targetVx;

    if (dashing) {
      maxX = (facingRight) ? DASH_SPEED.x : -1 * DASH_SPEED.x;
      targetVx = maxX;
    } else {
      if (crouching) {
        maxX = CROUCH_SPEED.x;
      } else {
        maxX = (sprinting && hasDir) ? SPRINT_SPEED.x : MAX_SPEED.x;
      }
      targetVx = walkDirection.x * maxX;
    }


    float impulseX = (targetVx - velocity.x) * body.getMass();
    body.applyLinearImpulse(new Vector2(impulseX, 0f), body.getWorldCenter(), true);
  }

  /**
   * Moves the player towards a given direction.
   *
   * @param direction direction to move in
   */
  void walk(Vector2 direction) {
    moving = true;
    this.walkDirection = direction;
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
      if (!isGroundJump || (currentTime - lastJumpTime) > JUMP_COOLDOWN_MS) {
        body.applyLinearImpulse(JUMP_VELOCITY, body.getWorldCenter(), true);
        jumpsLeft--;
        lastJumpTime = currentTime;
      }
    }
  }

  void sprintAttempt() {
    if (!crouching) {
      sprinting = true;
      entity.getEvents().trigger("sprintStart");
    }
  }

  void dash() {
    if (dashCooldown == 0) {
      if (crouching) { // Need to add grounded check here as well
        entity.getEvents().trigger("roll", facingRight); // Different animation
      } else {
        entity.getEvents().trigger("dash", facingRight); // To be used for animations or invulnerability checks
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
        if (!moving) {
          stopWalking();
        } else if(!sprinting){
          entity.getEvents().trigger("walk", walkDirection);
        }
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
    // Return if grounded TBA
    entity.getEvents().trigger("crouchStart");
    crouching = true;
  }


  /**
   * Makes the player attack.
   */
  void attack() {
    Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
    attackSound.play();
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
}
