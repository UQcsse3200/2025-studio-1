package com.csse3200.game.components.boss;

import com.csse3200.game.components.Component;

/**
 * AttackProtectionCounterComponent
 *
 * Listens to "updateHealth" from CombatStatsComponent.
 * - Each time health decreases, count one hit.
 * - When hit count reaches limit, start no-damage via DamageReductionComponent.start(1f, seconds).
 */
public class AttackProtectionComponent extends Component {
    private int lastHealth = Integer.MIN_VALUE;
    private int hits = 0;

    private int limit;      // hits required to trigger
    private float seconds;  // no-damage duration

    public AttackProtectionComponent() {
        this(6, 2f);
    }

    public AttackProtectionComponent(int limit, float seconds) {
        // simple lower bounds without Math.max
        if (limit < 1) limit = 1;
        if (seconds < 0f) seconds = 0f;
        this.limit = limit;
        this.seconds = seconds;
    }

    @Override
    public void create() {
        entity.getEvents().addListener("updateHealth", this::onHealthChanged);
    }

    private void onHealthChanged(int cur) {
        if (lastHealth == Integer.MIN_VALUE) {
            lastHealth = cur;
            return;
        }
        if (cur < lastHealth) { // took real damage
            hits++;
            if (hits >= limit) {
                DamageReductionComponent dr = entity.getComponent(DamageReductionComponent.class);
                if (dr != null) {
                    dr.start(1f, seconds);
                }
                hits = 0;
            }
        }
        lastHealth = cur;
    }
}