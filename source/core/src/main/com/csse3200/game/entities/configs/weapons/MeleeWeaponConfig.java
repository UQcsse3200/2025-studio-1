package com.csse3200.game.entities.configs.weapons;

import com.csse3200.game.entities.configs.ItemTypes;

public abstract class MeleeWeaponConfig extends WeaponConfig {
    public double hitTimer = 1.0;
    public float range = 1.5f;

    protected MeleeWeaponConfig() {
        weaponType = ItemTypes.MELEE;
    }
}
