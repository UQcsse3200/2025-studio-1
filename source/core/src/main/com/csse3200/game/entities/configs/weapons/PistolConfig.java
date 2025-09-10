package com.csse3200.game.entities.configs.weapons;

public class PistolConfig extends RangedWeaponConfig {
    public PistolConfig() {
        this.ammoCapacity = 30;
        this.reloadTimer = 1.0;
        this.shootTimer = 0.5;
        this.damage = 10;
        this.texturePath = "images/pistol.png";
    }
}
