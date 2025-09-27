package com.csse3200.game.entities.configs.weapons;

import com.badlogic.gdx.math.Vector2;

public class RifleConfig extends RangedWeaponConfig {
    public RifleConfig() {
        ammoCapacity = 50;
        reloadTimer = 0.6;
        damage = 30;
        shootTimer = 0.3;
        texturePath = "images/rifle.png";
    }
}
