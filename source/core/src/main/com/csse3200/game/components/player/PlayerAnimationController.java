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
    private boolean sprinting = false;
    private boolean crouching = false;
    //private boolean falling = false;
    /**
     * Creates a new animation controller and adds event listeners for relevant events.
     */
    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        entity.getEvents().addListener("walkAnimate", this::animateWalk);
        entity.getEvents().addListener("walkStop", this::animateStop);
        entity.getEvents().addListener("jump", this::animateJump);
        entity.getEvents().addListener("sprintStart", () -> sprinting = true);
        entity.getEvents().addListener("sprintStop", () -> sprinting = false);
        entity.getEvents().addListener("dash", this::animateDash);
        entity.getEvents().addListener("crouchStart", this::startCrouching);
        entity.getEvents().addListener("crouchStop", this::stopCrouching);
        entity.getEvents().addListener("fall", this::animateFall);
    }

    /**
     * Starts a walking animation facing the specified direction
     *
     * @param direction direction which the player is walking
     */
    void animateWalk(Vector2 direction) {
        if(!sprinting) {
            if (crouching) {
                animateCrouch(direction);
            }
            else if (direction.x > 0) {
                logger.debug("Animating right walk");
                animator.startAnimation("right_walk");
                facingRight = true;
            } else {
                logger.debug("Animating left walk");
                animator.startAnimation("left_walk");
                facingRight = false;
            }
        } else {
            if (direction.x > 0) {
                logger.debug("Animating right sprint");
                animator.startAnimation("right_run");
                facingRight = true;
            } else {
                logger.debug("Animating left sprint");
                animator.startAnimation("left_run");
                facingRight = false;
            }

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

    /**
     * Animates player standing facing the last known direction
     */
    void animateStop() {
        if(facingRight) {
            logger.debug("Animating right stand");
            animator.startAnimation("right_stand");
        } else {
            logger.debug("Animating left stand");
            animator.startAnimation("left_stand");
        }
    }

    /**
     * Animates a player dashing in the relevant direction
     * @param facingRight whether player is facing right or not
     */
    void animateDash(boolean facingRight) {
        if(facingRight) {
            logger.debug("Animating right dash");
            animator.startAnimation("right_run");
        } else {
            logger.debug("Animating left dash");
            animator.startAnimation("left_run");
        }
    }

    void startCrouching() {
        crouching = true;
        animateCrouch(Vector2.Zero);
    }

    void stopCrouching() {
        crouching = false;

    }

    /**
     * Animates player crouching in given direction
     *
     * @param direction direction player is moving
     */
    void animateCrouch(Vector2 direction) {
        if(direction == Vector2.Zero) {
            if(facingRight) {
                logger.debug("Animating right stand crouch");
                animator.startAnimation("right_stand_crouch");
            } else {
                logger.debug("Animating left stand crouch");
                animator.startAnimation("left_stand_crouch");
            }
        } else if(direction.x > 0) {
            logger.debug("Animating right crouch");
            animator.startAnimation("right_crouch");
            facingRight = true;
        } else {
            logger.debug("Animating left crouch");
            animator.startAnimation("left_crouch");
            facingRight = false;
        }
        // Need to add idle check (if direction = 0)
    }

    void animateFall(Vector2 direction) {
        if(direction.x > 0) {
            facingRight = true;
            animator.startAnimation("right_fall");
        } else {
            facingRight = false;
            animator.startAnimation("left_fall");
        }
    }

}
