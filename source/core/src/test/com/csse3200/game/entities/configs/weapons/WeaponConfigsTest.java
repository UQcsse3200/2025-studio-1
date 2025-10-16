package com.csse3200.game.entities.configs.weapons;

import com.csse3200.game.entities.configs.ItemTypes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WeaponConfigsTest {
    @Test
    void rifleConfigTest() {
        RangedWeaponConfig config = new RifleConfig();
        assertNotNull(config.projectileTexturePath);
        assertNotNull(config.texturePath);
        assertTrue(config.reloadTimer >= 0);
        assertTrue(config.ammoCapacity >= 0);
        assertTrue(config.shootTimer >= 0);
        assertTrue(config.damage >= 0);
        assertEquals(ItemTypes.RANGED, config.weaponType);
    }

    @Test
    void pistolConfigTest() {
        RangedWeaponConfig config = new PistolConfig();
        assertNotNull(config.projectileTexturePath);
        assertNotNull(config.texturePath);
        assertTrue(config.reloadTimer >= 0);
        assertTrue(config.ammoCapacity >= 0);
        assertTrue(config.shootTimer >= 0);
        assertTrue(config.damage >= 0);
        assertEquals(ItemTypes.RANGED, config.weaponType);
    }

    @Test
    void daggerConfigTest() {
        MeleeWeaponConfig config = new DaggerConfig();
        assertNotNull(config.texturePath);
        assertEquals(ItemTypes.MELEE, config.weaponType);
        assertTrue(config.damage >= 0);
        assertTrue(config.hitTimer >= 0);
        assertTrue(config.range >= 0);

    }

    @Test
    void LightsaberConfigTest() {
        MeleeWeaponConfig config = new LightsaberConfig();
        assertNotNull(config.texturePath);
        assertEquals(ItemTypes.MELEE, config.weaponType);
        assertTrue(config.damage >= 0);
        assertTrue(config.hitTimer >= 0);
        assertTrue(config.range >= 0);
    }

    @Test
    void launcherConfigTest() {
        RangedWeaponConfig config = new LauncherConfig();
        assertNotNull(config.projectileTexturePath);
        assertNotNull(config.texturePath);
        assertTrue(config.reloadTimer >= 0);
        assertTrue(config.ammoCapacity >= 0);
        assertTrue(config.shootTimer >= 0);
        assertTrue(config.damage >= 0);
        assertEquals(ItemTypes.RANGED, config.weaponType);
    }
}
