package com.csse3200.game.components.boss;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;

/**
 * Trigger for the boss's defense phase (one-time).
 * <p>When health ≤ {@code triggerHp}, activate {@link DamageReductionComponent}
 * (lasts {@code durationSec} seconds, reducing/avoiding damage by {@code reduction}).
 * Optionally disables self-damage during the defense phase
 * (via {@link WeaponsStatsComponent#setDisableDamage(boolean)}). </p>
 * <p>Requires both {@link CombatStatsComponent} and {@link DamageReductionComponent} to be attached to the entity. </p>
 */
public class BossDefenseComponent extends Component {
    private final float durationSec;
    private final float reduction;
    private final int triggerHp;
    private final boolean closeAttackWhileDefending;
    private boolean used = false;

    private final GameTime time = ServiceLocator.getTimeSource();

    /**
     * @param durationSec Defense duration (seconds)
     * @param reduction Damage reduction ratio, range [0, 1] (1 represents complete immunity)
     * @param triggerHp Trigger threshold: Current HP ≤ this value triggers the attack
     * @param closeAttackWhileDefending Whether to close self-damage during defense
     */
    public BossDefenseComponent(float durationSec, float reduction, int triggerHp,
                                boolean closeAttackWhileDefending) {
        this.durationSec = durationSec;
        this.reduction = reduction;
        this.triggerHp = triggerHp;
        this.closeAttackWhileDefending = closeAttackWhileDefending;
    }

    @Override
    public void update() {
        CombatStatsComponent hp = entity.getComponent(CombatStatsComponent.class);
        DamageReductionComponent dr = entity.getComponent(DamageReductionComponent.class);
        if (hp == null || dr == null) {
            return;
        }

        if (!used && hp.getHealth() <= triggerHp) {
            used = true;
            dr.start(reduction, durationSec);

            if (closeAttackWhileDefending) {
                WeaponsStatsComponent w = entity.getComponent(WeaponsStatsComponent.class);
                if (w != null) w.setDisableDamage(true);
            }
        }

        // When the damage reduction effect ends (dr.isActive() is false), restore "Can cause damage"
        // Only need to restore if the "Attack ban during defense period" option is enabled
        if (used && !dr.isActive() && closeAttackWhileDefending) {
            WeaponsStatsComponent w = entity.getComponent(WeaponsStatsComponent.class);
            if (w != null) w.setDisableDamage(false);
        }
    }
}
