package com.csse3200.game.entities.configs.weapons;

import com.csse3200.game.entities.configs.ItemTypes;

public abstract class WeaponConfig {
    public ItemTypes weaponType = ItemTypes.NONE;
    public int damage = 0;
    public String texturePath = "";
}
