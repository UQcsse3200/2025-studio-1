package com.csse3200.game.entities.configs;

import com.csse3200.game.entities.configs.consumables.ConsumableConfig;
import com.csse3200.game.entities.configs.consumables.GenericHealItemConfig;
import com.csse3200.game.entities.configs.consumables.GenericProjectileConsumableConfig;

public enum Consumables {
    GENERIC_HEAL_ITEM(new GenericHealItemConfig()),
    GENERIC_PROJECTILE_CONSUMABLE(new GenericProjectileConsumableConfig());

    private final ConsumableConfig config;

    Consumables(ConsumableConfig config) {
        this.config = config;
    }

    public ConsumableConfig getConfig() {
        return config;
    }
}
