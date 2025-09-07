package com.csse3200.game.entities.configs.weapons;

import com.csse3200.game.entities.configs.ItemTypes;

public class MeleeWeaponConfig extends WeaponConfig {
    public ItemTypes weaponType = ItemTypes.MELEE;
    public int baseAttack;
    public double hitTimer;
    public float range;
}
