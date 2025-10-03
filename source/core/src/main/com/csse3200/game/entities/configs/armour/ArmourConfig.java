package com.csse3200.game.entities.configs.armour;

import com.csse3200.game.entities.configs.ItemTypes;

public abstract class ArmourConfig {
    public ItemTypes armourType = ItemTypes.ARMOUR;
    public int protection = 0;
    public String texturePath = "";
}
