package com.csse3200.game.entities.configs.weapons;

public class DaggerConfig extends MeleeWeaponConfig {
    public DaggerConfig() {
        this.damage = 2;
        this.hitTimer = 0.8;
        this.range = 2f;
        this.texturePath = "images/dagger.png";
    }
}
