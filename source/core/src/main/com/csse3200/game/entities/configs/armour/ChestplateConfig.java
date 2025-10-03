package com.csse3200.game.entities.configs.armour;

import com.csse3200.game.entities.configs.ItemTypes;

public class ChestplateConfig extends ArmourConfig {
    public ChestplateConfig() {
        armourType = ItemTypes.CHESTPLATE_ARMOUR;
        protection = 15;
        texturePath = "images/armour-assets/chestplate.png";
    }
}

