package com.csse3200.game.components;

import com.badlogic.gdx.math.Vector2;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.armour.ArmourConfig;

/**
 * This component stores information about the armour.
 */
public class ArmourComponent extends Component{
    public ItemTypes armourType;
    public int protection;
    public String texturePath;
    public Vector2 rightOffset;
    public Vector2 leftOffset;
    public float heightScale;

    public ArmourComponent(ArmourConfig config) {
        this.armourType = config.armourType;
        this.protection = config.protection;
        this.texturePath = config.texturePath;
        this.rightOffset = config.rightOffset;
        this.leftOffset = config.leftOffset;
        this.heightScale = config.heightScale;
    }
}
