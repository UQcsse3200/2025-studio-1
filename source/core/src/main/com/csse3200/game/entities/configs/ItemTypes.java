package com.csse3200.game.entities.configs;

public enum ItemTypes {
    MELEE("melee"),
    RANGED("ranged"),
    PROJECTILE("projectile"),
    CONSUMABLE("consumable");

    private final String string;

    ItemTypes(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }
}
