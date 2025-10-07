package com.csse3200.game.entities.configs.weapons;

public class LauncherConfig extends RangedWeaponConfig {
    public LauncherConfig() {
        ammoCapacity = 1;
        reloadTimer = 2;
        damage = 80;
        shootTimer = 2;
        texturePath = "images/rocketlauncher.png";
        projectileTexturePath = "images/rocket.png";
    }

}
