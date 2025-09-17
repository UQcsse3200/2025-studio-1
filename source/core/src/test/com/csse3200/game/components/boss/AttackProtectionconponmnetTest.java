package com.csse3200.game.components.boss;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests AttackProtectionComponent together with DamageReductionComponent and CombatStatsComponent.
 * Uses a real GameTime via ServiceLocator. Time expiry of no-damage is not covered here.
 */
public class AttackProtectionconponmnetTest {
    @Before
    public void setup() {
        // Ensure a time source exists for DamageReductionComponent.start(...)
        ServiceLocator.registerTimeSource(new GameTime());
    }
    @After
    public void teardown() {
        try { ServiceLocator.clear(); } catch (Throwable ignored) {}
    }
    /**
     * After the hit limit of consecutive health drops is reached, no-damage starts.
     * While active, takeDamage() should not reduce health.
     */
    @Test
    public void triggersNoDamageAfterLimitHits_andBlocksDamage() {
        Entity e = new Entity();
        CombatStatsComponent stats = new CombatStatsComponent(100);
        DamageReductionComponent dr = new DamageReductionComponent();
        AttackProtectionComponent ap = new AttackProtectionComponent(3, 2f); // 3 hits -> no-damage
        e.addComponent(stats);
        e.addComponent(dr);
        e.addComponent(ap);
        e.create();
        int h0 = stats.getHealth();
        assertFalse(dr.isActive());
        // Seed lastHealth with a no-op update; should not count as a hit.
        stats.setHealth(h0);
        // Three real decrease -> should trigger no-damage
        stats.setHealth(h0 - 1);
        assertFalse(dr.isActive());
        stats.setHealth(h0 - 2);
        assertFalse(dr.isActive());
        stats.setHealth(h0 - 3);
        assertTrue("DR should be active after reaching hit limit", dr.isActive());
        int before = stats.getHealth();
        stats.takeDamage(50);
        assertEquals("Health must not change during no-damage window", before, stats.getHealth());
    }

    /**
     * Healing or equal health should not count as hits.
     * Only strict decreases (cur < lastHealth) increment the counter.
     */
    @Test
    public void healOrSameHealthDoesNotCountAsHit() {
        Entity e = new Entity();
        CombatStatsComponent stats = new CombatStatsComponent(50);
        DamageReductionComponent dr = new DamageReductionComponent();
        AttackProtectionComponent ap = new AttackProtectionComponent(2, 1f); // 2 hits -> no-damage
        e.addComponent(stats);
        e.addComponent(dr);
        e.addComponent(ap);
        e.create();
        int h = stats.getHealth();
        assertEquals(50, h);
        stats.setHealth(h);
        assertFalse(dr.isActive());
        stats.setHealth(h - 1);
        assertFalse(dr.isActive());
        stats.setHealth(h);
        assertFalse(dr.isActive());
        stats.setHealth(h - 1);
        assertTrue("DR should activate after second real decrease", dr.isActive());
    }
}