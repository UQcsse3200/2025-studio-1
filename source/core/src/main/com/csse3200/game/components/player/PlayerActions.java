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
  // Managers
  private Timer.Task staminaTask;
  private PhysicsComponent physicsComponent;

  // Movement Constants
  private static final float jumpImpulse = 120f;
  private static final Vector2 MAX_SPEED    = new Vector2(3f, 3f);
  private static final Vector2 CROUCH_SPEED = new Vector2(1.5f, 3f);
  private static final Vector2 SPRINT_SPEED = new Vector2(7f, 3f);
  private static final Vector2 JUMP_VELOCITY = new Vector2(0f, 120f);
  private static final Vector2 DASH_SPEED = new Vector2(20f, 9.8f);
  private static final float DASH_DURATION = 0.1f;
  private int DASH_COOLDOWN = 15; // hundredths of a second (1.5s)

  // Stamina Constants
  private static final int MAX_STAMINA = 100;
  private static final int INITIAL_STAMINA = 100;
  private static final float SPRINT_DRAIN_PER_SEC = 15f; // whilst moving or jumping normally
  private static final float SPRINT_REGEN_PER_SEC = 10f; // stamina/sec when not spending
  private static final int DASH_COST = 30; // stamina instantly consumed on dash
  private static final int DOUBLE_JUMP_COST = 10; // stamina consumed on air jump
  private static final long STAMINA_REGEN_DELAY_MS = 800; // time between last spend to regen
  private static final float STAMINA_TICK_SEC = 0.1f;

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
  private int dashCooldown= 0;
  private int jumpsLeft = MAX_JUMPS;
  private long lastJumpTime = 0; // timestamp of last ground jump

  // Stamina management
  private float stamina = INITIAL_STAMINA;
  private long  lastStaminaSpendMs = 0L;
  private boolean infiniteStamina = false;  // used for cheatcodes
  // Tracks the last integer stamina value we pushed to UI to avoid redundant events
  private int lastEmittedStamina = -1;

  @Override
  public void create() {
    physicsComponent = entity.getComponent(PhysicsComponent.class);
    entity.getEvents().addListener("walk", this::walk);
    entity.getEvents().addListener("walkStop", this::stopWalking);
    entity.getEvents().addListener("attack", this::attack);
    entity.getEvents().addListener("jumpAttempt", this::jump);
    entity.getEvents().addListener("sprintAttempt", this::sprintAttempt);
    entity.getEvents().addListener("dashAttempt", this::dash);
    entity.getEvents().addListener("crouchAttempt", this::crouchAttempt);
    entity.getEvents().addListener("crouchStop", () -> crouching = false);
    entity.getEvents().addListener("sprintStart", () -> sprinting = true);
    entity.getEvents().addListener("sprintStop",  () -> sprinting = false);

    startStaminaTask();
    emitStaminaChanged();
  }

  @Override
  public void update() {
    if (moving || dashing) {
      updateSpeed();
    }

    Body body = physicsComponent.getBody();
    if (touchingGround()) {
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
        boolean allowSprint = sprinting && hasDir && stamina > 0f;
        maxX = allowSprint ? SPRINT_SPEED.x : MAX_SPEED.x;
      }
      targetVx = walkDirection.x * maxX;
    }

    // impulse = (desiredVel - currentVel) * mass
    float impulseX = (targetVx - velocity.x) * body.getMass();
    body.applyLinearImpulse(new Vector2(impulseX, 0f), body.getWorldCenter(), true);

    if(grounded != touchingGround()) {
      if(touchingGround()) {
        grounded = true;
        entity.getEvents().trigger("walk", walkDirection);
      } else {
        grounded = false;
        entity.getEvents().trigger("fall", walkDirection);
      }
    }
   // if((!dashing) && (!crouching) && touchingGround() && hasDir) {
  //    entity.getEvents().trigger("walk", walkDirection);
  //  }
  }

  /**
   * Moves the player towards a given direction.
   *
   * @param direction direction to move in
   */
  void walk(Vector2 direction) {
    moving = true;
    this.walkDirection = direction;
    facingRight = this.walkDirection.x > 0;
    if(touchingGround()) {
      entity.getEvents().trigger("walkAnimate", walkDirection);
    } else {
      entity.getEvents().trigger("fall", walkDirection);
    }
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
      boolean withinCooldown = (currentTime - lastJumpTime) > JUMP_COOLDOWN_MS;
      if ((!isGroundJump || touchingGround())
              && (!isGroundJump || withinCooldown)) {
        if(!isGroundJump) {
          if(!trySpendStamina(DOUBLE_JUMP_COST)) {
            return;
          }
        }

        entity.getEvents().trigger("jump");
        body.applyLinearImpulse(JUMP_VELOCITY, body.getWorldCenter(), true);
        jumpsLeft--;
        lastJumpTime = currentTime;
        grounded = false;
    }
  }
}

  void sprintAttempt() {
    if (!crouching && hasStamina()) {
      sprinting = true;
      entity.getEvents().trigger("sprintStart");
    }
  }

  void dash() {
    if(touchingGround()) { // player must be grounded to dash
      if (dashCooldown == 0 && trySpendStamina(DASH_COST)) {
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
    if(touchingGround()) { // must be grounded
      entity.getEvents().trigger("crouchStart");
      crouching = true;
    }
  }

  /**
   * Checks if player is touching ground (ie. y velocity = 0)
   * @return if player is touching ground or not
   */
  boolean touchingGround() {
    // return true for infinite jumps
    return (physicsComponent.getBody().getLinearVelocity().y == 0f);
  }
  /**
   * Makes the player attack.
   */
  void attack() {
    Sound attackSound = ServiceLocator.getResourceService().getAsset("sounds/Impact4.ogg", Sound.class);
    attackSound.play();
  }

  /**
   * Checks if the player has any stamina left.
   * @return boolean indicating if the player has stamina.
   */
  private boolean hasStamina() {
    return stamina > 0f;
  }

  /**
   * Starts (or restarts) the repeating stamina update task.
   * Uses libGDX {@link Timer} so that ticks are posted onto the main render thread.
   * Idempotent: if a task is already running it will be cancelled and replaced.
   */
  private void startStaminaTask() {
    // Ensure there is only ever one repeating task running
    if (staminaTask != null) {
      staminaTask.cancel();
      staminaTask = null;
    }
    staminaTask = Timer.schedule(new Timer.Task() {
      @Override
      public void run() {
        staminaTick();
      }
    }, STAMINA_TICK_SEC, STAMINA_TICK_SEC); // initial delay, then fixed interval
  }

  /**
   * Cancels the repeating stamina task when this component is disposed to prevent leaks.
   */
  @Override
  public void dispose() {
    super.dispose();
    if (staminaTask != null) {
      staminaTask.cancel();
      staminaTask = null;
    }
  }

  /**
   * One stamina "tick". Called at a fixed cadence by {@link #startStaminaTask()}.
   * Checks for horizontal movement. Jumps, dashes and other special movement actions
   * stamina changes are implemented in the handlers respectively.
   *
   * Rules:
   * - If {@code infiniteStamina} is enabled, keep stamina pegged at MAX and notify UI.
   * - While sprinting and moving horizontally (and not dashing), drain stamina.
   * - If not spending for {@code STAMINA_REGEN_DELAY_MS}, regenerate up to MAX.
   * - UI is only notified when the integer stamina value actually changes.
   */
  private void staminaTick() {
    final long now = System.currentTimeMillis();

    // Cheat mode: keep stamina maxed and notify UI if it changed.
    if (infiniteStamina) {
      stamina = MAX_STAMINA;
      emitStaminaChanged();
      return;
    }

    // Drain if actively sprinting and actually moving horizontally (not dashing)
    final boolean actuallySprinting =
        sprinting && moving && Math.abs(walkDirection.x) > 0.0001f && !dashing;

    if (actuallySprinting && stamina > 0f) {
      final float drainPerTick = SPRINT_DRAIN_PER_SEC * STAMINA_TICK_SEC;
      stamina = Math.max(0f, stamina - drainPerTick);
      lastStaminaSpendMs = now;

      if ((int) stamina == 0) {
        // Auto-cancel sprint when we run dry.
        sprinting = false;
        entity.getEvents().trigger("sprintStop");
        entity.getEvents().trigger("outOfStamina");
      }
      emitStaminaChanged(); // only emits if value changed
      return; // If draining we don't regen in the same tick.
    }

    // Regenerate if we haven't spent for a while and we're not dashing.
    if (!dashing && (now - lastStaminaSpendMs) >= STAMINA_REGEN_DELAY_MS) {
      final float regenPerTick = SPRINT_REGEN_PER_SEC * STAMINA_TICK_SEC;
      final float before = stamina;
      stamina = Math.min(MAX_STAMINA, stamina + regenPerTick);
      if ((int) stamina != (int) before) {
        emitStaminaChanged();
      }
    }
  }

  /**
   * Attempts to spend the given amount of stamina.
   *
   * @param amount positive stamina cost
   * @return true if the cost was paid (or stamina is infinite); false if not enough stamina
   */
  private boolean trySpendStamina(int amount) {
    if (infiniteStamina) {
      return true;
    }
    if (amount <= 0) {
      // Non-positive costs are considered free.
      return true;
    }
    if (stamina >= amount) {
      stamina -= amount;
      lastStaminaSpendMs = System.currentTimeMillis();
      emitStaminaChanged();
      return true;
    }
    return false;
  }

  /**
   * Emits a stamina change event to the UI layer, but only if the integer value
   * has actually changed since the last emission. This avoids redundant UI work.
   */
  private void emitStaminaChanged() {
    final int current = (int) stamina;
    if (current == lastEmittedStamina) {
      return;
    }
    lastEmittedStamina = current;
    entity.getEvents().trigger("staminaChanged", current, MAX_STAMINA);
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
    this.infiniteStamina = true;
    this.stamina = MAX_STAMINA;
    emitStaminaChanged();
  }
}
