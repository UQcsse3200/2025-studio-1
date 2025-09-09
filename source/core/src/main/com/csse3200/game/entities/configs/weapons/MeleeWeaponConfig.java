package com.csse3200.game.entities.configs.weapons;

import com.csse3200.game.entities.configs.ItemTypes;

public class MeleeWeaponConfig extends WeaponConfig {
    public double hitTimer;
    public float range;

    public MeleeWeaponConfig() {
        this.weaponType = ItemTypes.MELEE;
    }
}
