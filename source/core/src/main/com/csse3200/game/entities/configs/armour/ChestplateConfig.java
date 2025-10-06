package com.csse3200.game.entities.configs.armour;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.configs.ItemTypes;

public class ChestplateConfig extends ArmourConfig {
    public ChestplateConfig() {
        armourType = ItemTypes.CHESTPLATE_ARMOUR;
        protection = 15;
        texturePath = "images/armour-assets/chestplate.png";
        offset = new Vector2(0.9f, 0.3f);
        heightScale = 0.3f;
    }
}

