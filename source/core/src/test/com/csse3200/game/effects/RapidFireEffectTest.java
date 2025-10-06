package com.csse3200.game.effects;

import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.entities.Entity;
import jdk.jfr.Description;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class RapidFireEffectTest {
    RapidFireEffect rapidFireEffect;
    WeaponsStatsComponent weaponsStatsComponent;

    @BeforeEach
    void setUp() {
        rapidFireEffect = new RapidFireEffect(10);
        weaponsStatsComponent = mock(WeaponsStatsComponent.class);
        when(weaponsStatsComponent.getCoolDown()).thenReturn(20f);
    }

    @Test
    void shouldApplyEffect() {
        Entity weapon = new Entity().addComponent(weaponsStatsComponent);
        assertTrue(rapidFireEffect.apply(weapon));
        assertTrue(rapidFireEffect.isActive());
    }

    @Test
    @Description("null WeaponStatsComponent")
    void shouldNotApplyEffect() {
        assertFalse(rapidFireEffect.apply(new Entity()));
        assertFalse(rapidFireEffect.isActive());
    }

    @Test
    void shouldFailIfAppliedTwice() {
        Entity weapon = new Entity().addComponent(weaponsStatsComponent);
        assertTrue(rapidFireEffect.apply(weapon));
        assertFalse(rapidFireEffect.apply(weapon));
    }

    @Test
    void shouldNotUpdateSuccessfully() {
        rapidFireEffect.update(1f);
        assertFalse(rapidFireEffect.isActive());
        verify(weaponsStatsComponent, never()).setCoolDown(anyFloat());
    }

    @Test
    void shouldNotFinishDuration() {
        Entity weapon = mock(Entity.class);
        when(weapon.getComponent(WeaponsStatsComponent.class)).thenReturn(weaponsStatsComponent);
        rapidFireEffect.apply(weapon);

        rapidFireEffect.update(1f);
        assertTrue(rapidFireEffect.isActive());
        verify(weaponsStatsComponent, never()).setCoolDown(20f);
    }

    @Test
    void shouldFinishDuration() {
        Entity weapon = mock(Entity.class);
        when(weapon.getComponent(WeaponsStatsComponent.class)).thenReturn(weaponsStatsComponent);
        rapidFireEffect.apply(weapon);

        rapidFireEffect.update(10f);
        assertFalse(rapidFireEffect.isActive());
        verify(weaponsStatsComponent, times(1)).setCoolDown(20f);
    }
}
