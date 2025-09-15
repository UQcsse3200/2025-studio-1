package com.csse3200.game.components.boss;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;

/**
 * 根据血量百分比切换动画的组件（3 段：idle / phase2 / angry）。
 * Switch boss animation by HP ratio (3 stages: idle / phase2 / angry).
 *
 * 规则 / Rules:
 *   hp/max > 0.50  -> idle
 *   0.30 < hp/max <= 0.50 -> phase2
 *   hp/max <= 0.30 -> angry
 *
 * 如果 atlas 中缺失对应的动画名，会跳过切换以避免报错。
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

    // 记录是否已经进入过这些阶段，便于触发事件时只发一次（可选）
    private boolean enteredPhase2 = false;
    private boolean enteredAngry  = false;

    // 当前播放中的动画名，避免每帧重复 startAnimation
    private String current = null;

    public Boss2HealthPhaseSwitcher(float phase2Threshold,
                                   float angryThreshold,
                                   String idleName,
                                   String phase2Name,
                                   String angryName) {
        // 合法化与存储 / sanitize & store
        this.phase2Threshold = Math.max(0f, Math.min(1f, phase2Threshold));
        this.angryThreshold  = Math.max(0f, Math.min(this.phase2Threshold, angryThreshold));
        this.idleName  = idleName;
        this.phase2Name = phase2Name;
        this.angryName  = angryName;
    }

    @Override
    public void create() {
        stats = entity.getComponent(CombatStatsComponent.class);
        arc   = entity.getComponent(AnimationRenderComponent.class);
        // 初始时就根据当前血量播一次
        playForCurrentHp();
    }

    @Override
    public void update() {
        // 懒加载，防止添加顺序导致 null
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
        int hp  = stats.getHealth();
        if (max <= 0) return;

        float ratio = hp / (float) max;
        String want;
        if (ratio <= angryThreshold && arc.hasAnimation(angryName)) {
            want = angryName;
            if (!enteredAngry) {
                enteredAngry = true;
                // 可选事件：进入暴怒
                // Optional: emit an event for entering angry stage
                entity.getEvents().trigger("boss2:angry");
            }
        } else if (ratio <= phase2Threshold && arc.hasAnimation(phase2Name)) {
            want = phase2Name;
            if (!enteredPhase2) {
                enteredPhase2 = true;
                // 可选事件：进入二阶段
                entity.getEvents().trigger("boss2:phase2");
            }
        } else {
            want = idleName; // 默认回到 idle
        }

        if (!want.equals(current)) {
            current = want;
            if (arc.hasAnimation(want)) {
                arc.startAnimation(want);
            }
        }
    }
}

