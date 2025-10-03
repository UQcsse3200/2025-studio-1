package com.csse3200.game.entities.configs;

import com.csse3200.game.entities.configs.consumables.ConsumableConfig;
import com.csse3200.game.entities.configs.consumables.HealthMonsterDrinkConfig;
import com.csse3200.game.entities.configs.consumables.LightningInABottleConfig;

public enum Consumables {
    HEALTH_MONSTER_DRINK(new HealthMonsterDrinkConfig()),
    LIGHTNING_IN_A_BOTTLE(new LightningInABottleConfig());

    private final ConsumableConfig config;

    Consumables(ConsumableConfig config) {
        this.config = config;
    }

    public ConsumableConfig getConfig() {
        return config;
    }
}
