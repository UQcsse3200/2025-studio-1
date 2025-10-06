package com.csse3200.game.components;

import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.ItemTypes;
import com.csse3200.game.entities.configs.armour.ArmourConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * This component is used to attach armour to the player, and update the armour's position,
 * so it follows the player.
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

    /**
     * Equips the armour onto the entity.
     * @param player
     */
    public void equip(Entity player, Entity armour) {
        if (player == null || armour == null) {
            return;
        }
    }


}
