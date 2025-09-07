package com.csse3200.game.entities.configs.weapons;

/**
 * Configuration for the lightsaber weapon
 */
public class LightsaberConfig extends MeleeWeaponConfig {
    public LightsaberConfig() {
        this.baseAttack = 10;
        this.hitTimer = 0.8;
        this.range = 3f;
        this.texturePath = "images/lightsaberSingle.png";
    }
}
