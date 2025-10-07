package com.csse3200.game.entities.factories.items;

import com.csse3200.game.components.ArmourComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.*;
import com.csse3200.game.entities.configs.armour.ArmourConfig;

/**
 * Utility factory class for creating armour entities in the game.
 * <p>
 */
public class ArmourFactory {

    /**
     * Creates and configures a new armour entity.
     * The armour has a texture, physics, and other needed parts.
     * This class is called by other Factories, to add other components as needed.
     *
     * @param armourType Member of Armour enum that represents the armour type.
     * @return entity representing an item
     */
    public static Entity createArmour(Armour armourType) {
        ArmourConfig config = armourType.getConfig();
        Entity armour = ItemFactory.createItem(config.texturePath);
        armour.addComponent(new ArmourComponent(config));
        armour.scaleHeight(armour.getComponent(ArmourComponent.class).heightScale);
        return armour;
    }

    /**
     * Stops you from making an ItemFactory object.
     * If you try, it throws an error.
     */
    private ArmourFactory() {
        throw new IllegalStateException("Instantiating static util class");
    }
}
