package com.csse3200.game.components.boss;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BossDefenseComponentTest {

    @Test
    void triggersAtThreshold_andExpiresNextUpdate() {
        ServiceLocator.registerTimeSource(new GameTime());

        Entity e = new Entity()
                .addComponent(new CombatStatsComponent(1000))
                .addComponent(new DamageReductionComponent())
                .addComponent(new WeaponsStatsComponent(5))
                // zero-duration so it expires on next update without mocking time
                .addComponent(new BossDefenseComponent(
                        0f,
                        1.0f,
                        300,
                        true));
        e.create();

        CombatStatsComponent hp = e.getComponent(CombatStatsComponent.class);
        DamageReductionComponent dr = e.getComponent(DamageReductionComponent.class);
        BossDefenseComponent def = e.getComponent(BossDefenseComponent.class);

        hp.setHealth(300);
        def.update();
        assertTrue(dr.isActive(), "should activate at threshold");

        dr.update();
        def.update();
        assertFalse(dr.isActive(), "should expire with zero duration");
    }
}

