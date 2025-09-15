package com.csse3200.game.components.boss;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.rendering.AnimationRenderComponent;

public class Boss2HealthPhaseSwitcher extends Component {
    private final float thresholdRatio; // 0.5 表示 50%
    private final String phase1Name;
    private final String phase2Name;
    private boolean switched = false;

    private CombatStatsComponent stats;
    private AnimationRenderComponent arc;

    public Boss2HealthPhaseSwitcher(float thresholdRatio, String phase1Name, String phase2Name) {
        this.thresholdRatio = thresholdRatio;
        this.phase1Name = phase1Name;
        this.phase2Name = phase2Name;
    }

    @Override
    public void create() {
        stats = entity.getComponent(CombatStatsComponent.class);
        arc   = entity.getComponent(AnimationRenderComponent.class);
    }

    @Override
    public void update() {
        if (switched) return;
        if (stats == null) {
            stats = entity.getComponent(CombatStatsComponent.class);
            if (stats == null) return;
        }
        int max = stats.getMaxHealth();
        int hp  = stats.getHealth();
        if (max <= 0) return;

        if (hp <= max * thresholdRatio) {
            switched = true;
            if (arc != null && arc.hasAnimation(phase2Name)) {
                arc.startAnimation(phase2Name);
            }
            // 如果你还要发事件： entity.getEvents().trigger("boss2:phase2");
        }
    }
}
