package com.csse3200.game.entities.configs.weapons;

public enum WeaponTypes {
    MELEE("melee"),
    RANGED("ranged"),
    CONSUMABLE("consumable");

    private final String string;

    WeaponTypes(String string) {
        this.string = string;
    }

    public String getString() {
        return string;
    }
}
