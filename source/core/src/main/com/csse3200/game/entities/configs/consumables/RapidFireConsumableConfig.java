package com.csse3200.game.entities.configs.consumables;

import com.csse3200.game.effects.RapidFireEffect;

import java.util.ArrayList;

public class RapidFireConsumableConfig extends ConsumableConfig {

    public RapidFireConsumableConfig() {
        this.effects = new ArrayList<>();
        this.effects.add(new RapidFireEffect(2f));
        this.texturePath = "images/rapidfirepowerup.png";
        this.isProjectile = false;
    }
}
