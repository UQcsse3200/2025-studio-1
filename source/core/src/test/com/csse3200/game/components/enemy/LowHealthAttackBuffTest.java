package com.csse3200.game.components.enemy;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LowHealthAttackBuffTest {

    @Test
    @DisplayName("Buffs attack at threshold health")
    void buffsAttackAtThreshold() {
        CombatStatsComponent stats = new CombatStatsComponent(100, 10);
        Entity ghostGpt = new Entity()
                .addComponent(stats)
                .addComponent(new LowHealthAttackBuff(10, stats));

        ghostGpt.create();

        ghostGpt.getComponent(CombatStatsComponent.class).setHealth(20);

        assertEquals(20, ghostGpt.getComponent(CombatStatsComponent.class).getHealth());
        assertEquals(20, ghostGpt.getComponent(CombatStatsComponent.class).getBaseAttack());
    }

    @Test
    @DisplayName("Buff applied only once")
    void buffAppliedOnlyOnce() {
        CombatStatsComponent stats = new CombatStatsComponent(100, 10);
        Entity ghostGpt = new Entity()
                .addComponent(stats)
                .addComponent(new LowHealthAttackBuff(10, stats));
        ghostGpt.create();

        ghostGpt.getComponent(CombatStatsComponent.class).setHealth(20);

        assertEquals(20, ghostGpt.getComponent(CombatStatsComponent.class).getHealth());
        assertEquals(20, ghostGpt.getComponent(CombatStatsComponent.class).getBaseAttack());

        ghostGpt.getComponent(CombatStatsComponent.class).setHealth(10);

        assertEquals(10, ghostGpt.getComponent(CombatStatsComponent.class).getHealth());
        assertEquals(20, ghostGpt.getComponent(CombatStatsComponent.class).getBaseAttack());
    }
}
