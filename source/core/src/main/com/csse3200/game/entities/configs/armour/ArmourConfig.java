package com.csse3200.game.entities.configs.armour;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.configs.ItemTypes;

/**
 * Stores information about the armour type.
 */
public abstract class ArmourConfig {
    public ItemTypes armourType = ItemTypes.ARMOUR;
    public int protection = 0;
    public String texturePath = "";
    public Vector2 offset;
    public float heightScale;
}
