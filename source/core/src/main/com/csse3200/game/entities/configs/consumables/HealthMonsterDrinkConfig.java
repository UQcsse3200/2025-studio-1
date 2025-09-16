package com.csse3200.game.entities.configs.consumables;

import com.csse3200.game.effects.HealthEffect;

import java.util.ArrayList;

// Placeholder Consumable used for testing of Heal Items
public class HealthMonsterDrinkConfig extends ConsumableConfig {
    // Adds effect to heal 10 health
    public HealthMonsterDrinkConfig() {
        this.effects = new ArrayList<>();
        this.effects.add(new HealthEffect(-100));
        this.texturePath = "images/monster.png";
        this.maxStack = 10;
    }
}
