package com.csse3200.game.entities.configs.weapons;

import com.badlogic.gdx.math.Vector2;

public class PistolConfig extends RangedWeaponConfig {
    public PistolConfig() {
        ammoCapacity = 30;
        reloadTimer = 1.0;
        shootTimer = 0.5;
        damage = 10;
        texturePath = "images/pistol.png";
    }
}
