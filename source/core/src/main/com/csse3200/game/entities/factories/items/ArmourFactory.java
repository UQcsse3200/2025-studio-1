package com.csse3200.game.entities.factories.items;

import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.csse3200.game.components.WeaponsStatsComponent;
import com.csse3200.game.components.items.ItemComponent;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.entities.configs.*;
import com.csse3200.game.entities.configs.armour.ArmourConfig;
import com.csse3200.game.entities.configs.weapons.WeaponConfig;
import com.csse3200.game.entities.factories.items.ItemFactory;
import com.csse3200.game.physics.components.HitboxComponent;
import com.csse3200.game.physics.components.PhysicsComponent;
import com.csse3200.game.rendering.TextureRenderComponent;

/**
 * Utility factory class for creating armour entities in the game.
 * <p>
 */
public class ArmourFactory {

    /**
     * Creates and configures a new item entity.
     * The item has a texture, physics, and other needed parts.
     * This class is called by other Factories, to add other components as needed.
     *
     * @param
     * @return entity representing an item
     */
    public static Entity createArmour(Armour armourType) {
        ArmourConfig config = armourType.getConfig();
        Entity armour = ItemFactory.createItem(config.texturePath);

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
