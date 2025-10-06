package com.csse3200.game.effects;

import jdk.jfr.Description;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AreaEffectTest {
    AreaEffect areaEffect;

    @Test
    @Description("Tests to see if an appropriate IllegalArgumentException is thrown")
    void testAreaEffectIsNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            areaEffect = new AreaEffect(null, 1);
        });
    }

    @Test
    @Description("Tests to see if an appropriate IllegalArgumentException is thrown")
    void testAreaEffectIsEmpty() {
        try {
            assertThrows(IllegalArgumentException.class, () -> {
                areaEffect = new AreaEffect(new ArrayList<>(), 1);
            });
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Description("Tests to see if an appropriate IllegalArgumentException is thrown")
    void testAreaEffectContainsNull() {
        ArrayList<Effect> effects = new ArrayList<>();
        effects.add(new HealthEffect(1));
        effects.add(null);

        assertThrows(IllegalArgumentException.class, () -> {
            areaEffect = new AreaEffect(effects, 1);
        });
    }

    @Test
    @Description("Tests to see if an appropriate IllegalArgumentException is thrown")
    void testCircularReference() {
        ArrayList<Effect> dummy = new ArrayList<>(List.of(new HealthEffect(1)));

        ArrayList<Effect> effects = new ArrayList<>();
        effects.add(new AreaEffect(dummy, 1));

        assertThrows(IllegalArgumentException.class, () -> {
            areaEffect = new AreaEffect(effects, 1);
        });
    }

    @Test
    @Description("Tests to see if an appropriate IllegalArgumentException is thrown")
    void testInvalidRadius() {
        ArrayList<Effect> effects = new ArrayList<>();
        effects.add(new HealthEffect(1));

        assertThrows(IllegalArgumentException.class, () -> {
            areaEffect = new AreaEffect(effects, 0);
        });
    }

    @Test
    void shouldGetEffects() {
        ArrayList<Effect> effects = new ArrayList<>();
        effects.add(new HealthEffect(1));
        effects.add(new RapidFireEffect(1));
        areaEffect = new AreaEffect(effects, 1);

        assertEquals(effects, areaEffect.getEffects());
    }

    @Test
    void shouldGetRadius() {
        ArrayList<Effect> effects = new ArrayList<>();
        effects.add(new HealthEffect(1));
        effects.add(new RapidFireEffect(1));
        areaEffect = new AreaEffect(effects, 1);

        assertEquals(1, areaEffect.getRadius());
    }

    @Test //TODO should apply the effect to entities
    void shouldApplyEffects() { }
}