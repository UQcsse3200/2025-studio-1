package com.csse3200.game.components.enemy;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.Component;

/**
 * Gives an attack buff to the entity when used.
 *
 * <p> Requires CombatStatsComponent on this entity.</p>
 */
public class LowHealthAttackBuff extends Component {
    private final int attackBuff;
    private final CombatStatsComponent stats;
    private boolean triggered = false;

    public LowHealthAttackBuff(int attackBuff, CombatStatsComponent stats) {
        this.attackBuff = attackBuff;
        this.stats = stats;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("buff", this::onBuff);
    }

    /**
     * Adds the attackBuff to the base attack of the entity.
     */
    private void onBuff() {
        if (triggered) {
            return;
        }
        stats.setBaseAttack(stats.getBaseAttack() + attackBuff);
        triggered = true;
    }
}
