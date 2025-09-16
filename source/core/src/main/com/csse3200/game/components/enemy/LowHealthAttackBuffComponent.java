package com.csse3200.game.components.enemy;

import com.csse3200.game.components.Component;
import com.csse3200.game.components.WeaponsStatsComponent;

/**
 * Gives an attack buff to the entity when used.
 *
 * <p> Requires CombatStatsComponent on this entity.</p>
 */
public class LowHealthAttackBuffComponent extends Component {
    private final int attackBuff;
    private final WeaponsStatsComponent stats;
    private boolean triggered = false;

    public LowHealthAttackBuffComponent(int attackBuff, WeaponsStatsComponent stats) {
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
