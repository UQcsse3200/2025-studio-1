package com.csse3200.game.entities.configs;

import com.badlogic.gdx.math.Vector2;
import static com.csse3200.game.entities.configs.Weapons.*;


public enum ItemTypes {
    NONE("none"),
    MELEE("melee"),
    RANGED("ranged"),
    PROJECTILE("projectile"),
    CONSUMABLE("consumable"),
    BENCH("bench"),
    HEALTH_BENCH("healthbench"),
    COMPUTER_BENCH("computerbench"),
    SPEED_BENCH("speedbench"),
    //added weapon types
    LIGHTSABER("lightsaber"),
    DAGGER("dagger"),
    RIFLE("rifle"),
    PISTOL("pistol");

    private final String typeName;

    ItemTypes(String typeName) {
        this.typeName = typeName;
    }

    /**
     * Returns the string representation of this item type.
     *
     * @return the string value associated with this {@link ItemTypes} constant
     */
    public String getTypeName() {
        return typeName;
    }


}
