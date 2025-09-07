package com.csse3200.game.entities.configs.weapons;

import com.csse3200.game.entities.configs.ItemTypes;

public class RangedWeaponConfig extends WeaponConfig {
    public ItemTypes weaponType = ItemTypes.RANGED;
    public int ammoCapacity;
    public double reloadTimer;
    public double shootTimer;
}
