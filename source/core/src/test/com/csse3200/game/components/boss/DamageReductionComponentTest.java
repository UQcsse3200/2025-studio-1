package com.csse3200.game.components.boss;

import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DamageReductionComponentTest {

    @BeforeEach
    void setupTime() {
        ServiceLocator.registerTimeSource(new GameTime());
    }

    @Test
    void inactive_keepsIncoming() {
        DamageReductionComponent dr = new DamageReductionComponent();
        assertFalse(dr.isActive());
        assertEquals(0, dr.apply(0));
        assertEquals(10, dr.apply(10));
    }

    @Test
    void fullImmunity_returnsZero() {
        DamageReductionComponent dr = new DamageReductionComponent();
        dr.start(1.0f, 10f);
        assertTrue(dr.isActive());
        assertEquals(0, dr.apply(1));
        assertEquals(0, dr.apply(100));
    }

    @Test
    void partialReduction_rounds() {
        DamageReductionComponent dr = new DamageReductionComponent();
        dr.start(0.25f, 5f);
        assertEquals(8, dr.apply(10));
        dr.stop();
        assertFalse(dr.isActive());
        assertEquals(10, dr.apply(10));
    }

    @Test
    void expiresImmediately_whenDurationZero() {
        DamageReductionComponent dr = new DamageReductionComponent();
        dr.start(1.0f, 0f);
        assertTrue(dr.isActive());
        dr.update();
        assertFalse(dr.isActive());
    }
}

