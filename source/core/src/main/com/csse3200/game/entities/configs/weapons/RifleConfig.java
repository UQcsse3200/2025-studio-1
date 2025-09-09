package com.csse3200.game.entities.configs.weapons;

public class RifleConfig extends RangedWeaponConfig {
    public RifleConfig() {
        this.ammoCapacity = 50;
        this.reloadTimer = 0.6;
        this.damage = 30;
        this.shootTimer = 0.3;
        this.texturePath = "images/rifle.png";
    }
}
