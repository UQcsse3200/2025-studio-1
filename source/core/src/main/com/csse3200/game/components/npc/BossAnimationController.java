package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

public class BossAnimationController extends Component {
    private AnimationRenderComponent animator;

    @Override
    public void create() {
        animator = entity.getComponent(AnimationRenderComponent.class);
        if (animator == null) {
            return;
        }

        entity.getEvents().addListener("wanderStart", this::playIdle);
        entity.getEvents().addListener("chaseStart",  this::playAngry);

        // Boss event
        entity.getEvents().addListener("boss:attackStart", this::playAngry);
        entity.getEvents().addListener("boss:enraged", this::playAttack);

        playIdle();
    }

    private void playIdle()  { if (animator != null) animator.startAnimation("Idle"); }
    private void playAngry() { if (animator != null) animator.startAnimation("attack"); }

    private void playAttack() {
        if (animator == null) return;
        animator.startAnimation("attack");
        //animator.startAnimation("angry_float");
    }
}
