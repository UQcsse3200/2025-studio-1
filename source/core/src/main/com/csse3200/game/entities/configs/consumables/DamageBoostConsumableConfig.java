package com.csse3200.game.entities.configs.consumables;

import com.csse3200.game.effects.DamageBoostEffect;

import java.util.ArrayList;

public class DamageBoostConsumableConfig extends ConsumableConfig {

    public DamageBoostConsumableConfig() {
        this.effects = new ArrayList<>();
        this.effects.add(new DamageBoostEffect(30f, 10));
        this.texturePath = "images/heart.png";
        this.isProjectile = false;
    }
}

