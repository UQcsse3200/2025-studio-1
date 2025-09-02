package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.math.Vector2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class listens to events relevant to the players state and plays the animation when one
 * of the events is triggered.
 */
public class PlayerAnimationController extends Component {
    AnimationRenderComponent animator;
    private static final Logger logger = LoggerFactory.getLogger(PlayerAnimationController.class);
    private boolean facingRight = true;
    private boolean stopped = true;
    private boolean sprinting = false;
    private boolean crouching = false;
    private boolean falling = true;
    /**
     * Creates a new animation controller and adds event listeners for relevant events.
     */
    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        entity.getEvents().addListener("move", this::updateDirection);
        entity.getEvents().addListener("walkAnimate", this::animateMove);
        entity.getEvents().addListener("walkStop", this::stopMoving);
        entity.getEvents().addListener("jump", this::animateJump);
        entity.getEvents().addListener("sprintStart", () -> sprinting = true);
        entity.getEvents().addListener("sprintStop", () -> sprinting = false);
        entity.getEvents().addListener("dash", this::animateDash);
        entity.getEvents().addListener("crouchStart", this::startCrouching);
        entity.getEvents().addListener("crouchStop", this::stopCrouching);
        entity.getEvents().addListener("groundLeft", this::startFalling);
        entity.getEvents().addListener("groundTouched", this::stopFalling);
    }

    void updateDirection(boolean movingRight) {
        facingRight = movingRight;
        stopped = false;
    }
    /**
     * Starts a walking animation facing the specified direction
     *
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

    void animateWalk() {
        if (facingRight) {
            logger.debug("Animating right walk");
            animator.startAnimation("right_walk");

        } else {
            logger.debug("Animating left walk");
            animator.startAnimation("left_walk");
        }
    }

    void animateSprint() {
        if (facingRight) {
            logger.debug("Animating right sprint");
            animator.startAnimation("right_run");

        } else {
            logger.debug("Animating left sprint");
            animator.startAnimation("left_run");
        }
    }

    /**
     * Animates a player jump facing the last known direction.
     */
    void animateJump() {
        if (facingRight) {
            logger.debug("Animating right jump");
            animator.startAnimation("right_jump");

        } else {
            logger.debug("Animating left jump");
            animator.startAnimation("left_jump");
        }
    }

    void stopMoving() {
        stopped = true;
        if (!falling) {
            animateIdle();
        }
    }

    /**
     * Animates player standing facing the last known direction
     */
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

    /**
     * Animates a player dashing in the relevant direction
     */
    void animateDash() {
        if (facingRight) {
            logger.debug("Animating right dash");
            animator.startAnimation("right_run");

        } else {
            logger.debug("Animating left dash");
            animator.startAnimation("left_run");
        }
    }

    /**
     * Animates player crouching in given direction
     *
     */
    void animateCrouch() {
        if (facingRight) {
            logger.debug("Animating right crouch");
            animator.startAnimation("right_crouch");

        } else {
            logger.debug("Animating left crouch");
            animator.startAnimation("left_crouch");
        }
    }

    void animateCrouchIdle() {
        if (facingRight) {
            logger.debug("Animating right stand crouch");
            animator.startAnimation("right_stand_crouch");

        } else {
            logger.debug("Animating left stand crouch");
            animator.startAnimation("left_stand_crouch");
        }
    }

    void animateFall() {
        if (facingRight) {
            animator.startAnimation("right_fall");

        } else {
            animator.startAnimation("left_fall");
        }
    }

    void startCrouching() {
        crouching = true;
        animateCrouchIdle();
    }

    void stopCrouching() {
        crouching = false;
        animateIdle();
    }

    void startFalling(Vector2 direction) {
        falling = true;
        animateFall();
    }

    void stopFalling(Vector2 direction) {
        falling = false;
        if (stopped) {
            animateIdle();
        } else {
            animateMove();
        }
    }
}
