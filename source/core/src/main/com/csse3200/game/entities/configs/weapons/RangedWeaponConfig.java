package com.csse3200.game.entities.configs.weapons;

import com.csse3200.game.entities.configs.ItemTypes;

public abstract class RangedWeaponConfig extends WeaponConfig {
    public int ammoCapacity;
    public double reloadTimer;
    public double shootTimer;

    public RangedWeaponConfig() {
        this.weaponType = ItemTypes.RANGED;
    }
}
