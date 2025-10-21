package com.csse3200.game.components.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Timer;
import com.csse3200.game.components.*;
import com.csse3200.game.effects.AimbotEffect;
import com.csse3200.game.effects.UnlimitedAmmoEffect;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;
import com.csse3200.game.services.ServiceLocator;

/**
 * Component that handles all player actions including movement, jumping, sprinting,
 * dashing, crouching, attacking, and shooting.
 *
 * <p>Events related to the player should be initialized in {@link #create()},
 * and corresponding actions are triggered when these events occur.</p>
 */
public class PlayerActions extends Component {
    // Movement Constants
    private static final Vector2 MAX_SPEED = new Vector2(3f, 3f);
    private static final Vector2 CROUCH_SPEED = new Vector2(1.5f, 3f);
    private static final Vector2 SPRINT_SPEED = new Vector2(7f, 3f);
    private static final Vector2 JUMP_VELOCITY = new Vector2(0f, 8f);
    private static final Vector2 DASH_SPEED = new Vector2(20f, 9.8f);
    private static final float DASH_DURATION = 0.1f;
    // Stamina Costs
    private static final int DASH_COST = 30;
    private static final int SPRINT_COST = 1;
    // Jumping Limits
    private static final int MAX_JUMPS = 2;
    /**
     * if player already has a weapon --> unequip first
     * sets new weapon as equipped
     * repositions the weapon to appear in player's hand
     */
    Entity currentWeapon = null;
    //to represent relative difference between the
    //player's body position and player's hand.
    float handOffsetX = 5f;
    float handOffsetY = 10f;
    // Components
    private StaminaComponent stamina;
    private PhysicsComponent physicsComponent;
    private int DASH_COOLDOWN = 15;
    // Internal movement state
    private Vector2 walkDirection = Vector2.Zero.cpy();
    private boolean moving = false;
    private boolean sprinting = false;
    private boolean facingRight = true;
    private boolean dashing = false;
    private boolean crouching = false;
    private boolean grounded = true;
    // Effects
    private final UnlimitedAmmoEffect unlimitedAmmoEffect = new UnlimitedAmmoEffect(10f);
    private final AimbotEffect aimbotEffect = new AimbotEffect(10f);
    // Ability cooldowns / counters
    private int dashCooldown = 0;
    private int jumpsLeft = MAX_JUMPS;
    // Tracks time since last reload for cooldown purposes
    private float timeSinceLastReload = 0;

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
        entity.getEvents().addListener("jumpAttempt", this::jump);
        entity.getEvents().addListener("sprintAttempt", this::sprintAttempt);
        entity.getEvents().addListener("dashAttempt", this::dash);
        entity.getEvents().addListener("crouchAttempt", this::crouchAttempt);
        entity.getEvents().addListener("crouchStop", () -> crouching = false);
        entity.getEvents().addListener("sprintStart", this::startSprinting);
        entity.getEvents().addListener("sprintStop", this::stopSprinting);
        entity.getEvents().addListener("reload", this::reload);
    }

    /**
     * Updates player movement, speed, and grounded status.
     * Called once per frame.
     */
    @Override
    public void update() {
        if (!Gdx.input.isKeyPressed(Input.Keys.A) && !Gdx.input.isKeyPressed(Input.Keys.D) && !dashing) {
            this.walkDirection.x = 0f;
            entity.getEvents().trigger("walkStop");
            if (!grounded) {
                entity.getEvents().trigger("groundLeft", walkDirection);
            }
        }
        if (!Gdx.input.isKeyPressed(Input.Keys.S) && crouching) {
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

        timeSinceLastReload += ServiceLocator.getTimeSource().getDeltaTime();
    }

    /**
     * Updates grounded state and triggers ground touched events.
     */
    private void updateGrounded() {
        if (!grounded) {
            setGrounded(true);
            entity.getEvents().trigger("groundTouched", walkDirection);
        }
        jumpsLeft = MAX_JUMPS;
    }

    /**
     * Updates midair state and triggers ground left events.
     */
    private void updateMidair() {
        if (grounded) {
            setGrounded(false);
            entity.getEvents().trigger("groundLeft", walkDirection);
        }
        crouching = false;
    }

    /**
     * Applies player movement based on current walk direction and velocity.
     */
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

    /**
     * Calculates impulse to apply based on desired speed and current velocity.
     */
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

    /**
     * Stops walking and updates speed and movement state.
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
     * Adds vertical velocity to the player to jump if jumps remain.
     */
    void jump() {
        Body body = physicsComponent.getBody();

        if (dashing) return;

        if (jumpsLeft > 0) {
            Sound jump = ServiceLocator.getResourceService().getAsset("sounds/jump.mp3", Sound.class);
            jump.play();

            setGrounded(false);

            boolean isGroundJump = (jumpsLeft == MAX_JUMPS);
            boolean canJump = true;

            // Check cooldown for ground jumps
            if (isGroundJump) {
                if (!touchingGround()) canJump = false;
                if (physicsComponent.getBody().getLinearVelocity().y > 0f) canJump = false;
            }

            if (canJump) {
                entity.getEvents().trigger("jump");
                body.applyLinearImpulse(JUMP_VELOCITY, body.getWorldCenter(), true);
                jumpsLeft--;
            }
        }
    }

    /**
     * Attempts to start sprinting if stamina allows.
     */
    void sprintAttempt() {
        if (!crouching && stamina.hasStamina(SPRINT_COST)) {
            sprinting = true;
            stamina.setSprinting(true);
            entity.getEvents().trigger("sprintStart");
        }
    }

    /**
     * Attempts to perform a dash if cooldown and stamina allow.
     */
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

    /**
     * Sets the duration of a dash, after which dashing stops.
     */
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

    /**
     * Handles dash cooldown by decrementing over time.
     */
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

    /**
     * Attempts to crouch if player is grounded.
     */
    void crouchAttempt() {
        if (touchingGround()) {
            entity.getEvents().trigger("crouchStart");
            crouching = true;
        }
    }

    /**
     * Sets grounded state and updates stamina accordingly.
     */
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

    /**
     * Starts sprinting and updates stamina.
     */
    void startSprinting() {
        sprinting = true;
        stamina.setSprinting(true);
    }

    /**
     * Stops sprinting and updates stamina.
     */
    void stopSprinting() {
        sprinting = false;
        stamina.setSprinting(false);
    }

    /**
     * Cheat: infinite dashes.
     */
    public void infDash() {
        this.DASH_COOLDOWN = 0;
    }

    /**
     * Cheat: infinite jumps.
     */
    public void infJumps() {
        this.jumpsLeft = 9999;
    }

    /**
     * Cheat: infinite stamina.
     */
    public void infStamina() {
        stamina.setInfiniteStamina(true);
    }

    public UnlimitedAmmoEffect getUnlimitedAmmoEffect() {
        return unlimitedAmmoEffect;
    }

    public AimbotEffect getAimbotEffect() {
        return aimbotEffect;
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
     * Upgrades the speed of the player
     */
    public void upgradeSpeed() {
        MAX_SPEED.x *= 1.25F;
        MAX_SPEED.y *= 1.25F;
        CROUCH_SPEED.x *= 1.25F;
        CROUCH_SPEED.y *= 1.25F;
        SPRINT_SPEED.x *= 1.25F;
        SPRINT_SPEED.y *= 1.25F;

    }

    /**
     * @return the max speed vector
     */
    public Vector2 getMaxSpeed() {
        return MAX_SPEED;
    }

    /**
     * @return the crouch speed
     */
    public Vector2 getCrouchSpeed() {
        return CROUCH_SPEED;
    }

    /**
     * @return the sprint speed
     */
    public Vector2 getSprintSpeed() {
        return SPRINT_SPEED;
    }

    /**
     * Makes player reload their equipped weapon
     */
    void reload() {
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        Entity gun = inventory.getCurrSlot();

        if (gun != null) {
            MagazineComponent mag = gun.getComponent(MagazineComponent.class);
            if (mag != null) {

                if (timeSinceLastReload <= 1.5f) {

                    return;
                }

                Sound reloadSound;

                if (mag.reload(entity)) {
                    reloadSound = ServiceLocator.getResourceService().getAsset("sounds/reload.mp3", Sound.class);
                } else {
                    reloadSound = ServiceLocator.getResourceService().getAsset("sounds/shot_failed.mp3", Sound.class);
                }

                reloadSound.play();
                timeSinceLastReload = 0f;
                entity.getEvents().trigger("after reload");
            }
        }
    }



    public void equipWeapon(Entity weapon) {
        if (currentWeapon != null) {
            unequipWeapon();
        }
        currentWeapon = weapon;

        TextureRenderComponent renderComp = weapon.getComponent(TextureRenderComponent.class);
        if (renderComp != null) {
            renderComp.setEnabled(true);
        }

        updateWeaponPosition();
    }

    /**
     * ensures that no weapon is equipped and weapon’s
     * texture (sprite) is no longer visible in the game.
     */
    public void unequipWeapon() {
        if (currentWeapon == null) return;

        TextureRenderComponent renderComp = currentWeapon.getComponent(TextureRenderComponent.class);
        if (renderComp != null) {
            renderComp.setEnabled(false);
        }

        currentWeapon = null;
    }

    /**
     * this function sets the coordinates for the weapon in player's hand
     */
    public void updateWeaponPosition() {
        if (currentWeapon == null) return;

        PhysicsComponent physics = currentWeapon.getComponent(PhysicsComponent.class);
        if (physics == null) {
            System.out.println("Weapon has no PhysicsComponent," +
                    "\nupdating position directly");
            // Directly update visual position without physics:
            Vector2 playerPos = entity.getPosition();
            if (facingRight) {
                currentWeapon.setPosition(playerPos.x + handOffsetX, playerPos.y + handOffsetY);
            } else {
                currentWeapon.setPosition(playerPos.x - handOffsetX, playerPos.y + handOffsetY);
            }
            return;
        }

        Body body = physics.getBody();
        if (body == null) {
            System.out.println("Weapon's PhysicsComponent body is null," +
                    "\nskipping physics-based position update");
            return; // Don't update position through physics if body not created
        }

        // Safe to use physics body
        Vector2 playerPos = entity.getPosition();
        if (facingRight) {
            body.setTransform(playerPos.x + handOffsetX, playerPos.y + handOffsetY, 0f);
        } else {
            body.setTransform(playerPos.x - handOffsetX, playerPos.y + handOffsetY, 0f);
        }
    }

    public boolean isFacingRight() {
        return this.facingRight;
    }
}
