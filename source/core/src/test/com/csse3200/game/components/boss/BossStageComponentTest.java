package com.csse3200.game.components.boss;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.components.enemy.BlackholeComponent;
import com.csse3200.game.components.enemy.BossChargeSkillComponent;
import com.csse3200.game.components.enemy.FireballAttackComponent;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class BossStageComponentTest {

    @Before
    public void setUp() {
        ServiceLocator.registerTimeSource(new GameTime());
    }

    @After
    public void tearDown() {
        try { ServiceLocator.clear(); } catch (Throwable ignored) {}
    }

    private static boolean getBool(Object obj, String field) throws Exception {
        var f = obj.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.getBoolean(obj);
    }

    @Test
    public void stage2_enablesCharge_disablesBlackhole() throws Exception {
        Entity e = new Entity();
        var stats = new CombatStatsComponent(100);
        var blackhole = new BlackholeComponent(null, 1f, 5f);
        var charge = new BossChargeSkillComponent(
                null, 6f, 0.1f, 0.1f, 10f, 0.5f, 1f,
                0f, 10f, 5f, 2f
        );
        var fireball = new FireballAttackComponent(null, 1f, 8f, 6f, 5);
        var missile = new MissueAttackComponent();
        var stage = new BossStageComponent(e);

        e.addComponent(stats)
                .addComponent(blackhole)
                .addComponent(charge)
                .addComponent(fireball)
                .addComponent(missile)
                .addComponent(stage);
        e.create();

        assertTrue(getBool(blackhole, "attack"));
        assertFalse(getBool(charge, "crash"));

        stats.setHealth(50);
        stage.update();

        assertFalse(getBool(blackhole, "attack"));
        assertTrue(getBool(charge, "crash"));
    }

    @Test
    public void stage3_disablesFireball_enablesMissile_disablesCharge() throws Exception {
        Entity e = new Entity();
        var stats = new CombatStatsComponent(100);
        var blackhole = new BlackholeComponent(null, 1f, 5f);
        var charge = new BossChargeSkillComponent(
                null, 6f, 0.1f, 0.1f, 10f, 0.5f, 1f,
                0f, 10f, 5f, 2f
        );
        var fireball = new FireballAttackComponent(null, 1f, 8f, 6f, 5);
        var missile = new MissueAttackComponent();
        var stage = new BossStageComponent(e);

        e.addComponent(stats)
                .addComponent(blackhole)
                .addComponent(charge)
                .addComponent(fireball)
                .addComponent(missile)
                .addComponent(stage);
        e.create();
        stats.setHealth(50);
        stage.update();
        stats.setHealth(30);
        stage.update();
        assertFalse(getBool(fireball, "attack"));
        assertFalse(getBool(blackhole, "attack"));
        assertTrue(getBool(missile, "attack"));
        assertFalse(getBool(charge, "attack"));
    }
}