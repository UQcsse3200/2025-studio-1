package com.csse3200.game.entities.configs.armour;

import com.csse3200.game.entities.configs.ItemTypes;

public class HoodConfig extends ArmourConfig {
    public HoodConfig() {
        armourType = ItemTypes.HOOD_ARMOUR;
        protection = 5;
        texturePath = "images/armour-assets/hood.png";
    }
}
