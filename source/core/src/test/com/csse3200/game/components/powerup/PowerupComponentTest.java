package com.csse3200.game.components.powerup;

import com.csse3200.game.components.MagazineComponent;
import com.csse3200.game.components.PowerupComponent;
import com.csse3200.game.components.player.InventoryComponent;
import com.csse3200.game.components.player.PlayerActions;
import com.csse3200.game.effects.AimbotEffect;
import com.csse3200.game.effects.DoubleProcessorsEffect;
import com.csse3200.game.effects.RapidFireEffect;
import com.csse3200.game.effects.UnlimitedAmmoEffect;
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
public class PowerupComponentTest {

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

    // rapidfire effect
    @Test
    void testRapidFireEffectAddSuccessfully() {
        when(rapidFireEffect.apply(weapon)).thenReturn(true);
        when(rapidFireEffect.isActive()).thenReturn(true);

        when(ServiceLocator.getTimeSource().getDeltaTime()).thenReturn(1f);

        powerupComponent.addEffect(rapidFireEffect);
        powerupComponent.update();

        verify(rapidFireEffect, times(1)).update(1f);
    }

    @Test
    void testAddRapidFireEffectFailsWhenApplyReturnsFalse() {
        when(rapidFireEffect.apply(weapon)).thenReturn(false);

        powerupComponent.addEffect(rapidFireEffect);
        powerupComponent.update();

        verify(rapidFireEffect, never()).update(anyFloat());
    }

    @Test
    void testRapidFireEffectRemovedAfterInactive() {
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
    void testNoRapidFireEffectAddedWithoutWeapon() {
        PowerupComponent noWeaponComponent = new PowerupComponent();

        noWeaponComponent.addEffect(rapidFireEffect);

        noWeaponComponent.update();

        verify(rapidFireEffect, never()).apply(any());
        verify(rapidFireEffect, never()).update(anyFloat());
    }

    // aimbot effect
    @Test
    void testAimbotEffectAddSuccessfully() {
        MagazineComponent magazine = new MagazineComponent(30);
        weapon.addComponent(magazine);

        AimbotEffect effect = new AimbotEffect(1f, mock(PlayerActions.class));

        powerupComponent.addEffect(effect);
        powerupComponent.update();

        assertTrue(effect.isActive());
    }

    @Test
    void testAimbotEffectFailsWithoutMagazine() {
        weapon = new Entity();
        powerupComponent.setEquippedWeapon(weapon);

        AimbotEffect effect = new AimbotEffect(1f, mock(PlayerActions.class));

        powerupComponent.addEffect(effect);
        powerupComponent.update();

        assertFalse(effect.isActive());
    }

    @Test
    void testAimbotEffectRemovedAfterDuration() {
        MagazineComponent magazine = new MagazineComponent(30);
        weapon.addComponent(magazine);

        AimbotEffect effect = new AimbotEffect(0f, mock(PlayerActions.class));

        powerupComponent.addEffect(effect);
        powerupComponent.update();

        assertFalse(effect.isActive());
    }

    // unlimited ammo effect
    @Test
    void testUnlimitedAmmoEffectAddSuccessfully() {
        MagazineComponent magazine = new MagazineComponent(10);
        weapon.addComponent(magazine);

        UnlimitedAmmoEffect effect = new UnlimitedAmmoEffect(1f, null);

        powerupComponent.addEffect(effect);
        powerupComponent.update();

        assertTrue(effect.isActive());
        assertEquals(10, magazine.getCurrentAmmo());
    }

    @Test
    void testUnlimitedAmmoEffectFailsWithoutMagazine() {
        Entity gunWithoutMagazine = new Entity();
        powerupComponent.setEquippedWeapon(gunWithoutMagazine);

        UnlimitedAmmoEffect effect = new UnlimitedAmmoEffect(1f, null);

        powerupComponent.addEffect(effect);
        powerupComponent.update();

        assertFalse(effect.isActive());
    }

    @Test
    void testUnlimitedAmmoEffectExpiresAfterDuration() {
        MagazineComponent magazine = new MagazineComponent(10);
        weapon.addComponent(magazine);

        UnlimitedAmmoEffect effect = new UnlimitedAmmoEffect(1f, null);

        powerupComponent.addEffect(effect);

        effect.update(1f);

        assertFalse(effect.isActive());
        assertEquals(9, magazine.getCurrentAmmo());
    }

    // double processors effect
    @Test
    void testDoubleProcessorsEffectAddSuccessfully() {
        InventoryComponent inventory = new InventoryComponent(0);
        weapon.addComponent(inventory);
        powerupComponent.setEquippedWeapon(weapon);

        DoubleProcessorsEffect effect = new DoubleProcessorsEffect(1f);

        powerupComponent.addEffect(effect);
        powerupComponent.update();

        assertTrue(inventory.hasDoubleProcessors());
        assertTrue(effect.isActive());
    }

    @Test
    void testDoubleProcessorsEffectFailsWithoutInventory() {
        weapon = new Entity();
        powerupComponent.setEquippedWeapon(weapon);

        DoubleProcessorsEffect effect = new DoubleProcessorsEffect(1f);

        powerupComponent.addEffect(effect);
        powerupComponent.update();

        assertFalse(effect.isActive());
    }

    @Test
    void testDoubleProcessorsEffectRemovedAfterInactive() {
        InventoryComponent inventory = new InventoryComponent(0);
        weapon.addComponent(inventory);
        powerupComponent.setEquippedWeapon(weapon);

        DoubleProcessorsEffect effect = new DoubleProcessorsEffect(0f);

        powerupComponent.addEffect(effect);
        powerupComponent.update();

        assertFalse(effect.isActive());
    }

    @AfterEach
    void cleanup() {
        ServiceLocator.clear();
    }
}