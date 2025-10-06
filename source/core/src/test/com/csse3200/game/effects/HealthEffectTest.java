package com.csse3200.game.effects;

import com.csse3200.game.components.CombatStatsComponent;
import com.csse3200.game.entities.Entity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HealthEffectTest {
    HealthEffect healthEffect;

    @BeforeEach
    void setup() {
        healthEffect = new HealthEffect(10);
    }

    @Test
    void shouldApplyEffectOnEntityWithComponent() {
        CombatStatsComponent component = new CombatStatsComponent(100);
        Entity player = new Entity().addComponent(component);

        assertTrue(healthEffect.apply(player));
        assertEquals(90, component.getHealth());
    }

    @Test
    void shouldApplyEffectOnEntityWithoutComponent() {
        Entity player = new Entity();

        assertFalse(healthEffect.apply(player));
    }
}
