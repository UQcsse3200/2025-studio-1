package com.csse3200.game.entities.configs.consumables;

import com.csse3200.game.effects.HealthEffect;

// Placeholder Consumable used for testing of Projectile Consumables
public class GenericProjectileConsumableConfig extends ProjectileConsumableConfig {
    public GenericProjectileConsumableConfig() {
        this.effects.add(new HealthEffect(10));
        this.texturePath = "images/mud.png";
    }

}
