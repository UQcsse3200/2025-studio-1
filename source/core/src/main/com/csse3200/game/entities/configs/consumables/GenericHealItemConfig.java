package com.csse3200.game.entities.configs.consumables;

import com.csse3200.game.effects.HealthEffect;

// Placeholder Consumable used for testing of Heal Items
public class GenericHealItemConfig extends ConsumableConfig {
    // Adds effect to heal 10 health
    public GenericHealItemConfig() {
        this.effects.add(new HealthEffect(-10));
        this.texturePath = "images/box_boy.png";
    }
}
