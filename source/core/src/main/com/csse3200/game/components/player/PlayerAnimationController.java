package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.math.Vector2;

/**
 * This class listens to events relevant to the players state and plays the animation when one
 * of the events is triggered.
 */
public class PlayerAnimationController extends Component {
    AnimationRenderComponent animator;
    private boolean facingRight = true;
    private boolean sprinting = false;

    /**
     * Creates a new animation controller and adds event listeners for relevant events.
     */
    @Override
    public void create() {
        super.create();
        animator = this.entity.getComponent(AnimationRenderComponent.class);
        entity.getEvents().addListener("walk", this::animateWalk);
        entity.getEvents().addListener("walkStop", this::animateStop);
        entity.getEvents().addListener("jump", this::animateJump);
        entity.getEvents().addListener("sprintStart", () -> sprinting = true);
        entity.getEvents().addListener("sprintStop", () -> sprinting = false);
    }

    /**
     * Starts a walking animation facing the specified direction
     *
     * @param direction direction which the player is walking
     */
    void animateWalk(Vector2 direction) {
        if(!sprinting) {
            if (direction.x > 0) {
                animator.startAnimation("right_walk");
                facingRight = true;
            } else {
                animator.startAnimation("left_walk");
                facingRight = false;
            }
        } else {
            if (direction.x > 0) {
                animator.startAnimation("right_run");
                facingRight = true;
            } else {
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
            animator.startAnimation("right_jump");
        } else {
            animator.startAnimation("left_jump");
        }
    }

    /**
     * Animates player standing facing the last known direction
     */
    void animateStop() {
        if(facingRight) {
            animator.startAnimation("right_stand");
        } else {
            animator.startAnimation("left_stand");
        }
    }

}
