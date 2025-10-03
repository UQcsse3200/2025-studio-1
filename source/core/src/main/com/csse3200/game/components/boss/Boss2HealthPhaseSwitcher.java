package com.csse3200.game.components.boss;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * Switch boss animation by HP ratio (3 stages: idle / phase2 / angry).
 * If an animation name is missing in the atlas, the switch is skipped safely.
 */
public class Boss2HealthPhaseSwitcher extends Component {
    private final float phase2Threshold; // e.g., 0.5f
    private final float angryThreshold;  // e.g., 0.3f
    private final String idleName;
    private final String phase2Name;
    private final String angryName;

    private CombatStatsComponent stats;
    private AnimationRenderComponent arc;

    private boolean enteredPhase2 = false;
    private boolean enteredAngry = false;

    private String current = null;

    public Boss2HealthPhaseSwitcher(float phase2Threshold,
                                    float angryThreshold,
                                    String idleName,
                                    String phase2Name,
                                    String angryName) {
        // 合法化与存储 / sanitize & store
        this.phase2Threshold = Math.max(0f, Math.min(1f, phase2Threshold));
        this.angryThreshold = Math.max(0f, Math.min(this.phase2Threshold, angryThreshold));
        this.idleName = idleName;
        this.phase2Name = phase2Name;
        this.angryName = angryName;
    }

    @Override
    public void create() {
        stats = entity.getComponent(CombatStatsComponent.class);
        arc = entity.getComponent(AnimationRenderComponent.class);
        // 初始时就根据当前血量播一次
        playForCurrentHp();
    }

    @Override
    public void update() {
        if (stats == null) {
            stats = entity.getComponent(CombatStatsComponent.class);
        }
        if (arc == null) {
            arc = entity.getComponent(AnimationRenderComponent.class);
        }
        if (stats == null || arc == null) return;

        playForCurrentHp();
    }

    private void playForCurrentHp() {
        int max = stats.getMaxHealth();
        int hp = stats.getHealth();
        if (max <= 0) return;

        float ratio = hp / (float) max;
        String want;
        if (ratio <= angryThreshold && arc.hasAnimation(angryName)) {
            want = angryName;
            if (!enteredAngry) {
                enteredAngry = true;
                // Optional: emit an event for entering angry stage
                entity.getEvents().trigger("boss2:angry");
            }
        } else if (ratio <= phase2Threshold && arc.hasAnimation(phase2Name)) {
            want = phase2Name;
            if (!enteredPhase2) {
                enteredPhase2 = true;
                entity.getEvents().trigger("boss2:phase2");
            }
        } else {
            want = idleName;
        }

        if (!want.equals(current)) {
            current = want;
            if (arc.hasAnimation(want)) {
                arc.startAnimation(want);
            }
        }
    }
}

