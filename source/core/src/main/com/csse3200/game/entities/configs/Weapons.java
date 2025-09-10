package com.csse3200.game.entities.configs;

import com.csse3200.game.entities.configs.weapons.*;

public enum Weapons {
    LIGHTSABER(new LightsaberConfig()),
    DAGGER(new DaggerConfig()),
    RIFLE(new RifleConfig()),
    PISTOL(new PistolConfig());

    private final WeaponConfig config;

    Weapons(WeaponConfig config) {
        this.config = config;
    }

    /**
     * Returns the configuration details associated with this weapon type.
     *
     * @return the {@link WeaponConfig} for this weapon
     */
    public WeaponConfig getConfig() {
        return config;
    }
}
