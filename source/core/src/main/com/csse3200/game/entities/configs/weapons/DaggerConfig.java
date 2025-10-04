package com.csse3200.game.entities.configs.weapons;

public class DaggerConfig extends MeleeWeaponConfig {
    public DaggerConfig() {
        damage = 2;
        hitTimer = 0.8;
        range = 2f;
        texturePath = "images/dagger.png";
        this.setName("dagger");
    }
}
