package com.csse3200.game.entities.configs.weapons;

import com.badlogic.gdx.math.Vector2;

/**
 * Configuration for the lightsaber weapon
 */
public class LightsaberConfig extends MeleeWeaponConfig {
    public LightsaberConfig() {
        damage = 10;
        hitTimer = 0.8;
        range = 3f;
        texturePath = "images/lightsaberSingle.png";
    }
}
