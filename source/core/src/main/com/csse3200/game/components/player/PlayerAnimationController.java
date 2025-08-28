package com.csse3200.game.components.player;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;
import com.badlogic.gdx.math.Vector2;

/**
 * This class listens to events relevant to a ghost entity's state and plays the animation when one
 * of the events is triggered.
 */
public class PlayerAnimationController extends Component {
    AnimationRenderComponent animator;
    private boolean facingRight = true;
    private boolean sprinting = false;

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

    void animateWalk(Vector2 direction) {
        if(!sprinting) {
            if (direction.x > 0) {
                animator.startAnimation("right_walk");
            } else {
                animator.startAnimation("left_walk");
            }
        } else {
            if (direction.x > 0) {
                animator.startAnimation("right_run");
            } else {
                animator.startAnimation("left_run");
            }

        }
    }

    void animateJump() {
        if (facingRight) {
            animator.startAnimation("right_jump");
        } else {
            animator.startAnimation("left_jump");
        }
    }

    void animateStop() {
        if(facingRight) {
            animator.startAnimation("right_stand");
        } else {
            animator.startAnimation("left_stand");
        }
    }

}
