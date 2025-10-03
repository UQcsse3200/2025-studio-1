package com.csse3200.game.entities.configs;

public enum ItemTypes {
    NONE("none"),
    MELEE("melee"),
    RANGED("ranged"),
    PROJECTILE("projectile"),
    CONSUMABLE("consumable"),
    BENCH("bench"),
    HEALTH_BENCH("healthbench"),
    COMPUTER_BENCH("computerbench"),
    SPEED_BENCH("speedbench");

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
