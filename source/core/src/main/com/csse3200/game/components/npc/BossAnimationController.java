package com.csse3200.game.components.npc;

import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

public class BossAnimationController extends Component {
    private AnimationRenderComponent animator;

    @Override
    public void create() {
        animator = entity.getComponent(AnimationRenderComponent.class);

        entity.getEvents().addListener("wanderStart", this::playIdle);
        entity.getEvents().addListener("chaseStart",  this::playAngry);

        // Boss event
        entity.getEvents().addListener("boss:enraged", this::playAngry);
        entity.getEvents().addListener("boss:attackStart", this::playAttack);

        playIdle();
    }

    private void playIdle()  { if (animator != null) animator.startAnimation("float"); }
    private void playAngry() { if (animator != null) animator.startAnimation("angry_float"); }

    private void playAttack() {
        if (animator == null) return;
        // animator.startAnimation("attack");
        animator.startAnimation("angry_float");
    }
}
