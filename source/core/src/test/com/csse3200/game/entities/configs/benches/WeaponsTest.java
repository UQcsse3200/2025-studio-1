package com.csse3200.game.entities.configs.benches;

import com.csse3200.game.entities.configs.Weapons;
import com.csse3200.game.entities.configs.weapons.DaggerConfig;
import com.csse3200.game.entities.configs.weapons.LightsaberConfig;
import com.csse3200.game.entities.configs.weapons.PistolConfig;
import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class WeaponsTest {
    WeaponConfig lightsaber;
    WeaponConfig dagger;
    WeaponConfig rifle;
    WeaponConfig pistol;

    @BeforeEach
    void setup() {
        lightsaber = Weapons.LIGHTSABER.getConfig();
        dagger = Weapons.DAGGER.getConfig();
        rifle = Weapons.RIFLE.getConfig();
        pistol = Weapons.PISTOL.getConfig();
    }

    @Test
    void setupCorrect() {
        assertInstanceOf(LightsaberConfig.class, lightsaber);
        assertInstanceOf(DaggerConfig.class, dagger);
        assertInstanceOf(WeaponConfig.class, rifle);
        assertInstanceOf(PistolConfig.class, pistol);
    }
}
