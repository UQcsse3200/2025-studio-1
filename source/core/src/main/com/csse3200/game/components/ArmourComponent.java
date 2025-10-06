package com.csse3200.game.components;

import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.armour.ArmourConfig;

/**
 * This component stores information about the armour.
 */
public class ArmourComponent extends Component{
    public ItemTypes armourType;
    public int protection;
    public String texturePath;

    public ArmourComponent(ArmourConfig config) {
        this.armourType = config.armourType;
        this.protection = config.protection;
        this.texturePath = config.texturePath;
    }
}
