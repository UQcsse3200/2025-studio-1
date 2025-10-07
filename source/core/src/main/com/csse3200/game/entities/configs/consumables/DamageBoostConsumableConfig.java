package com.csse3200.game.entities.configs.consumables;

import com.csse3200.game.effects.DamageBoostEffect;

import java.util.ArrayList;

public class DamageBoostConsumableConfig extends ConsumableConfig {

    public DamageBoostConsumableConfig() {
        this.effects = new ArrayList<>();
        this.effects.add(new DamageBoostEffect(15f, 2.0f)); // 15 seconds, 2x damage
        this.texturePath = "images/heart.png";
        this.isProjectile = false;
    }
}
