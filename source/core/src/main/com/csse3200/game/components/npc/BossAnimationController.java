package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

public class BossAnimationController extends Component {
    private AnimationRenderComponent animator;
    private boolean inFury = false;

    @Override
    public void create() {
        animator = entity.getComponent(AnimationRenderComponent.class);
        if (animator == null) {
            return;
        }

        entity.getEvents().addListener("wanderStart", this::playIdle);
        entity.getEvents().addListener("chaseStart",  this::playAttack);

        // Boss event
        entity.getEvents().addListener("boss:attackStart", this::playAttack);
        entity.getEvents().addListener("boss:enraged", this::onFury);
        entity.getEvents().addListener("boss:death", this::playDeath);

        playIdle();
    }

    private void onFury() {
        inFury = true;
        playFury();
    }

    private void playIdle() {
        if (animator == null) return;
        if (inFury) {
            animator.startAnimation("fury");
        } else {
            animator.startAnimation("Idle");
        }
    }

    private void playAttack() { if (animator != null) animator.startAnimation("attack"); }
    private void playFury() { if (animator != null) animator.startAnimation("fury");}
    private void playDeath() { if (animator != null) animator.startAnimation("death"); }
}
