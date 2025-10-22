package com.csse3200.game.components.boss;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for IndividualCocoonComponent
 */
@ExtendWith(MockitoExtension.class)
class IndividualCocoonComponentTest {

    private Entity cocoon;
    private CombatStatsComponent combatStats;

    @BeforeEach
    void setUp() {
        cocoon = new Entity();
        // Use real CombatStatsComponent instead of mock
        combatStats = new CombatStatsComponent(20);

        // Mock Gdx.app
        Gdx.app = mock(Application.class);
    }

    @Test
    void testInit() {
        IndividualCocoonComponent component = new IndividualCocoonComponent();
        assertFalse(component.isDestroyed());
    }

    @Test
    void testDeathEvent() {
        cocoon.addComponent(combatStats);

        IndividualCocoonComponent component = new IndividualCocoonComponent();
        cocoon.addComponent(component);
        cocoon.create();

        cocoon.getEvents().trigger("death");

        assertTrue(component.isDestroyed());
    }

    @Test
    void testZeroHealth() {
        combatStats.setHealth(0);
        cocoon.addComponent(combatStats);

        IndividualCocoonComponent component = new IndividualCocoonComponent();
        cocoon.addComponent(component);
        cocoon.create();

        component.update();

        assertTrue(component.isDestroyed());
    }

    @Test
    void testPositiveHealth() {
        combatStats.setHealth(10);
        cocoon.addComponent(combatStats);

        IndividualCocoonComponent component = new IndividualCocoonComponent();
        cocoon.addComponent(component);
        cocoon.create();

        component.update();

        assertFalse(component.isDestroyed());
    }

    @Test
    void testIsDestroyed() {
        IndividualCocoonComponent component = new IndividualCocoonComponent();
        assertFalse(component.isDestroyed());
    }

    @Test
    void testHitEvent() {
        combatStats.setHealth(15);
        cocoon.addComponent(combatStats);

        IndividualCocoonComponent component = new IndividualCocoonComponent();
        cocoon.addComponent(component);
        cocoon.create();

        // Trigger hit - should not crash
        cocoon.getEvents().trigger("hit");

        assertFalse(component.isDestroyed());
    }

    @Test
    void testMultipleDeath() {
        cocoon.addComponent(combatStats);

        IndividualCocoonComponent component = new IndividualCocoonComponent();
        cocoon.addComponent(component);
        cocoon.create();

        cocoon.getEvents().trigger("death");
        cocoon.getEvents().trigger("death");

        assertTrue(component.isDestroyed());
    }
}