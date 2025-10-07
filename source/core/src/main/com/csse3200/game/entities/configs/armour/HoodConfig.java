package com.csse3200.game.entities.configs.armour;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.configs.ItemTypes;

public class HoodConfig extends ArmourConfig {
    public HoodConfig() {
        armourType = ItemTypes.HOOD_ARMOUR;
        protection = 5;
        texturePath = "images/armour-assets/hood.png";
        rightOffset = new Vector2(0.7f, 0.5f);
        leftOffset = new Vector2(1.3f, 0.5f);
        heightScale = 0.6f;
    }
}
