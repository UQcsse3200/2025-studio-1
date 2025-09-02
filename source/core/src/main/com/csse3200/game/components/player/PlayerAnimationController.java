package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.math.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls player animations by listening to relevant events and
 * updating the AnimationRenderComponent accordingly.
 *
 * <p>This component reacts to movement, jumping, sprinting, crouching,
 * dashing, and falling events to play the appropriate animations
 * based on the player's state and direction.</p>
 */
public class PlayerAnimationController extends Component {
    /** The animation component used to play animations. */
    AnimationRenderComponent animator;

    private static final Logger logger = LoggerFactory.getLogger(PlayerAnimationController.class);

    /** True if the player is facing right, false if left. */
    private boolean facingRight = true;

    /** True if the player is stopped, false if moving. */
    private boolean stopped = true;

    /** True if the player is sprinting. */
    private boolean sprinting = false;

    /** True if the player is crouching. */
    private boolean crouching = false;

    /** True if the player is falling. */
    private boolean falling = true;

    /**
     * Creates a new animation controller, retrieves the animator component,
     * and registers event listeners for relevant player actions.
     */
    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        entity.getEvents().addListener("move", this::updateDirection);
        entity.getEvents().addListener("walkAnimate", this::animateMove);
        entity.getEvents().addListener("walkStop", this::stopMoving);
        entity.getEvents().addListener("jump", this::animateJump);
        entity.getEvents().addListener("sprintStart", this::startSprinting);
        entity.getEvents().addListener("sprintStop", this::stopSprinting);
        entity.getEvents().addListener("dash", this::animateDash);
        entity.getEvents().addListener("crouchStart", this::startCrouching);
        entity.getEvents().addListener("crouchStop", this::stopCrouching);
        entity.getEvents().addListener("groundLeft", this::startFalling);
        entity.getEvents().addListener("groundTouched", this::stopFalling);
    }

    /**
     * Updates the facing direction based on movement and marks
     * the player as moving.
     *
     * @param movingRight true if the player is moving right, false if left
     */
    void updateDirection(boolean movingRight) {
        facingRight = movingRight;
        stopped = false;
    }

    /**
     * Starts the appropriate movement animation depending on
     * whether the player is walking, sprinting, crouching, or falling.
     */
    void animateMove() {
        if (falling) {
            animateFall();
        } else if (crouching) {
            animateCrouch();
        } else if (sprinting) {
            animateSprint();
        } else {
            animateWalk();
        }
    }

    /** Plays the walking animation for the current facing direction. */
    void animateWalk() {
        if (facingRight) {
            logger.debug("Animating right walk");
            animator.startAnimation("right_walk");
        } else {
            logger.debug("Animating left walk");
            animator.startAnimation("left_walk");
        }
    }

    /** Plays the sprinting animation for the current facing direction. */
    void animateSprint() {
        if (facingRight) {
            logger.debug("Animating right sprint");
            animator.startAnimation("right_run");
        } else {
            logger.debug("Animating left sprint");
            animator.startAnimation("left_run");
        }
    }

    /** Plays the jump animation for the current facing direction. */
    void animateJump() {
        if (facingRight) {
            logger.debug("Animating right jump");
            animator.startAnimation("right_jump");
        } else {
            logger.debug("Animating left jump");
            animator.startAnimation("left_jump");
        }
    }

    /** Stops movement and plays idle animation if the player is not falling. */
    void stopMoving() {
        stopped = true;
        if (!falling) {
            animateIdle();
        }
    }

    /** Plays the idle animation for the current facing direction. */
    void animateIdle() {
        if (crouching) {
            animateCrouchIdle();
            return;
        }
        if (facingRight) {
            logger.debug("Animating right stand");
            animator.startAnimation("right_stand");
        } else {
            logger.debug("Animating left stand");
            animator.startAnimation("left_stand");
        }
    }

    /** Plays the dash animation for the current facing direction. */
    void animateDash() {
        if (facingRight) {
            logger.debug("Animating right dash");
            animator.startAnimation("right_run");
        } else {
            logger.debug("Animating left dash");
            animator.startAnimation("left_run");
        }
    }

    /** Plays the crouch animation for the current facing direction. */
    void animateCrouch() {
        if (facingRight) {
            logger.debug("Animating right crouch");
            animator.startAnimation("right_crouch");
        } else {
            logger.debug("Animating left crouch");
            animator.startAnimation("left_crouch");
        }
    }

    /** Plays the idle crouch animation for the current facing direction. */
    void animateCrouchIdle() {
        if (facingRight) {
            logger.debug("Animating right stand crouch");
            animator.startAnimation("right_stand_crouch");
        } else {
            logger.debug("Animating left stand crouch");
            animator.startAnimation("left_stand_crouch");
        }
    }

    /** Plays the falling animation for the current facing direction. */
    void animateFall() {
        if (facingRight) {
            animator.startAnimation("right_fall");
        } else {
            animator.startAnimation("left_fall");
        }
    }

    /** Starts crouching and plays the appropriate crouch idle animation. */
    void startCrouching() {
        crouching = true;
        animateCrouchIdle();
    }

    /** Stops crouching and returns to the idle animation. */
    void stopCrouching() {
        crouching = false;
        animateIdle();
    }

    /** Starts sprinting and updates the movement animation if applicable. */
    void startSprinting() {
        sprinting = true;
        if (!stopped) {
            animateMove();
        }
    }

    /** Stops sprinting and updates the movement or idle animation. */
    void stopSprinting() {
        sprinting = false;
        if (!stopped) {
            animateMove();
        } else {
            animateIdle();
        }
    }

    /**
     * Called when the player leaves the ground, starting the falling animation.
     *
     * @param direction The direction vector of the fall
     */
    void startFalling(Vector2 direction) {
        falling = true;
        animateFall();
    }

    /**
     * Called when the player touches the ground, stopping the falling animation
     * and updating movement or idle animations accordingly.
     *
     * @param direction The direction vector when landing
     */
    void stopFalling(Vector2 direction) {
        falling = false;
        if (stopped) {
            animateIdle();
        } else {
            animateMove();
        }
    }
}
