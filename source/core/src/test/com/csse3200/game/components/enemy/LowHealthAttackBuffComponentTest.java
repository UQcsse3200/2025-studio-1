package com.csse3200.game.components.enemy;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LowHealthAttackBuffComponentTest {

    @Test
    @DisplayName("Buffs attack at threshold health")
    void buffsAttackAtThreshold() {
        WeaponsStatsComponent stats = new WeaponsStatsComponent(10);
        Entity ghostGpt = new Entity()
                .addComponent(new CombatStatsComponent(100))
                .addComponent(stats)
                .addComponent(new LowHealthAttackBuffComponent(10, stats));

        ghostGpt.create();

        ghostGpt.getComponent(CombatStatsComponent.class).setHealth(20);

        assertEquals(20, ghostGpt.getComponent(CombatStatsComponent.class).getHealth());
        assertEquals(20, ghostGpt.getComponent(WeaponsStatsComponent.class).getBaseAttack());
    }

    @Test
    @DisplayName("Buff applied only once")
    void buffAppliedOnlyOnce() {
        WeaponsStatsComponent stats = new WeaponsStatsComponent(10);
        Entity ghostGpt = new Entity()
                .addComponent(new CombatStatsComponent(100))
                .addComponent(stats)
                .addComponent(new LowHealthAttackBuffComponent(10, stats));
        ghostGpt.create();

        ghostGpt.getComponent(CombatStatsComponent.class).setHealth(20);

        assertEquals(20, ghostGpt.getComponent(CombatStatsComponent.class).getHealth());
        assertEquals(20, ghostGpt.getComponent(WeaponsStatsComponent.class).getBaseAttack());

        ghostGpt.getComponent(CombatStatsComponent.class).setHealth(10);

        assertEquals(10, ghostGpt.getComponent(CombatStatsComponent.class).getHealth());
        assertEquals(20, ghostGpt.getComponent(WeaponsStatsComponent.class).getBaseAttack());
    }
}
