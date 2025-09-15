package com.csse3200.game.entities.configs.weapons;

import com.csse3200.game.entities.configs.ItemTypes;

public abstract class RangedWeaponConfig extends WeaponConfig {
    public int ammoCapacity = 1;
    public double reloadTimer = 1.0;
    public double shootTimer = 1.0;
    public String projectileTexturePath = "images/round.png";

    protected RangedWeaponConfig() {
        weaponType = ItemTypes.RANGED;
    }
}
