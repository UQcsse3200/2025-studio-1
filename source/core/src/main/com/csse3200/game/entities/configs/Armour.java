package com.csse3200.game.entities.configs;

import com.csse3200.game.entities.configs.armour.ArmourConfig;
import com.csse3200.game.entities.configs.armour.ChestplateConfig;
import com.csse3200.game.entities.configs.armour.HoodConfig;

public enum Armour {
    HOOD(new HoodConfig()),
    CHESTPLATE(new ChestplateConfig());

    private final ArmourConfig config;

    Armour(ArmourConfig config) {this.config = config;}

    public ArmourConfig getConfig() {
        return this.config;
    }
}