package com.csse3200.game.components;

import com.csse3200.game.effects.RapidFireEffect;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.GameTime;
import com.csse3200.game.services.ServiceLocator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PowerupComponentTest {

    PowerupComponent powerupComponent;
    Entity weapon;

    @Mock
    GameTime timeSource;

    @Mock
    RapidFireEffect rapidFireEffect;

    @BeforeEach
    void setup() {
        weapon = new Entity();
        powerupComponent = new PowerupComponent();
        powerupComponent.setEquippedWeapon(weapon);

        ServiceLocator.registerTimeSource(timeSource);
    }

    @Test
    void testSetAndGetEquippedWeapon() {
        assertEquals(weapon, powerupComponent.getEquippedWeapon());

        Entity newWeapon = new Entity();
        powerupComponent.setEquippedWeapon(newWeapon);

        assertEquals(newWeapon, powerupComponent.getEquippedWeapon());
    }

    @Test
    void testAddEffectSuccessfully() {
        when(rapidFireEffect.apply(weapon)).thenReturn(true);
        when(rapidFireEffect.isActive()).thenReturn(true);

        when(ServiceLocator.getTimeSource().getDeltaTime()).thenReturn(1f);

        powerupComponent.addEffect(rapidFireEffect);
        powerupComponent.update();

        verify(rapidFireEffect, times(1)).update(1f);
    }

    @Test
    void testAddEffectFailsWhenApplyReturnsFalse() {
        when(rapidFireEffect.apply(weapon)).thenReturn(false);

        powerupComponent.addEffect(rapidFireEffect);
        powerupComponent.update();

        verify(rapidFireEffect, never()).update(anyFloat());
    }

    @Test
    void testEffectRemovedAfterInactive() {
        ServiceLocator.clear();
        ServiceLocator.registerTimeSource(timeSource);
        when(timeSource.getDeltaTime()).thenReturn(1f);

        when(rapidFireEffect.apply(weapon)).thenReturn(true);
        when(rapidFireEffect.isActive()).thenReturn(false);

        powerupComponent.addEffect(rapidFireEffect);
        powerupComponent.update();

        verify(rapidFireEffect).update(1f);
        verify(rapidFireEffect).isActive();

        // nothing should happen since it was removed
        powerupComponent.update();
        verifyNoMoreInteractions(rapidFireEffect);
    }

    @Test
    void testNoEffectAddedWithoutWeapon() {
        PowerupComponent noWeaponComponent = new PowerupComponent();

        noWeaponComponent.addEffect(rapidFireEffect);

        noWeaponComponent.update();

        verify(rapidFireEffect, never()).apply(any());
        verify(rapidFireEffect, never()).update(anyFloat());
    }

    @AfterEach
    void cleanup() {
        ServiceLocator.clear();
    }
}
